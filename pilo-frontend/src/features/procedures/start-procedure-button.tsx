"use client";

import Link from "next/link";
import { useState } from "react";
import { readIsAuthenticated } from "@/lib/auth/session";

type StartProcedureButtonProps = {
  procedureId: string;
  procedureTitle: string;
};

export function StartProcedureButton({
  procedureId,
  procedureTitle,
}: StartProcedureButtonProps) {
  const [authenticated] = useState(readIsAuthenticated);
  const [confirmed, setConfirmed] = useState(false);
  const loginHref = `/login?next=${encodeURIComponent(`/tramites/${procedureId}`)}`;

  if (!authenticated) {
    return (
      <div className="rounded-lg bg-primary-container px-4 py-4">
        <p className="text-body-md text-on-primary-container">
          Para iniciar <strong>{procedureTitle}</strong> necesitas una cuenta.
        </p>
        <Link
          href={loginHref}
          className="mt-4 inline-flex h-12 items-center rounded-md bg-primary px-5 font-label text-on-primary hover:bg-primary-hover"
        >
          Iniciar sesión para continuar
        </Link>
      </div>
    );
  }

  if (confirmed) {
    return (
      <div className="rounded-lg bg-success-container px-4 py-4 text-body-md text-on-success-container">
        Tu sesión está activa. La creación del expediente se conectará en el siguiente paso
        del proyecto.
      </div>
    );
  }

  return (
    <div className="rounded-lg bg-surface-container px-4 py-4">
      <p className="text-body-md text-on-surface">
        Ya puedes iniciar este trámite con tu cuenta.
      </p>
      <button
        type="button"
        onClick={() => setConfirmed(true)}
        className="mt-4 inline-flex h-12 items-center rounded-md bg-primary px-5 font-label text-on-primary hover:bg-primary-hover"
      >
        Iniciar trámite
      </button>
    </div>
  );
}
