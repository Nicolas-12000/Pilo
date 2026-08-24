import { FileText } from "lucide-react";
import { StatusBadge } from "@/components/pilo/status-badge";
import { formatFileSize } from "@/lib/cases/dates";
import { documentStatus } from "@/lib/cases/status";
import type { DocumentRecord } from "@/lib/api/types";

export function DocumentRow({ document }: { document: DocumentRecord }) {
  return (
    <li className="flex items-center gap-sm rounded-sm bg-surface-container-low p-sm">
      <FileText size={20} strokeWidth={1.75} className="shrink-0 text-on-surface-variant" />
      <div className="min-w-0 flex-1">
        <p className="truncate font-title-sm text-on-surface">{document.fileName}</p>
        <p className="mt-0.5 text-body-sm text-on-surface-variant">
          {formatFileSize(document.fileSize)}
        </p>
      </div>
      <StatusBadge status={documentStatus[document.status]} />
    </li>
  );
}
