import { cn } from "@/lib/utils/cn";

type ProgressProps = {
  value: number;
  label?: string;
  className?: string;
};

export function Progress({ value, label, className }: ProgressProps) {
  const clamped = Math.min(100, Math.max(0, Math.round(value)));

  return (
    <div className={cn("w-full", className)}>
      <div
        role="progressbar"
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={clamped}
        aria-label={label ?? "Progreso del expediente"}
        className="h-2 w-full overflow-hidden rounded-full bg-surface-container-high"
      >
        <div
          className="h-full rounded-full bg-primary transition-[width] duration-content"
          style={{ width: `${clamped}%` }}
        />
      </div>
    </div>
  );
}
