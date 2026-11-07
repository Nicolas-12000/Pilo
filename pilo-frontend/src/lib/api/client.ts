import { clearSession, getAccessToken } from "@/lib/auth/session";
import type { ApiErrorBody } from "@/lib/api/types";
import { routes } from "@/lib/routes";

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

/** Expired, revoked or disabled sessions: drop the stale token and send the user back to login. */
function handleRejectedToken() {
  if (typeof window === "undefined") {
    return;
  }
  clearSession();
  const { pathname, search } = window.location;
  if (pathname !== routes.login) {
    window.location.assign(routes.loginNext(pathname + search));
  }
}

export async function authFetch(input: string, init?: RequestInit): Promise<Response> {
  const hadToken = getAccessToken() != null;
  const response = await fetch(input, { ...init, headers: authHeaders(init?.headers) });
  if (response.status === 401 && hadToken) {
    handleRejectedToken();
  }
  return response;
}
