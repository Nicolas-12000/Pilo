import type { Requirement } from "@/lib/api/types";

type RequirementListProps = {
  requirements: Requirement[];
};

export function RequirementList({ requirements }: RequirementListProps) {
  if (requirements.length === 0) {
    return (
      <p className="text-body-md text-on-surface-variant">
        Este trámite aún no tiene requisitos configurados.
      </p>
    );
  }

  return (
    <ul className="flex flex-col gap-3">
      {requirements.map((requirement) => (
        <li
          key={requirement.id}
          className={`rounded-sm px-4 py-3 ${
            requirement.mandatory
              ? "bg-surface-container text-on-surface"
              : "bg-surface-container-low text-on-surface-variant"
          }`}
        >
          <div className="flex items-start justify-between gap-3">
            <div>
              <p className="font-title-sm text-on-surface">{requirement.name}</p>
              <p className="mt-1 text-body-sm text-on-surface-variant">
                {requirement.description}
              </p>
            </div>
            <span
              className={`rounded-full px-2.5 py-1 font-caps ${
                requirement.mandatory
                  ? "bg-warning-container text-on-warning-container"
                  : "bg-surface-container-high text-on-surface-variant"
              }`}
            >
              {requirement.mandatory ? "Obligatorio" : "Opcional"}
            </span>
          </div>
        </li>
      ))}
    </ul>
  );
}
