import { History } from "lucide-react";
import { EmptyState } from "@/components/ui/empty-state";
import { formatDateTime } from "@/lib/cases/dates";
import { auditActionLabel } from "@/lib/cases/status";
import type { AuditEvent } from "@/lib/api/types";
import { cn } from "@/lib/utils/cn";

export function AuditTimeline({ events }: { events: AuditEvent[] }) {
  if (events.length === 0) {
    return (
      <EmptyState
        icon={History}
        title="Sin actividad registrada"
        description="Cada acción sobre el expediente quedará registrada aquí."
      />
    );
  }

  return (
    <ul className="flex flex-col gap-xs">
      {events.map((event) => (
        <li
          key={event.id}
          className="flex items-start gap-sm rounded-sm bg-surface-container-low p-sm"
        >
          <span
            aria-hidden
            className={cn(
              "mt-1.5 size-2 shrink-0 rounded-full",
              event.result === "SUCCESS" ? "bg-success" : "bg-danger",
            )}
          />
          <div className="min-w-0 flex-1">
            <p className="font-title-sm text-on-surface">{auditActionLabel(event.action)}</p>
            <p className="mt-0.5 text-body-sm text-on-surface-variant">
              {formatDateTime(event.timestamp)}
            </p>
          </div>
        </li>
      ))}
    </ul>
  );
}
