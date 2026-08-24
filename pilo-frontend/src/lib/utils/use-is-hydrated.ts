"use client";

import { useSyncExternalStore } from "react";

const noopSubscribe = () => () => {};
const clientSnapshot = () => true;
const serverSnapshot = () => false;

/**
 * False while rendering on the server and during hydration, true afterwards.
 * Lets client-only values (session, theme) render without a hydration mismatch
 * and without a state update inside an effect.
 */
export function useIsHydrated() {
  return useSyncExternalStore(noopSubscribe, clientSnapshot, serverSnapshot);
}
