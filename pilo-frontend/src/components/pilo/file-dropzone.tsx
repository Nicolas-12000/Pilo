"use client";

import { Loader2, UploadCloud } from "lucide-react";
import { useDropzone } from "react-dropzone";
import { cn } from "@/lib/utils/cn";

export const MAX_UPLOAD_BYTES = 10 * 1024 * 1024;

const ACCEPTED_TYPES = {
  "application/pdf": [".pdf"],
  "image/jpeg": [".jpg", ".jpeg"],
  "image/png": [".png"],
  "image/webp": [".webp"],
};

type FileDropzoneProps = {
  onFileSelected: (file: File) => void;
  onRejected?: (message: string) => void;
  uploading?: boolean;
  disabled?: boolean;
  className?: string;
};

export function FileDropzone({
  onFileSelected,
  onRejected,
  uploading = false,
  disabled = false,
  className,
}: FileDropzoneProps) {
  const { getRootProps, getInputProps, isDragActive, isDragReject } = useDropzone({
    accept: ACCEPTED_TYPES,
    maxSize: MAX_UPLOAD_BYTES,
    multiple: false,
    disabled: disabled || uploading,
    onDrop: (accepted, rejections) => {
      if (rejections.length > 0) {
        const tooLarge = rejections[0].errors.some((error) => error.code === "file-too-large");
        onRejected?.(
          tooLarge
            ? "El archivo supera los 10 MB permitidos."
            : "Formato no admitido. Usa PDF, JPG, PNG o WEBP.",
        );
        return;
      }
      const file = accepted[0];
      if (file) {
        onFileSelected(file);
      }
    },
  });

  return (
    <div
      {...getRootProps()}
      className={cn(
        "flex min-h-24 cursor-pointer flex-col items-center justify-center gap-2 rounded-md border border-dashed p-lg text-center transition-colors duration-feedback",
        isDragReject
          ? "border-danger bg-danger-container text-on-danger-container"
          : isDragActive
            ? "border-primary bg-primary-container text-on-primary-container"
            : "border-outline-variant bg-surface-container-low text-on-surface-variant hover:border-outline",
        (disabled || uploading) && "cursor-not-allowed opacity-70",
        className,
      )}
    >
      <input {...getInputProps()} />
      {uploading ? (
        <>
          <Loader2 size={24} strokeWidth={1.75} className="animate-spin text-primary" />
          <p className="text-body-sm">Subiendo y analizando con IA…</p>
        </>
      ) : (
        <>
          <UploadCloud size={24} strokeWidth={1.75} />
          <p className="font-label text-on-surface">
            Arrastra el documento o pulsa para seleccionarlo
          </p>
          <p className="text-body-sm">PDF, JPG, PNG o WEBP · máx. 10 MB</p>
        </>
      )}
    </div>
  );
}
