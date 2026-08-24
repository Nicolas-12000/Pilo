import type { ReactNode } from "react";
import { cn } from "@/lib/utils/cn";

export function PageEnter({ children, className }: { children: ReactNode; className?: string }) {
  return <div className={cn("min-h-0", className)}>{children}</div>;
}
