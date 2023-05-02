# Getting Started

### Start

```sh
mvn spring-boot:run  -Dspring-boot.run.jvmArguments="-Xmx50M"  -Dspring-boot.run.arguments=" \
 --S3_ACCESS_KEY=<?> \
 --S3_SECRET_KEY=<?> \
 --S3_ENDPOINT=<?> \
 --S3_REGION=sa-saopaulo-1 \
 --S3_BUCKET=<?>"
```

### Test

Using http://httpie.io/
```sh
# List Objects
http localhost:8080/list?prefix=bcp/

# Upload Object
http -f POST localhost:8080/upload fileName='bcp/myfile' file@'~/wks/tmp/s3/myfile;type=application/txt'

# Download Object
http localhost:8080/download?fileName=bcp/myfile

# Delete Object
http DELETE localhost:8080/delete?fileName=bcp/myfile

# Presigned GET Objects
http POST localhost:8080/presignedurl/get?fileName=bcp/tb131.txt
http GET $(http POST localhost:8080/presignedurl/get?fileName=bcp/tb131.txt)

```