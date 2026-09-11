import { AlertTriangle, FileText, Sparkles } from "lucide-react";
import { StatusBadge } from "@/components/pilo/status-badge";
import { formatFileSize } from "@/lib/cases/dates";
import {
  extractionFieldLabel,
  orderedExtractionEntries,
  validationFailureLabel,
} from "@/lib/cases/validation-labels";
import { documentStatus } from "@/lib/cases/status";
import type { DocumentRecord } from "@/lib/api/types";

export function DocumentRow({ document }: { document: DocumentRecord }) {
  const failures = document.validationFailures ?? [];
  const extraction = document.extraction;
  const extractionEntries = extraction ? orderedExtractionEntries(extraction.fields ?? {}) : [];

  return (
    <li className="rounded-sm bg-surface-container-low p-sm">
      <div className="flex items-start gap-sm">
        <FileText size={20} strokeWidth={1.75} className="mt-0.5 shrink-0 text-on-surface-variant" />
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-start justify-between gap-sm">
            <div className="min-w-0">
              <p className="truncate font-title-sm text-on-surface">{document.fileName}</p>
              <p className="mt-0.5 text-body-sm text-on-surface-variant">
                {formatFileSize(document.fileSize)}
              </p>
            </div>
            <StatusBadge status={documentStatus[document.status]} />
          </div>

          {document.processingFailureReason ? (
            <p className="mt-sm flex items-start gap-1.5 text-body-sm text-danger">
              <AlertTriangle size={16} strokeWidth={1.75} className="mt-0.5 shrink-0" />
              {document.processingFailureReason}
            </p>
          ) : null}

          {failures.length > 0 ? (
            <ul className="mt-sm flex flex-col gap-1 text-body-sm text-danger">
              {failures.map((failure) => (
                <li key={failure} className="flex items-start gap-1.5">
                  <AlertTriangle size={16} strokeWidth={1.75} className="mt-0.5 shrink-0" />
                  {validationFailureLabel(failure)}
                </li>
              ))}
            </ul>
          ) : null}

          {extraction ? (
            <div className="mt-sm rounded-sm bg-surface p-sm ring-1 ring-outline-variant">
              <div className="flex items-center gap-1.5 font-label text-on-surface">
                <Sparkles size={16} strokeWidth={1.75} className="text-primary" />
                Datos extraídos por IA
              </div>
              <dl className="mt-sm grid gap-xs text-body-sm">
                <div className="flex flex-wrap justify-between gap-x-md gap-y-0.5">
                  <dt className="text-on-surface-variant">Tipo detectado</dt>
                  <dd className="font-code text-on-surface">{extraction.documentTypeDetected}</dd>
                </div>
                <div className="flex flex-wrap justify-between gap-x-md gap-y-0.5">
                  <dt className="text-on-surface-variant">Confianza</dt>
                  <dd className="font-code text-on-surface">
                    {Math.round(extraction.confidence * 100)}%
                  </dd>
                </div>
                {extractionEntries.map(([key, value]) => (
                  <div key={key} className="flex flex-wrap justify-between gap-x-md gap-y-0.5">
                    <dt className="text-on-surface-variant">{extractionFieldLabel(key)}</dt>
                    <dd className="text-right text-on-surface">{value}</dd>
                  </div>
                ))}
              </dl>
            </div>
          ) : null}
        </div>
      </div>
    </li>
  );
}
