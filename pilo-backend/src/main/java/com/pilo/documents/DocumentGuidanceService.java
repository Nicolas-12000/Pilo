package com.pilo.documents;

import com.pilo.ai.StructuredExtraction;
import com.pilo.documents.validation.ValidationFailureMessages;
import com.pilo.documents.validation.ValidationResult;
import com.pilo.procedures.Requirement;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DocumentGuidanceService {

	public record DocumentGuidance(String citizenGuidance, String reviewerSummary) {}

	public DocumentGuidance build(Requirement requirement, StructuredExtraction extraction, ValidationResult validation) {
		if (validation.valid()) {
			return buildValidated(requirement, extraction);
		}
		return buildRejected(requirement, extraction, validation.failures());
	}

	private DocumentGuidance buildValidated(Requirement requirement, StructuredExtraction extraction) {
		int confidencePct = (int) Math.round(extraction.confidence() * 100);
		String citizen = """
				El archivo para «%s» ha superado las comprobaciones automáticas. Abre la vista previa \
				y confirma que los datos extraídos (nombre, fechas, números) coinciden con tu documento. \
				Si algo no cuadra, puedes quitar el archivo y subir otro escaneado o fotografiado con buena luz."""
				.formatted(requirement.getName());
		String reviewer = """
				%s: extracción automática OK (tipo %s, confianza %d%%). \
				Contrasta el PDF o imagen con los campos listados antes de cerrar el requisito."""
				.formatted(requirement.getName(), extraction.documentType(), confidencePct);
		return new DocumentGuidance(citizen.strip(), reviewer.strip());
	}

	private DocumentGuidance buildRejected(
			Requirement requirement, StructuredExtraction extraction, List<String> failures) {
		int confidencePct = (int) Math.round(extraction.confidence() * 100);
		StringBuilder citizen = new StringBuilder();
		citizen.append("No hemos podido validar automáticamente «")
				.append(requirement.getName())
				.append("». ");
		if (!requirement.getDescription().isBlank()) {
			citizen.append(requirement.getDescription().trim()).append(' ');
		}
		citizen.append(
				"Sube otro archivo (foto nítida, sin reflejos, PDF completo) o el documento que corresponda a este requisito. ");
		citizen.append(
				"Si crees que el archivo es correcto, puedes dejarlo: un revisor humano puede confirmarlo aunque la IA haya fallado.");

		String failureLabels = ValidationFailureMessages.ofAll(failures).stream()
				.map(ValidationFailureMessages.Message::label)
				.collect(Collectors.joining(" "));
		StringBuilder reviewer = new StringBuilder();
		reviewer.append(requirement.getName())
				.append(": rechazo automático. ")
				.append(failureLabels)
				.append(" Tipo detectado ")
				.append(extraction.documentType())
				.append(", confianza ")
				.append(confidencePct)
				.append("%. ");
		reviewer.append("Abrir vista previa; si el documento es válido en la práctica, aprobar el expediente ");
		reviewer.append("(se registrará discrepancia con la validación automática).");

		return new DocumentGuidance(citizen.toString().strip(), reviewer.toString().strip());
	}
}
