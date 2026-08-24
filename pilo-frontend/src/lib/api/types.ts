export type Role = "USER" | "REVIEWER" | "ADMIN";

export type User = {
  id: string;
  email: string;
  fullName: string;
  role: Role;
};

export type LoginResponse = {
  accessToken: string;
  tokenType: "Bearer";
  expiresIn: number;
  user: User;
};

export type ApiErrorBody = {
  error: string;
  message: string;
};

export type ProcedureTypeSummary = {
  id: string;
  title: string;
  description: string;
  targetDays: number;
  requirementCount: number;
};

export type Requirement = {
  id: string;
  code: string;
  name: string;
  description: string;
  mandatory: boolean;
};

export type ProcedureTypeDetail = ProcedureTypeSummary & {
  requirements: Requirement[];
};

export type CaseStatus =
  | "PENDING"
  | "IN_PROGRESS"
  | "UNDER_REVIEW"
  | "APPROVED"
  | "REJECTED";

export type DocumentStatus =
  | "UPLOADED"
  | "PROCESSING"
  | "VALIDATED"
  | "REJECTED"
  | "PROCESSING_FAILED";

export type WorkflowTaskStatus = "PENDING" | "IN_PROGRESS" | "COMPLETED" | "BLOCKED";

export type RequirementFulfillmentStatus =
  | "PENDING"
  | "UPLOADED"
  | "PROCESSING"
  | "FULFILLED"
  | "REJECTED";

export type CaseSummary = {
  id: string;
  caseNumber: string;
  procedureTypeId: string;
  procedureTypeTitle: string;
  status: CaseStatus;
  progressPercentage: number;
  deadlineAt: string;
  createdAt: string;
};

export type CaseRequirementStatus = {
  id: string;
  code: string;
  name: string;
  description: string;
  mandatory: boolean;
  fulfillmentStatus: RequirementFulfillmentStatus;
};

export type AuditEvent = {
  id: string;
  action: string;
  resource: string;
  result: string;
  metadata: string;
  timestamp: string;
};

export type CaseDetail = CaseSummary & {
  procedureTypeDescription: string;
  requirements: CaseRequirementStatus[];
  recentAuditEvents: AuditEvent[];
};

export type DocumentRecord = {
  id: string;
  requirementId: string;
  requirementCode: string;
  fileName: string;
  mimeType: string;
  fileSize: number;
  status: DocumentStatus;
};

export type WorkflowTask = {
  id: string;
  taskCode: string;
  taskName: string;
  status: WorkflowTaskStatus;
  assignedRole: Role;
  dependsOnTaskId: string | null;
};

export type Workflow = {
  caseId: string;
  tasks: WorkflowTask[];
};

export type ExternalReference = {
  sourceName: string;
  externalId: string;
  title: string;
};
