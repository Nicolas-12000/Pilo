import { Lightbulb } from "lucide-react";
import type { DocumentRecord } from "@/lib/api/types";

type DocumentGuidanceProps = {
  document: DocumentRecord;
  audience: "citizen" | "reviewer";
};

export function DocumentGuidance({ document, audience }: DocumentGuidanceProps) {
  const text =
    audience === "reviewer"
      ? document.extraction?.reviewerSummary
      : document.extraction?.citizenGuidance;

  if (!text?.trim()) {
    if (document.status === "PROCESSING_FAILED" && audience === "citizen") {
      return (
        <GuidanceBox title="Qué puedes hacer">
          No hemos podido leer el archivo. Prueba con otro PDF o una foto más nítida, o elimina este archivo
          y vuelve a subirlo.
        </GuidanceBox>
      );
    }
    return null;
  }

  const title =
    audience === "reviewer"
      ? "Síntesis para revisión"
      : document.status === "REJECTED"
        ? "Qué puedes hacer"
        : "Comprueba antes de continuar";

  return <GuidanceBox title={title}>{text}</GuidanceBox>;
}

function GuidanceBox({ title, children }: { title: string; children: string }) {
  return (
    <div className="mt-sm rounded-sm bg-primary-container p-sm text-on-primary-container">
      <div className="flex items-start gap-1.5 font-label">
        <Lightbulb size={16} strokeWidth={1.75} className="mt-0.5 shrink-0" aria-hidden />
        {title}
      </div>
      <p className="mt-1 text-body-sm whitespace-pre-wrap">{children}</p>
    </div>
  );
}
