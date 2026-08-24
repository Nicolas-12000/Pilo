"use client";

import { useCallback, useMemo, useSyncExternalStore } from "react";
import type { User } from "@/lib/api/types";
import {
  SESSION_CHANGED_EVENT,
  clearSession,
  readStoredUserRaw,
} from "@/lib/auth/session";
import { useIsHydrated } from "@/lib/utils/use-is-hydrated";

export type SessionStatus = "loading" | "authenticated" | "anonymous";

function subscribe(onChange: () => void) {
  window.addEventListener(SESSION_CHANGED_EVENT, onChange);
  window.addEventListener("storage", onChange);
  return () => {
    window.removeEventListener(SESSION_CHANGED_EVENT, onChange);
    window.removeEventListener("storage", onChange);
  };
}

const serverSnapshot = () => null;

/**
 * Subscribes to the stored session. The raw JSON string is the snapshot so it
 * stays referentially stable between renders, and it is parsed on change only.
 */
export function useSession() {
  const raw = useSyncExternalStore(subscribe, readStoredUserRaw, serverSnapshot);
  const isHydrated = useIsHydrated();

  const user = useMemo<User | null>(() => {
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as User;
    } catch {
      return null;
    }
  }, [raw]);

  const status: SessionStatus = !isHydrated
    ? "loading"
    : user
      ? "authenticated"
      : "anonymous";

  const signOut = useCallback(() => {
    clearSession();
  }, []);

  return { user, status, signOut };
}
