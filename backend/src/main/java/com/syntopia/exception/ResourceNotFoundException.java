package com.syntopia.exception;

/**
 * Generic resource not found exception to produce 404 responses via GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
