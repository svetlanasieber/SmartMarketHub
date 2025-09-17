package com.smartmarkethub.smart.web.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s with %s: '%s' already exists", resourceName, fieldName, fieldValue));
    }
}