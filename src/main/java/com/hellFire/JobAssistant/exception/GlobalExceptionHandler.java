package com.hellFire.JobAssistant.exception;

import java.time.Instant;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private final boolean exposeInternalErrorDetails;

	public GlobalExceptionHandler(
			@Value("${app.api.expose-internal-error-details:false}") boolean exposeInternalErrorDetails) {
		this.exposeInternalErrorDetails = exposeInternalErrorDetails;
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, WebRequest request) {
		int status = ex.getStatus();
		HttpStatus resolved = HttpStatus.resolve(status);
		String reason = resolved != null ? resolved.getReasonPhrase() : "Error";
		return ResponseEntity
				.status(status)
				.body(error(status, reason, ex.getMessage(), request));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
		String msg = ex.getBindingResult().getFieldErrors().stream()
				.map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return ResponseEntity
				.badRequest()
				.body(error(400, "Bad Request", msg, request));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
		return ResponseEntity
				.status(HttpStatus.FORBIDDEN)
				.body(error(403, "Forbidden", "You do not have permission to access this resource", request));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
		log.error("Unhandled exception", ex);
		String message = exposeInternalErrorDetails && ex.getMessage() != null
				? ex.getMessage()
				: "An unexpected error occurred";
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(error(500, "Internal Server Error", message, request));
	}

	private static ErrorResponse error(int status, String error, String message, WebRequest request) {
		String path = "";
		if (request instanceof ServletWebRequest swr) {
			path = swr.getRequest().getRequestURI();
		}
		return new ErrorResponse(Instant.now(), status, error, message, path);
	}
}
