package com.pilo.workflows;

import com.pilo.cases.ProcedureCase;
import com.pilo.users.Role;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow_tasks")
public class WorkflowTask {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "case_id", nullable = false)
	private ProcedureCase procedureCase;

	@Column(name = "task_code", nullable = false, length = 64)
	private String taskCode;

	@Column(name = "task_name", nullable = false, length = 128)
	private String taskName;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Enumerated(EnumType.STRING)
	@Column(name = "completion_rule", nullable = false, length = 64)
	private CompletionRule completionRule;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private WorkflowTaskStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "assigned_role", nullable = false, length = 32)
	private Role assignedRole;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "depends_on_task_id")
	private WorkflowTask dependsOnTask;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
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

	public String getTaskCode() {
		return taskCode;
	}

	public void setTaskCode(String taskCode) {
		this.taskCode = taskCode;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public CompletionRule getCompletionRule() {
		return completionRule;
	}

	public void setCompletionRule(CompletionRule completionRule) {
		this.completionRule = completionRule;
	}

	public WorkflowTaskStatus getStatus() {
		return status;
	}

	public void setStatus(WorkflowTaskStatus status) {
		this.status = status;
	}

	public Role getAssignedRole() {
		return assignedRole;
	}

	public void setAssignedRole(Role assignedRole) {
		this.assignedRole = assignedRole;
	}

	public WorkflowTask getDependsOnTask() {
		return dependsOnTask;
	}

	public void setDependsOnTask(WorkflowTask dependsOnTask) {
		this.dependsOnTask = dependsOnTask;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
