package com.pilo.auth;

public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("INVALID_CREDENTIALS");
	}
}
