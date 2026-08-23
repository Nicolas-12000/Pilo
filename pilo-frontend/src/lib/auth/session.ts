import type { LoginResponse, User } from "@/lib/api/types";

const TOKEN_KEY = "pilo.accessToken";
const USER_KEY = "pilo.user";

export function persistSession(response: LoginResponse) {
  window.sessionStorage.setItem(TOKEN_KEY, response.accessToken);
  window.sessionStorage.setItem(USER_KEY, JSON.stringify(response.user));
}

export function clearSession() {
  window.sessionStorage.removeItem(TOKEN_KEY);
  window.sessionStorage.removeItem(USER_KEY);
}

export function getAccessToken() {
  return window.sessionStorage.getItem(TOKEN_KEY);
}

export function getStoredUser(): User | null {
  const raw = window.sessionStorage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as User;
  } catch {
    return null;
  }
}

export function isAuthenticated() {
  return Boolean(getAccessToken() && getStoredUser());
}

export function readStoredUser(): User | null {
  if (typeof window === "undefined") {
    return null;
  }
  return getStoredUser();
}

export function readIsAuthenticated(): boolean {
  if (typeof window === "undefined") {
    return false;
  }
  return isAuthenticated();
}
