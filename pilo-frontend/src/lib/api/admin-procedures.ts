import { getApiUrl } from "@/lib/api/config";
import { authFetch, readApiError } from "@/lib/api/client";
import type {
  AdminProcedureTypeDetail,
  AdminRequirement,
  ProcedureTypeSummary,
} from "@/lib/api/types";

export type ProcedureTypeInput = {
  title: string;
  description: string;
  targetDays: number;
};

export type ValidationRulesInput = {
  expectedDocumentType: string;
  requireFutureExpiration: boolean;
  minConfidence?: number | null;
};

export type RequirementInput = {
  code: string;
  name: string;
  description: string;
  mandatory: boolean;
  validationRules: ValidationRulesInput;
};

export async function listAdminProcedures(): Promise<ProcedureTypeSummary[]> {
  const response = await authFetch(`${getApiUrl()}/api/v1/admin/procedures/types`, {
    cache: "no-store",
  });
  if (!response.ok) {
    await readApiError(response);
  }
  return (await response.json()) as ProcedureTypeSummary[];
}

export async function getAdminProcedure(id: string): Promise<AdminProcedureTypeDetail> {
  const response = await authFetch(`${getApiUrl()}/api/v1/admin/procedures/types/${id}`, {
    cache: "no-store",
  });
  if (!response.ok) {
    await readApiError(response);
  }
  return (await response.json()) as AdminProcedureTypeDetail;
}

export async function createAdminProcedure(input: ProcedureTypeInput): Promise<AdminProcedureTypeDetail> {
  const response = await authFetch(`${getApiUrl()}/api/v1/admin/procedures/types`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });
  if (!response.ok) {
    await readApiError(response);
  }
  return (await response.json()) as AdminProcedureTypeDetail;
}

export async function updateAdminProcedure(
  id: string,
  input: ProcedureTypeInput,
): Promise<AdminProcedureTypeDetail> {
  const response = await authFetch(`${getApiUrl()}/api/v1/admin/procedures/types/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });
  if (!response.ok) {
    await readApiError(response);
  }
  return (await response.json()) as AdminProcedureTypeDetail;
}

export async function createAdminRequirement(
  procedureId: string,
  input: RequirementInput,
): Promise<AdminRequirement> {
  const response = await authFetch(
    `${getApiUrl()}/api/v1/admin/procedures/types/${procedureId}/requirements`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(input),
    },
  );
  if (!response.ok) {
    await readApiError(response);
  }
  return (await response.json()) as AdminRequirement;
}

export async function updateAdminRequirement(
  procedureId: string,
  requirementId: string,
  input: Omit<RequirementInput, "code">,
): Promise<AdminRequirement> {
  const response = await authFetch(
    `${getApiUrl()}/api/v1/admin/procedures/types/${procedureId}/requirements/${requirementId}`,
    {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(input),
    },
  );
  if (!response.ok) {
    await readApiError(response);
  }
  return (await response.json()) as AdminRequirement;
}

export async function deleteAdminRequirement(procedureId: string, requirementId: string): Promise<void> {
  const response = await authFetch(
    `${getApiUrl()}/api/v1/admin/procedures/types/${procedureId}/requirements/${requirementId}`,
    { method: "DELETE" },
  );
  if (!response.ok) {
    await readApiError(response);
  }
}
