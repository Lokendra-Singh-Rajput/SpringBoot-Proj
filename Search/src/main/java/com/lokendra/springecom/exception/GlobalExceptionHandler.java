package com.lokendra.springecom.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Resource Not Found");
        pd.setType(URI.create("https://springecom.dev/errors/not-found"));
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("path", request.getRequestURI());
        return pd;
    }

    
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Bad Request");
        pd.setType(URI.create("https://springecom.dev/errors/bad-request"));
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("path", request.getRequestURI());
        return pd;
    }

    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxSizeExceeded(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "File size exceeds the maximum allowed limit.");
        pd.setTitle("File Too Large");
        pd.setType(URI.create("https://springecom.dev/errors/file-too-large"));
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("path", request.getRequestURI());
        return pd;
    }

    
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "Access Denied: You do not have permission to perform this action.");
        pd.setTitle("Forbidden");
        pd.setType(URI.create("https://springecom.dev/errors/forbidden"));
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("path", request.getRequestURI());
        return pd;
    }

    
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(
            org.springframework.security.authentication.BadCredentialsException ex, HttpServletRequest request) {

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password.");
        pd.setTitle("Unauthorized");
        pd.setType(URI.create("https://springecom.dev/errors/unauthorized"));
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("path", request.getRequestURI());
        return pd;
    }

    
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(
            Exception ex, HttpServletRequest request) {

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
        pd.setTitle("Internal Server Error");
        pd.setType(URI.create("https://springecom.dev/errors/internal"));
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("path", request.getRequestURI());
        
        pd.setProperty("debug", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        return pd;
    }
}
