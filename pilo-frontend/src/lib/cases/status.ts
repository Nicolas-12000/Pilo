import {
  AlertTriangle,
  CheckCircle2,
  Clock,
  Loader2,
  XCircle,
  type LucideIcon,
} from "lucide-react";
import type { BadgeTone } from "@/components/ui/badge";
import type {
  CaseStatus,
  DocumentStatus,
  RequirementFulfillmentStatus,
  WorkflowTaskStatus,
} from "@/lib/api/types";

export type StatusDescriptor = {
  label: string;
  tone: BadgeTone;
  icon: LucideIcon;
  spin?: boolean;
};

const PENDING: StatusDescriptor = { label: "Pendiente", tone: "pending", icon: Clock };
const PROCESSING: StatusDescriptor = {
  label: "Procesando",
  tone: "in-progress",
  icon: Loader2,
  spin: true,
};

export const caseStatus: Record<CaseStatus, StatusDescriptor> = {
  PENDING,
  IN_PROGRESS: { label: "En curso", tone: "in-progress", icon: Loader2, spin: true },
  UNDER_REVIEW: { label: "En revisión", tone: "under-review", icon: AlertTriangle },
  APPROVED: { label: "Aprobado", tone: "approved", icon: CheckCircle2 },
  REJECTED: { label: "Rechazado", tone: "rejected", icon: XCircle },
};

export const documentStatus: Record<DocumentStatus, StatusDescriptor> = {
  UPLOADED: { label: "Subido", tone: "pending", icon: Clock },
  PROCESSING: PROCESSING,
  VALIDATED: { label: "Validado", tone: "approved", icon: CheckCircle2 },
  REJECTED: { label: "Rechazado", tone: "rejected", icon: XCircle },
  PROCESSING_FAILED: { label: "Error al procesar", tone: "rejected", icon: XCircle },
};

export const requirementStatus: Record<RequirementFulfillmentStatus, StatusDescriptor> = {
  PENDING,
  UPLOADED: { label: "Subido", tone: "pending", icon: Clock },
  PROCESSING: { label: "Analizando con IA", tone: "in-progress", icon: Loader2, spin: true },
  FULFILLED: { label: "Validado", tone: "approved", icon: CheckCircle2 },
  REJECTED: { label: "Rechazado", tone: "rejected", icon: XCircle },
};

export const workflowTaskStatus: Record<WorkflowTaskStatus, StatusDescriptor> = {
  PENDING,
  IN_PROGRESS: { label: "En curso", tone: "in-progress", icon: Loader2, spin: true },
  COMPLETED: { label: "Completado", tone: "approved", icon: CheckCircle2 },
  BLOCKED: { label: "Bloqueado", tone: "pending", icon: Clock },
};

export const auditActionLabels: Record<string, string> = {
  CASE_CREATED: "Expediente creado",
  DOCUMENT_UPLOADED: "Documento subido",
  DOCUMENT_VALIDATED: "Documento validado",
  DOCUMENT_REJECTED: "Documento rechazado",
  DOCUMENT_PROCESSING_FAILED: "Error al procesar el documento",
};

export function auditActionLabel(action: string) {
  return auditActionLabels[action] ?? action;
}
