"use client";

import { Download, Eye, Loader2, X } from "lucide-react";
import { useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { fetchDocumentContent, isPreviewableMime } from "@/lib/api/documents";
import { ApiError } from "@/lib/api/client";

type DocumentPreviewDialogProps = {
  caseId: string;
  documentId: string;
  fileName: string;
  mimeType: string;
  onClose: () => void;
};

export function DocumentPreviewDialog({
  caseId,
  documentId,
  fileName,
  mimeType,
  onClose,
}: DocumentPreviewDialogProps) {
  const [objectUrl, setObjectUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        onClose();
      }
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [onClose]);

  useEffect(() => {
    let revoked = false;
    let url: string | null = null;

    fetchDocumentContent(caseId, documentId)
      .then((blob) => {
        if (revoked) {
          return;
        }
        url = URL.createObjectURL(blob);
        setObjectUrl(url);
      })
      .catch((cause) => {
        toast.error(
          cause instanceof ApiError ? cause.message : "No se ha podido abrir el documento.",
        );
        onClose();
      })
      .finally(() => {
        if (!revoked) {
          setLoading(false);
        }
      });

    return () => {
      revoked = true;
      if (url) {
        URL.revokeObjectURL(url);
      }
    };
  }, [caseId, documentId, onClose]);

  async function handleDownload() {
    try {
      const blob = await fetchDocumentContent(caseId, documentId, true);
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = fileName;
      link.click();
      URL.revokeObjectURL(url);
    } catch (cause) {
      toast.error(
        cause instanceof ApiError ? cause.message : "No se ha podido descargar el documento.",
      );
    }
  }

  const previewable = isPreviewableMime(mimeType);

  return createPortal(
    <div
        className="fixed inset-0 z-50 flex items-center justify-center bg-(--pilo-overlay) p-md"
      role="presentation"
      onClick={onClose}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="document-preview-title"
        className="flex max-h-[92vh] w-full max-w-4xl flex-col overflow-hidden rounded-lg bg-surface p-lg shadow-overlay"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="flex items-start justify-between gap-sm">
          <h2 id="document-preview-title" className="min-w-0 truncate font-title-sm text-on-surface">
            {fileName}
          </h2>
          <Button variant="ghost" size="icon" onClick={onClose} aria-label="Cerrar vista previa">
            <X size={20} strokeWidth={1.75} />
          </Button>
        </div>

        <div className="mt-md min-h-80 flex-1 overflow-hidden rounded-md bg-surface-container-low">
          {loading ? (
            <div className="flex h-80 items-center justify-center gap-sm text-on-surface-variant">
              <Loader2 size={20} strokeWidth={1.75} className="animate-spin" />
              Cargando documento…
            </div>
          ) : objectUrl && mimeType.startsWith("image/") ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img src={objectUrl} alt={fileName} className="mx-auto max-h-[70vh] object-contain" />
          ) : objectUrl && mimeType === "application/pdf" ? (
            <iframe
              title={fileName}
              src={objectUrl}
              className="h-[70vh] w-full border-0"
            />
          ) : (
            <p className="p-lg text-body-sm text-on-surface-variant">
              Este archivo no se puede previsualizar en el navegador. Descárgalo para revisarlo.
            </p>
          )}
        </div>

        <div className="mt-md flex flex-wrap items-center justify-end gap-sm">
          {!previewable ? (
            <p className="mr-auto text-body-sm text-on-surface-variant">
              Descárgalo para abrirlo en tu dispositivo.
            </p>
          ) : null}
          <Button variant="secondary" onClick={() => void handleDownload()}>
            <Download size={18} strokeWidth={1.75} />
            Descargar
          </Button>
        </div>
      </div>
    </div>,
    document.body,
  );
}

export function useDocumentFileActions(caseId: string, documentId: string, fileName: string) {
  const [previewOpen, setPreviewOpen] = useState(false);

  async function download() {
    try {
      const blob = await fetchDocumentContent(caseId, documentId, true);
      const url = URL.createObjectURL(blob);
      const link = window.document.createElement("a");
      link.href = url;
      link.download = fileName;
      link.click();
      URL.revokeObjectURL(url);
    } catch (cause) {
      toast.error(
        cause instanceof ApiError ? cause.message : "No se ha podido descargar el documento.",
      );
    }
  }

  return {
    previewOpen,
    openPreview: () => setPreviewOpen(true),
    closePreview: () => setPreviewOpen(false),
    download,
  };
}

export function DocumentFileActions({
  caseId,
  documentId,
  fileName,
  mimeType,
  disabled,
}: {
  caseId: string;
  documentId: string;
  fileName: string;
  mimeType: string;
  disabled?: boolean;
}) {
  const actions = useDocumentFileActions(caseId, documentId, fileName);

  return (
    <>
      <div className="flex shrink-0 gap-1">
        <Button
          variant="ghost"
          size="icon"
          className="size-10"
          disabled={disabled}
          onClick={actions.openPreview}
          aria-label={`Previsualizar ${fileName}`}
        >
          <Eye size={18} strokeWidth={1.75} />
        </Button>
        <Button
          variant="ghost"
          size="icon"
          className="size-10"
          disabled={disabled}
          onClick={() => void actions.download()}
          aria-label={`Descargar ${fileName}`}
        >
          <Download size={18} strokeWidth={1.75} />
        </Button>
      </div>
      {actions.previewOpen ? (
        <DocumentPreviewDialog
          caseId={caseId}
          documentId={documentId}
          fileName={fileName}
          mimeType={mimeType}
          onClose={actions.closePreview}
        />
      ) : null}
    </>
  );
}
