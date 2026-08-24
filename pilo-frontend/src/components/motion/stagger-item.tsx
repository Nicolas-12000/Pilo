import type { CSSProperties, ReactNode } from "react";
import { cn } from "@/lib/utils/cn";

type StaggerItemProps = {
  index: number;
  children: ReactNode;
  className?: string;
};

/** Staggered entrance — 40ms steps, capped at 200ms per DESING.MD motion policy. */
export function StaggerItem({ index, children, className }: StaggerItemProps) {
  const style = {
    "--stagger": Math.min(index, 5),
  } as CSSProperties;

  return (
    <div className={cn("animate-enter-stagger", className)} style={style}>
      {children}
    </div>
  );
}
