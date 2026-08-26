import type { LoginResponse, User } from "@/lib/api/types";

const TOKEN_KEY = "pilo.accessToken";
const USER_KEY = "pilo.user";

export const SESSION_CHANGED_EVENT = "pilo:session-changed";

function notifySessionChange() {
  window.dispatchEvent(new Event(SESSION_CHANGED_EVENT));
}

export function persistSession(response: LoginResponse) {
  window.sessionStorage.setItem(TOKEN_KEY, response.accessToken);
  window.sessionStorage.setItem(USER_KEY, JSON.stringify(response.user));
  notifySessionChange();
}

export function updateStoredUser(user: User) {
  window.sessionStorage.setItem(USER_KEY, JSON.stringify(user));
  notifySessionChange();
}

export function clearSession() {
  window.sessionStorage.removeItem(TOKEN_KEY);
  window.sessionStorage.removeItem(USER_KEY);
  notifySessionChange();
}

export function getAccessToken() {
  if (typeof window === "undefined") {
    return null;
  }
  return window.sessionStorage.getItem(TOKEN_KEY);
}

/** Raw JSON string so it can be used directly as a useSyncExternalStore snapshot. */
export function readStoredUserRaw(): string | null {
  if (typeof window === "undefined") {
    return null;
  }
  return window.sessionStorage.getItem(USER_KEY);
}
