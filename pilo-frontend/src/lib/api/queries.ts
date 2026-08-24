import { getCaseDetail } from "@/lib/api/cases";
import { listDocuments } from "@/lib/api/documents";
import { getWorkflow } from "@/lib/api/workflow";
import type { CaseDetail, DocumentRecord, Workflow } from "@/lib/api/types";

export const caseKeys = {
  all: ["cases"] as const,
  list: () => [...caseKeys.all, "list"] as const,
  bundle: (caseId: string) => [...caseKeys.all, "bundle", caseId] as const,
};

export const procedureKeys = {
  all: ["procedures"] as const,
  list: () => [...procedureKeys.all, "list"] as const,
  detail: (id: string) => [...procedureKeys.all, "detail", id] as const,
};

export type CaseBundle = {
  detail: CaseDetail;
  workflow: Workflow;
  documents: DocumentRecord[];
};

export async function fetchCaseBundle(caseId: string): Promise<CaseBundle> {
  const [detail, workflow, documents] = await Promise.all([
    getCaseDetail(caseId),
    getWorkflow(caseId),
    listDocuments(caseId),
  ]);
  return { detail, workflow, documents };
}

/** The async AI pipeline runs after upload, so the UI polls while anything is mid-flight. */
export function hasWorkInProgress(bundle: CaseBundle | undefined) {
  if (!bundle) {
    return false;
  }
  return (
    bundle.detail.requirements.some(
      (requirement) => requirement.fulfillmentStatus === "PROCESSING",
    ) || bundle.documents.some((document) => document.status === "PROCESSING")
  );
}
