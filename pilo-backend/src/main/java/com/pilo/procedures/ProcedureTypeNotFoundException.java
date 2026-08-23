package com.pilo.procedures;

public class ProcedureTypeNotFoundException extends RuntimeException {

	public ProcedureTypeNotFoundException() {
		super("PROCEDURE_TYPE_NOT_FOUND");
	}
}
