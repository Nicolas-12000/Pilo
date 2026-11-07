package com.pilo.common;

import com.pilo.auth.InvalidCredentialsException;
import com.pilo.auth.InvalidCurrentPasswordException;
import com.pilo.auth.TooManyLoginAttemptsException;
import com.pilo.procedures.ProcedureTypeNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(InvalidCredentialsException.class)
	ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ignored) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new ApiError(
						"INVALID_CREDENTIALS", "El correo o la contraseña no son correctos."));
	}

	@ExceptionHandler(InvalidCurrentPasswordException.class)
	ResponseEntity<ApiError> handleInvalidCurrentPassword(InvalidCurrentPasswordException ignored) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ApiError("INVALID_CURRENT_PASSWORD", "La contraseña actual no es correcta."));
	}

	@ExceptionHandler(TooManyLoginAttemptsException.class)
	ResponseEntity<ApiError> handleTooManyLoginAttempts(TooManyLoginAttemptsException ignored) {
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
				.body(new ApiError(
						"TOO_MANY_LOGIN_ATTEMPTS", "Demasiados intentos. Inténtalo de nuevo en unos minutos."));
	}

	// @PreAuthorize denials surface inside MVC; unhandled they fall through to /error and become a 401.
	@ExceptionHandler(AccessDeniedException.class)
	ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ignored) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(new ApiError("FORBIDDEN", "Tu cuenta no tiene permisos para esta acción."));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ignored) {
		return ResponseEntity.badRequest()
				.body(new ApiError("VALIDATION_ERROR", "Revisa los datos del formulario."));
	}

	@ExceptionHandler(ProcedureTypeNotFoundException.class)
	ResponseEntity<ApiError> handleProcedureTypeNotFound(ProcedureTypeNotFoundException ignored) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiError("PROCEDURE_TYPE_NOT_FOUND", "No hemos encontrado ese trámite."));
	}

	@ExceptionHandler(ResponseStatusException.class)
	ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException exception) {
		String code = exception.getReason() == null ? "REQUEST_FAILED" : exception.getReason();
		return ResponseEntity.status(exception.getStatusCode())
				.body(new ApiError(code, userMessage(code)));
	}

	private static String userMessage(String code) {
		return switch (code) {
			case "WORKFLOW_DEFINITION_MISSING" ->
				"Este trámite aún no tiene un flujo de trabajo configurado.";
			case "PROCEDURE_TYPE_NOT_FOUND" -> "No hemos encontrado ese trámite.";
			case "PROCEDURE_TYPE_ARCHIVED" -> "Este trámite ya no admite solicitudes nuevas.";
			case "REQUIREMENT_NOT_FOUND" -> "No hemos encontrado ese requisito.";
			case "DOCUMENT_DELETE_FORBIDDEN" -> "Solo el titular del expediente puede eliminar documentos.";
			case "DOCUMENT_CANNOT_DELETE" -> "No puedes eliminar un documento mientras se está analizando.";
			case "CASE_CLOSED" -> "El expediente ya está cerrado.";
			case "REQUIREMENT_MISMATCH" -> "Ese requisito no pertenece a este expediente.";
			case "EMPTY_FILE" -> "El archivo está vacío.";
			case "DOCUMENT_NOT_FOUND" -> "No hemos encontrado ese documento.";
			case "DOCUMENT_NOT_READY" -> "El documento todavía no está disponible para previsualizar.";
			case "INVALID_FILE_NAME" -> "El nombre del archivo no es válido.";
			case "INVALID_FILE_SIZE" -> "El archivo supera el tamaño permitido.";
			case "INVALID_MIME_TYPE" -> "Solo se admiten PDF, JPG, PNG o WEBP.";
			case "CANNOT_DISABLE_SELF" -> "No puedes desactivar tu propia cuenta.";
			case "LAST_ADMIN" -> "Tiene que quedar al menos un administrador activo.";
			case "USER_NOT_FOUND" -> "No hemos encontrado ese usuario.";
			default -> "No se ha podido completar la petición.";
		};
	}
}
