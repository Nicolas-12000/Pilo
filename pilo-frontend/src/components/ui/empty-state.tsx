import type { LucideIcon } from "lucide-react";
import type { ReactNode } from "react";
import { cn } from "@/lib/utils/cn";

type EmptyStateProps = {
  icon: LucideIcon;
  title: string;
  description?: string;
  action?: ReactNode;
  tone?: "neutral" | "danger";
  className?: string;
};

export function EmptyState({
  icon: Icon,
  title,
  description,
  action,
  tone = "neutral",
  className,
}: EmptyStateProps) {
  return (
    <div
      className={cn(
        "flex flex-col items-center rounded-lg px-md py-xxl text-center",
        tone === "danger"
          ? "bg-danger-container text-on-danger-container"
          : "text-on-surface-variant",
        className,
      )}
    >
      <Icon
        size={24}
        strokeWidth={1.75}
        className={tone === "danger" ? "text-on-danger-container" : "text-outline"}
      />
      <p
        className={cn(
          "mt-md font-title-md",
          tone === "danger" ? "text-on-danger-container" : "text-on-surface",
        )}
      >
        {title}
      </p>
      {description ? <p className="mt-xs max-w-md text-body-sm">{description}</p> : null}
      {action ? <div className="mt-lg">{action}</div> : null}
    </div>
  );
}
