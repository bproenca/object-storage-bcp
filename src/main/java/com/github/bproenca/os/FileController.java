package com.github.bproenca.os;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;

@RestController
public class FileController {
    
    @Autowired
    private MinioClient minioClient;

    @Value("${application.bucket.name}")
    private String bucketName;

    private Logger logger = LoggerFactory.getLogger(FileController.class);

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getListOfFiles(@RequestParam(value = "prefix", defaultValue = "") String prefix) throws Exception{
        logger.info("# listObjects in bucket {} with prefix {}", bucketName, prefix);
        ListObjectsArgs listArgs = ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).recursive(true).build();
        
        Iterable<Result<Item>> listObj = minioClient.listObjects(listArgs);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Result<Item> obj : listObj) {
                Map<String, Object> map = new HashMap<>();
                Item item = obj.get();
                map.put("name", item.objectName());
                map.put("size", readableFileSize(item.size()));
                result.add(map);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/upload")
    public String uploadFile(   @RequestParam("fileName") String fileName,
                                @RequestParam("file") MultipartFile file) throws Exception {
        logger.info("# upload object {}", fileName);
        Instant start = Instant.now();

        minioClient.putObject(PutObjectArgs.builder()
            .bucket(bucketName)
            .object(fileName)
            .stream(file.getInputStream(), file.getSize(), -1)
            .contentType(file.getContentType())
            .build());
        
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        logger.info("# upload elapsed time {} seconds", timeElapsed.toSeconds());
        return "File " + fileName + " uploaded successfully in " + timeElapsed + " seconds";
    }
    
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam(value = "fileName", required = true) String fileName) throws Exception {
        logger.info("# download object {}", fileName);
        InputStream inputStream;
        try {
            inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
        } catch (ErrorResponseException e) {
            String msg = "Object " + fileName + " not found in bucket " + bucketName;
            throw new ObjNotFoundException(msg, e);
        }
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
        return ResponseEntity
                .ok()
                .header("Content-Disposition", "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(inputStreamResource);
    }

    @DeleteMapping(value = "/delete")
    public String delete(@RequestParam(value = "fileName", required = true) String fileName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
        return "File: " + fileName + " deleted";
    }


    @GetMapping(value = "/memory")
    public String memory() {
        int mb = 1024 * 1024;
        Runtime runtime = Runtime.getRuntime();
        StringBuilder builder = new StringBuilder();
        builder.append("\tUsed Memory   : " + (runtime.totalMemory() - runtime.freeMemory()) / mb + " mb");
        builder.append("\tFree Memory   : " + runtime.freeMemory() / mb + " mb");
        builder.append("\tTotal Memory  : " + runtime.totalMemory() / mb + " mb");
        builder.append("\tMax Memory    : " + runtime.maxMemory() / mb + " mb");
        return builder.toString();
    }

    private String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
