import { FileText } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { EmptyState } from "@/components/ui/empty-state";
import type { Requirement } from "@/lib/api/types";

export function RequirementList({ requirements }: { requirements: Requirement[] }) {
  if (requirements.length === 0) {
    return (
      <EmptyState
        icon={FileText}
        title="Sin requisitos configurados"
        description="Este trámite todavía no tiene requisitos definidos."
      />
    );
  }

  return (
    <ul className="flex flex-col gap-xs">
      {requirements.map((requirement) => (
        <li
          key={requirement.id}
          className="flex items-start gap-sm rounded-sm bg-surface-container-low p-sm"
        >
          <FileText
            size={20}
            strokeWidth={1.75}
            className="mt-0.5 shrink-0 text-on-surface-variant"
          />
          <div className="min-w-0 flex-1">
            <p className="font-title-sm text-on-surface">{requirement.name}</p>
            <p className="mt-1 text-body-sm text-on-surface-variant">{requirement.description}</p>
          </div>
          {requirement.mandatory ? (
            <Badge tone="under-review">Obligatorio</Badge>
          ) : (
            <Badge tone="external">Opcional</Badge>
          )}
        </li>
      ))}
    </ul>
  );
}
