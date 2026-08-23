package com.pilo.common;

import com.pilo.auth.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(InvalidCredentialsException.class)
	ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ignored) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new ApiError(
						"INVALID_CREDENTIALS", "El correo o la contraseña no son correctos."));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ignored) {
		return ResponseEntity.badRequest()
				.body(new ApiError("VALIDATION_ERROR", "Revisa los datos del formulario."));
	}
}
