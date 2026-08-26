import type { LoginValues } from "@/lib/auth/login-schema";
import { authFetch, readApiError } from "@/lib/api/client";
import { getApiUrl } from "@/lib/api/config";
import type { ApiErrorBody, LoginResponse, User } from "./types";

const API_URL = getApiUrl();

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

export async function login(values: LoginValues): Promise<LoginResponse> {
  const response = await fetch(`${API_URL}/api/v1/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(values),
  });

  if (!response.ok) {
    const body = (await response.json().catch(() => null)) as ApiErrorBody | null;
    throw new ApiError(
      body?.message ?? "No se ha podido iniciar sesión.",
      response.status,
      body?.error,
    );
  }

  return (await response.json()) as LoginResponse;
}

export async function fetchCurrentUser(): Promise<User> {
  const response = await authFetch(`${API_URL}/api/v1/auth/me`);

  if (!response.ok) {
    await readApiError(response);
  }

  return (await response.json()) as User;
}

export async function updateProfile(fullName: string): Promise<User> {
  const response = await authFetch(`${API_URL}/api/v1/auth/me`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ fullName }),
  });

  if (!response.ok) {
    await readApiError(response);
  }

  return (await response.json()) as User;
}

export async function changePassword(currentPassword: string, newPassword: string): Promise<void> {
  const response = await authFetch(`${API_URL}/api/v1/auth/me/password`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ currentPassword, newPassword }),
  });

  if (!response.ok) {
    await readApiError(response);
  }
}
