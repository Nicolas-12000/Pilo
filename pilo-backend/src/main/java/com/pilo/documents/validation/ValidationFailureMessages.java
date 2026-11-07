package com.pilo.documents.validation;

import java.util.List;
import java.util.Map;

/** Spanish copy for rule codes. The API and the guidance text both read from here. */
public final class ValidationFailureMessages {

	public record Message(String code, String label, String hint) {}

	private static final Map<String, Message> BY_CODE = Map.of(
			"CONFIDENCE",
			new Message(
					"CONFIDENCE",
					"La confianza de la IA está por debajo del umbral mínimo.",
					"Sube una copia más nítida o un PDF generado, no una captura borrosa."),
			"EXPECTED_DOCUMENT_TYPE",
			new Message(
					"EXPECTED_DOCUMENT_TYPE",
					"El tipo de documento detectado no coincide con el requisito.",
					"Comprueba que estás subiendo el documento que describe el requisito."),
			"FUTURE_EXPIRATION",
			new Message(
					"FUTURE_EXPIRATION",
					"La fecha de vencimiento no es válida o ya ha expirado.",
					"Renueva el documento si está caducado o caduca en breve."),
			"REQUIRED_FIELDS",
			new Message(
					"REQUIRED_FIELDS",
					"Faltan campos obligatorios extraídos del documento.",
					"Asegúrate de que todo el texto relevante sea visible, sin recortes."),
			"ID_NUMBER_FORMAT",
			new Message(
					"ID_NUMBER_FORMAT",
					"El número de identificación no tiene un formato DNI/NIE válido.",
					"Este trámite exige DNI o NIE español legible."),
			"DATE_COHERENCE",
			new Message(
					"DATE_COHERENCE",
					"Las fechas extraídas no son coherentes entre sí.",
					"Si las fechas se ven mal en la vista previa, vuelve a escanear el documento."));

	private ValidationFailureMessages() {}

	public static Message of(String code) {
		Message known = BY_CODE.get(code);
		if (known != null) {
			return known;
		}
		return new Message(code, code, "Comprueba el archivo y vuelve a intentarlo.");
	}

	public static List<Message> ofAll(List<String> codes) {
		if (codes == null || codes.isEmpty()) {
			return List.of();
		}
		return codes.stream().map(ValidationFailureMessages::of).toList();
	}
}
