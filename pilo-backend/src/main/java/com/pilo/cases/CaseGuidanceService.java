package com.pilo.cases;

import com.pilo.documents.Document;
import com.pilo.documents.DocumentStatus;
import com.pilo.procedures.Requirement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CaseGuidanceService {

	public record CaseGuidance(String citizenSummary, String reviewerSummary) {}

	public CaseGuidance summarize(ProcedureCase procedureCase, List<Requirement> requirements, List<Document> documents) {
		long mandatory = requirements.stream().filter(Requirement::isMandatory).count();
		long validated =
				countMandatoryWithStatus(requirements, documents, DocumentStatus.VALIDATED);
		long rejected = countMandatoryWithStatus(requirements, documents, DocumentStatus.REJECTED)
				+ countMandatoryWithStatus(requirements, documents, DocumentStatus.PROCESSING_FAILED);
		long pending = countMandatoryWithoutUsableDocument(requirements, documents);

		String deadline = deadlinePhrase(procedureCase.getDeadlineAt(), procedureCase.getStatus());

		return switch (procedureCase.getStatus()) {
			case APPROVED -> new CaseGuidance(
					"Tu expediente ha sido aprobado. Conserva los justificantes por si la administración los solicita.",
					"Expediente aprobado en revisión final.");
			case REJECTED -> new CaseGuidance(
					"Tu expediente ha sido rechazado en la revisión final. Revisa el historial y contacta con la oficina si necesitas aclaraciones.",
					"Expediente rechazado en revisión final.");
			case UNDER_REVIEW -> new CaseGuidance(
					"""
							Has completado la documentación obligatoria. Un revisor está comprobando tus archivos \
							(fecha objetivo: %s). Si algún documento fue rechazado solo por la IA, el revisor puede validarlo igualmente."""
							.formatted(deadline),
					reviewerUnderReviewSummary(rejected, validated, mandatory));
			default -> citizenInProgressSummary(
					mandatory, validated, rejected, pending, procedureCase.getProgressPercentage(), deadline,
					reviewerInProgressSummary(rejected, validated, mandatory, pending));
		};
	}

	public String briefForReviewQueue(ProcedureCase procedureCase, List<Requirement> requirements, List<Document> documents) {
		long rejected = countMandatoryWithStatus(requirements, documents, DocumentStatus.REJECTED)
				+ countMandatoryWithStatus(requirements, documents, DocumentStatus.PROCESSING_FAILED);
		if (rejected > 0) {
			return rejected == 1
					? "1 obligatorio con rechazo automático: valorar falso negativo de la IA."
					: rejected + " obligatorios con rechazo automático: valorar falsos negativos de la IA.";
		}
		return "Documentación procesada; contrastar vista previa con extracción.";
	}

	private static String reviewerUnderReviewSummary(long rejected, long validated, long mandatory) {
		if (rejected > 0) {
			return """
					En cola de revisión: %d/%d obligatorios validados automáticamente; \
					%d con rechazo automático (posible discrepancia IA/humano)."""
					.formatted(validated, mandatory, rejected);
		}
		return "En cola de revisión: obligatorios procesados; contrastar PDF con campos extraídos.";
	}

	private static CaseGuidance citizenInProgressSummary(
			long mandatory,
			long validated,
			long rejected,
			long pending,
			int progressPct,
			String deadline,
			String reviewerSummary) {
		String citizen;
		if (pending >= mandatory && mandatory > 0) {
			citizen =
					"""
							Queda subir la documentación obligatoria (%s). Consulta la guía bajo cada requisito. \
							Progreso %d%%."""
							.formatted(deadline, progressPct);
		} else if (rejected > 0) {
			citizen =
					"""
							Hay %d documento(s) que la validación automática no aceptó. Lee «Qué puedes hacer» en cada uno \
							o espera a la revisión humana si el archivo es correcto. %s Progreso %d%%."""
							.formatted(rejected, deadline, progressPct);
		} else if (validated < mandatory) {
			citizen =
					"""
							Sigue completando los requisitos obligatorios. Cuando la IA termine de leer cada archivo, \
							revisa los datos extraídos. %s Progreso %d%%."""
							.formatted(deadline, progressPct);
		} else {
			citizen = "Documentación obligatoria en regla según las reglas automáticas. %s".formatted(deadline);
		}
		return new CaseGuidance(citizen.strip(), reviewerSummary.strip());
	}

	private static String reviewerInProgressSummary(long rejected, long validated, long mandatory, long pending) {
		return "Expediente en curso: %d/%d obligatorios validados; %d rechazados; %d pendientes de subir."
				.formatted(validated, mandatory, rejected, pending);
	}

	private static long countMandatoryWithStatus(
			List<Requirement> requirements, List<Document> documents, DocumentStatus status) {
		return requirements.stream()
				.filter(Requirement::isMandatory)
				.filter(requirement -> documents.stream()
						.anyMatch(document -> document.getRequirement().getId().equals(requirement.getId())
								&& document.getStatus() == status))
				.count();
	}

	private static long countMandatoryWithoutUsableDocument(List<Requirement> requirements, List<Document> documents) {
		return requirements.stream()
				.filter(Requirement::isMandatory)
				.filter(requirement -> documents.stream()
						.noneMatch(document -> {
							if (!document.getRequirement().getId().equals(requirement.getId())) {
								return false;
							}
							DocumentStatus status = document.getStatus();
							return status != DocumentStatus.PENDING_UPLOAD;
						}))
				.count();
	}

	private static String deadlinePhrase(Instant deadlineAt, CaseStatus status) {
		if (deadlineAt == null || status == CaseStatus.APPROVED || status == CaseStatus.REJECTED) {
			return "sin plazo";
		}
		long days = ChronoUnit.DAYS.between(Instant.now(), deadlineAt);
		if (days < 0) {
			return "plazo objetivo vencido";
		}
		if (days == 0) {
			return "plazo objetivo: hoy";
		}
		return "plazo objetivo: " + days + (days == 1 ? " día" : " días");
	}
}
