package com.pilo.documents;

import com.pilo.cases.ProcedureCase;
import com.pilo.procedures.Requirement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class Document {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "case_id", nullable = false)
	private ProcedureCase procedureCase;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "requirement_id", nullable = false)
	private Requirement requirement;

	@Column(name = "file_name", nullable = false)
	private String fileName;

	@Column(name = "mime_type", nullable = false, length = 128)
	private String mimeType;

	@Column(name = "file_size", nullable = false)
	private long fileSize;

	@Column(name = "storage_key", nullable = false, length = 512)
	private String storageKey;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private DocumentStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public UUID getId() {
		return id;
	}

	public ProcedureCase getProcedureCase() {
		return procedureCase;
	}

	public void setProcedureCase(ProcedureCase procedureCase) {
		this.procedureCase = procedureCase;
	}

	public Requirement getRequirement() {
		return requirement;
	}

	public void setRequirement(Requirement requirement) {
		this.requirement = requirement;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getStorageKey() {
		return storageKey;
	}

	public void setStorageKey(String storageKey) {
		this.storageKey = storageKey;
	}

	public DocumentStatus getStatus() {
		return status;
	}

	public void setStatus(DocumentStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
