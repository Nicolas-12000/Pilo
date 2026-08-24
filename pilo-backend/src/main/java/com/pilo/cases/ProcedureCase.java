package com.pilo.cases;

import com.pilo.procedures.ProcedureType;
import com.pilo.users.User;
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
@Table(name = "procedure_cases")
public class ProcedureCase {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "case_number", nullable = false, unique = true, length = 32)
	private String caseNumber;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "procedure_type_id", nullable = false)
	private ProcedureType procedureType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private CaseStatus status;

	@Column(name = "progress_percentage", nullable = false)
	private int progressPercentage;

	@Column(name = "deadline_at", nullable = false)
	private Instant deadlineAt;

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

	public String getCaseNumber() {
		return caseNumber;
	}

	public void setCaseNumber(String caseNumber) {
		this.caseNumber = caseNumber;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public ProcedureType getProcedureType() {
		return procedureType;
	}

	public void setProcedureType(ProcedureType procedureType) {
		this.procedureType = procedureType;
	}

	public CaseStatus getStatus() {
		return status;
	}

	public void setStatus(CaseStatus status) {
		this.status = status;
	}

	public int getProgressPercentage() {
		return progressPercentage;
	}

	public void setProgressPercentage(int progressPercentage) {
		this.progressPercentage = progressPercentage;
	}

	public Instant getDeadlineAt() {
		return deadlineAt;
	}

	public void setDeadlineAt(Instant deadlineAt) {
		this.deadlineAt = deadlineAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
