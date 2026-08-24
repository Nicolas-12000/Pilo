"use client";

import { useState } from "react";
import { toast } from "sonner";
import { DocumentRow } from "@/components/pilo/document-row";
import { FileDropzone } from "@/components/pilo/file-dropzone";
import { StatusBadge } from "@/components/pilo/status-badge";
import { Badge } from "@/components/ui/badge";
import { uploadDocument } from "@/lib/api/documents";
import { ApiError } from "@/lib/api/client";
import type { CaseRequirementStatus, DocumentRecord } from "@/lib/api/types";
import { requirementStatus } from "@/lib/cases/status";
import { cn } from "@/lib/utils/cn";

type RequirementItemProps = {
  caseId: string;
  requirement: CaseRequirementStatus;
  documents: DocumentRecord[];
  onUploaded: () => void;
};

const UPLOADABLE_STATUSES = new Set(["PENDING", "REJECTED", "UPLOADED"]);

export function RequirementItem({
  caseId,
  requirement,
  documents,
  onUploaded,
}: RequirementItemProps) {
  const [uploading, setUploading] = useState(false);
  const fulfilled = requirement.fulfillmentStatus === "FULFILLED";
  const canUpload = UPLOADABLE_STATUSES.has(requirement.fulfillmentStatus);

  async function handleUpload(file: File) {
    setUploading(true);
    try {
      await uploadDocument(caseId, requirement.id, file);
      toast.success("Documento subido", {
        description: "La IA está extrayendo los datos del documento.",
      });
      onUploaded();
    } catch (cause) {
      toast.error(
        cause instanceof ApiError ? cause.message : "No se ha podido subir el documento.",
      );
    } finally {
      setUploading(false);
    }
  }

  return (
    <li
      className={cn(
        "rounded-sm p-sm transition-colors duration-content",
        fulfilled ? "bg-success-container" : "bg-surface-container",
      )}
    >
      <div className="flex flex-wrap items-start justify-between gap-sm">
        <div className="min-w-0">
          <div className="flex items-center gap-xs">
            <h3
              className={cn(
                "font-title-sm",
                fulfilled ? "text-on-success-container" : "text-on-surface",
              )}
            >
              {requirement.name}
            </h3>
            {!requirement.mandatory ? <Badge tone="external">Opcional</Badge> : null}
          </div>
          <p
            className={cn(
              "mt-1 text-body-sm",
              fulfilled ? "text-on-success-container/80" : "text-on-surface-variant",
            )}
          >
            {requirement.description}
          </p>
        </div>
        <StatusBadge status={requirementStatus[requirement.fulfillmentStatus]} />
      </div>

      {documents.length > 0 ? (
        <ul className="mt-sm flex flex-col gap-xs">
          {documents.map((document) => (
            <DocumentRow key={document.id} document={document} />
          ))}
        </ul>
      ) : null}

      {canUpload ? (
        <FileDropzone
          className="mt-sm"
          uploading={uploading}
          onFileSelected={(file) => void handleUpload(file)}
          onRejected={(message) => toast.error(message)}
        />
      ) : null}
    </li>
  );
}
