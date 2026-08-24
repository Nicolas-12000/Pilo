import { Check } from "lucide-react";
import type { WorkflowTask } from "@/lib/api/types";
import { workflowTaskStatus } from "@/lib/cases/status";
import { cn } from "@/lib/utils/cn";

const stepStyles = {
  done: "bg-success text-on-success",
  active: "bg-primary text-on-primary",
  pending: "bg-surface-container-high text-on-surface-variant",
} as const;

function stepStateFor(task: WorkflowTask) {
  if (task.status === "COMPLETED") {
    return "done" as const;
  }
  if (task.status === "IN_PROGRESS") {
    return "active" as const;
  }
  return "pending" as const;
}

export function WorkflowStepper({ tasks }: { tasks: WorkflowTask[] }) {
  return (
    <ol className="flex flex-col gap-0">
      {tasks.map((task, index) => {
        const state = stepStateFor(task);
        const descriptor = workflowTaskStatus[task.status];
        const isLast = index === tasks.length - 1;

        return (
          <li key={task.id} className="flex gap-sm">
            <div className="flex flex-col items-center">
              <span
                aria-hidden
                className={cn(
                  "grid size-7 shrink-0 place-items-center rounded-full font-caps",
                  stepStyles[state],
                )}
              >
                {state === "done" ? <Check size={16} strokeWidth={2.25} /> : index + 1}
              </span>
              {!isLast ? (
                <span
                  aria-hidden
                  className={cn(
                    "min-h-8 w-px flex-1",
                    state === "done" ? "bg-success/40" : "bg-outline-variant",
                  )}
                />
              ) : null}
            </div>

            <div className={cn("min-w-0 flex-1", isLast ? "pb-0" : "pb-lg")}>
              <p
                className={cn(
                  "font-title-sm",
                  state === "pending" ? "text-on-surface-variant" : "text-on-surface",
                )}
              >
                {task.taskName}
              </p>
              <p className="mt-1 inline-flex items-center gap-1.5 text-body-sm text-on-surface-variant">
                <descriptor.icon
                  size={16}
                  strokeWidth={1.75}
                  className={cn("shrink-0", descriptor.spin && "animate-spin")}
                />
                {descriptor.label}
              </p>
            </div>
          </li>
        );
      })}
    </ol>
  );
}
