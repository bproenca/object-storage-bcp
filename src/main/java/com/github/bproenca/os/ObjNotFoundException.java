package com.github.bproenca.os;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ObjNotFoundException extends RuntimeException {
    public ObjNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
