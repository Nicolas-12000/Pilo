"use client";

import { Loader2, LogIn, Sparkles } from "lucide-react";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { toast } from "sonner";
import { Button, ButtonLink } from "@/components/ui/button";
import { createCase } from "@/lib/api/cases";
import { ApiError } from "@/lib/api/client";
import { useSession } from "@/lib/auth/use-session";
import { routes } from "@/lib/routes";

type StartProcedureButtonProps = {
  procedureId: string;
  procedureTitle: string;
};

export function StartProcedureButton({
  procedureId,
  procedureTitle,
}: StartProcedureButtonProps) {
  const router = useRouter();
  const { status } = useSession();
  const [loading, setLoading] = useState(false);

  if (status === "loading") {
    return <div className="h-28 animate-pulse rounded-lg bg-surface-container" />;
  }

  if (status === "anonymous") {
    return (
      <div className="rounded-lg bg-primary-container p-md md:p-lg">
        <p className="text-body-md text-on-primary-container">
          Para iniciar <strong>{procedureTitle}</strong> necesitas una cuenta.
        </p>
        <ButtonLink
          href={routes.loginNext(routes.procedure(procedureId))}
          className="mt-md"
        >
          <LogIn size={20} strokeWidth={1.75} />
          Iniciar sesión para continuar
        </ButtonLink>
      </div>
    );
  }

  async function handleStart() {
    setLoading(true);
    try {
      const created = await createCase(procedureId);
      toast.success("Expediente creado", { description: created.caseNumber });
      router.push(routes.case(created.id));
    } catch (cause) {
      toast.error(
        cause instanceof ApiError
          ? cause.message
          : "No se ha podido crear el expediente. Inténtalo de nuevo.",
      );
      setLoading(false);
    }
  }

  return (
    <div className="rounded-lg bg-surface-container p-md md:p-lg">
      <p className="text-body-md text-on-surface">
        Se creará un expediente con los requisitos de este trámite y podrás empezar a subir
        documentos de inmediato.
      </p>
      <Button onClick={handleStart} disabled={loading} className="mt-md">
        {loading ? (
          <Loader2 size={20} strokeWidth={1.75} className="animate-spin" />
        ) : (
          <Sparkles size={20} strokeWidth={1.75} />
        )}
        {loading ? "Creando expediente…" : "Iniciar trámite"}
      </Button>
    </div>
  );
}
