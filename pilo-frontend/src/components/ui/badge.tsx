import type { LucideIcon } from "lucide-react";
import { cn } from "@/lib/utils/cn";

export type BadgeTone =
  | "pending"
  | "in-progress"
  | "under-review"
  | "approved"
  | "rejected"
  | "external";

const tones: Record<BadgeTone, string> = {
  pending: "bg-surface-container-high text-on-surface-variant",
  "in-progress": "bg-info-container text-on-info-container",
  "under-review": "bg-warning-container text-on-warning-container",
  approved: "bg-success-container text-on-success-container",
  rejected: "bg-danger-container text-on-danger-container",
  external: "bg-secondary-container text-on-secondary-container",
};

type BadgeProps = {
  tone: BadgeTone;
  icon?: LucideIcon;
  spinIcon?: boolean;
  children: React.ReactNode;
  className?: string;
};

export function Badge({ tone, icon: Icon, spinIcon, children, className }: BadgeProps) {
  return (
    <span
      className={cn(
        "inline-flex shrink-0 items-center gap-1.5 px-2.5 py-1 font-caps",
        tone === "external" ? "rounded-sm" : "rounded-full",
        tones[tone],
        className,
      )}
    >
      {Icon ? (
        <Icon size={16} strokeWidth={1.75} className={cn("shrink-0", spinIcon && "animate-spin")} />
      ) : null}
      {children}
    </span>
  );
}
