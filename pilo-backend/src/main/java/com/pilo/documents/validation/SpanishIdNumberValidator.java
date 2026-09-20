package com.pilo.documents.validation;

import java.util.Locale;

final class SpanishIdNumberValidator {

	private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

	private SpanishIdNumberValidator() {}

	static boolean isValid(String raw) {
		if (raw == null || raw.isBlank()) {
			return false;
		}
		String value = raw.trim().toUpperCase(Locale.ROOT).replace(" ", "").replace("-", "");
		if (value.matches("\\d{8}[A-Z]")) {
			int number = Integer.parseInt(value.substring(0, 8));
			return value.charAt(8) == DNI_LETTERS.charAt(number % 23);
		}
		if (value.matches("[XYZ]\\d{7}[A-Z]")) {
			char prefix = value.charAt(0);
			int prefixValue = prefix == 'X' ? 0 : prefix == 'Y' ? 1 : 2;
			int number = prefixValue * 10_000_000 + Integer.parseInt(value.substring(1, 8));
			return value.charAt(8) == DNI_LETTERS.charAt(number % 23);
		}
		return false;
	}
}
