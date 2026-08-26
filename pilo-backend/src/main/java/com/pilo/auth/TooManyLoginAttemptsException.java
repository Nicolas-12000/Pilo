package com.pilo.auth;

public class TooManyLoginAttemptsException extends RuntimeException {

	public TooManyLoginAttemptsException() {
		super("TOO_MANY_LOGIN_ATTEMPTS");
	}
}
