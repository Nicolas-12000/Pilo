package com.pilo.auth;

public class InvalidCurrentPasswordException extends RuntimeException {

	public InvalidCurrentPasswordException() {
		super("INVALID_CURRENT_PASSWORD");
	}
}
