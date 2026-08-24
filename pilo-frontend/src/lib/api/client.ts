import { getAccessToken } from "@/lib/auth/session";
import type { ApiErrorBody } from "@/lib/api/types";

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

export async function readApiError(response: Response): Promise<never> {
  const body = (await response.json().catch(() => null)) as ApiErrorBody | null;
  throw new ApiError(
    body?.message ?? "No se ha podido completar la petición.",
    response.status,
    body?.error,
  );
}

export function authHeaders(extra?: HeadersInit): HeadersInit {
  const token = getAccessToken();
  return {
    ...(extra ?? {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

export async function authFetch(input: string, init?: RequestInit): Promise<Response> {
  const headers = authHeaders(init?.headers);
  return fetch(input, { ...init, headers });
}
