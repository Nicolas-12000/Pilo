package com.pilo.workflows;

import com.pilo.procedures.ProcedureType;
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
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "workflow_step_definitions")
public class WorkflowStepDefinition {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "procedure_type_id", nullable = false)
	private ProcedureType procedureType;

	@Column(name = "task_code", nullable = false, length = 64)
	private String taskCode;

	@Column(name = "display_name", nullable = false, length = 128)
	private String displayName;

	@Enumerated(EnumType.STRING)
	@Column(name = "assigned_role", nullable = false, length = 32)
	private Role assignedRole;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(name = "depends_on_task_code", length = 64)
	private String dependsOnTaskCode;

	@Enumerated(EnumType.STRING)
	@Column(name = "completion_rule", nullable = false, length = 64)
	private CompletionRule completionRule;

	public UUID getId() {
		return id;
	}

	public ProcedureType getProcedureType() {
		return procedureType;
	}

	public void setProcedureType(ProcedureType procedureType) {
		this.procedureType = procedureType;
	}

	public String getTaskCode() {
		return taskCode;
	}

	public void setTaskCode(String taskCode) {
		this.taskCode = taskCode;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Role getAssignedRole() {
		return assignedRole;
	}

	public void setAssignedRole(Role assignedRole) {
		this.assignedRole = assignedRole;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getDependsOnTaskCode() {
		return dependsOnTaskCode;
	}

	public void setDependsOnTaskCode(String dependsOnTaskCode) {
		this.dependsOnTaskCode = dependsOnTaskCode;
	}

	public CompletionRule getCompletionRule() {
		return completionRule;
	}

	public void setCompletionRule(CompletionRule completionRule) {
		this.completionRule = completionRule;
	}
}
