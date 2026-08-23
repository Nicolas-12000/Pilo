import { Suspense } from "react";
import { LoginPanel } from "@/features/auth/login-panel";

export default function LoginPage() {
  return (
    <Suspense fallback={<div className="flex flex-1 items-center justify-center px-4">Cargando…</div>}>
      <LoginPanel />
    </Suspense>
  );
}
