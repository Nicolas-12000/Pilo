package com.pilo.documents;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "document_extractions")
public class DocumentExtraction {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "document_id", nullable = false, unique = true)
	private Document document;

	@Column(name = "document_type_detected", nullable = false, length = 128)
	private String documentTypeDetected;

	@Column(nullable = false)
	private double confidence;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "extracted_data", nullable = false, columnDefinition = "jsonb")
	private String extractedData;

	@Column(name = "processed_at", nullable = false)
	private Instant processedAt;

	@PrePersist
	void onCreate() {
		if (processedAt == null) {
			processedAt = Instant.now();
		}
	}

	public UUID getId() {
		return id;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public String getDocumentTypeDetected() {
		return documentTypeDetected;
	}

	public void setDocumentTypeDetected(String documentTypeDetected) {
		this.documentTypeDetected = documentTypeDetected;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public String getExtractedData() {
		return extractedData;
	}

	public void setExtractedData(String extractedData) {
		this.extractedData = extractedData;
	}

	public Instant getProcessedAt() {
		return processedAt;
	}
}
