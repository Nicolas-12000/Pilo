import { getApiUrl } from "@/lib/api/config";
import type { ApiErrorBody, ProcedureTypeDetail, ProcedureTypeSummary } from "@/lib/api/types";

/** Amplify/`next build` has no API rewrite yet; relative `/api` hangs until Next aborts (~60s). */
function isNextProductionBuild() {
  return process.env.NEXT_PHASE === "phase-production-build";
}

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code?: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

async function readApiError(response: Response) {
  const body = (await response.json().catch(() => null)) as ApiErrorBody | null;
  throw new ApiError(body?.message ?? "No se ha podido completar la petición.", response.status, body?.error);
}

// Public, non-user-specific catalog: safe to let Next's fetch cache serve it for a short
// window across requests/users, cutting load on the backend without risking stale per-user data.
export async function listProcedureTypes(): Promise<ProcedureTypeSummary[]> {
  if (isNextProductionBuild()) {
    return [];
  }
  const response = await fetch(`${getApiUrl()}/api/v1/procedures/types`, {
    next: { revalidate: 60 },
    signal: AbortSignal.timeout(15_000),
  });

  if (!response.ok) {
    await readApiError(response);
  }

  return (await response.json()) as ProcedureTypeSummary[];
}

export async function getProcedureType(id: string): Promise<ProcedureTypeDetail> {
  if (isNextProductionBuild()) {
    throw new ApiError("Build sin API", 503);
  }
  const response = await fetch(`${getApiUrl()}/api/v1/procedures/types/${id}`, {
    next: { revalidate: 60 },
    signal: AbortSignal.timeout(15_000),
  });

  if (!response.ok) {
    await readApiError(response);
  }

  return (await response.json()) as ProcedureTypeDetail;
}
