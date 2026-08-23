import type { LoginValues } from "@/lib/auth/login-schema";
import type { ApiErrorBody, LoginResponse } from "./types";

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

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
