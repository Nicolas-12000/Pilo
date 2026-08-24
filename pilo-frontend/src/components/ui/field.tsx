import type { ComponentProps } from "react";
import { cn } from "@/lib/utils/cn";

export function Input({ className, ...props }: ComponentProps<"input">) {
  return (
    <input
      className={cn(
        "h-12 w-full rounded-sm bg-surface px-3.5 text-body-md text-on-surface ring-1 ring-outline-variant outline-none transition-shadow duration-feedback placeholder:text-on-surface-variant/70 focus:ring-2 focus:ring-primary",
        "aria-invalid:bg-danger-container aria-invalid:text-on-danger-container aria-invalid:ring-danger",
        className,
      )}
      {...props}
    />
  );
}

export function Label({ className, ...props }: ComponentProps<"span">) {
  return <span className={cn("font-label text-on-surface", className)} {...props} />;
}

export function FieldError({ className, ...props }: ComponentProps<"span">) {
  return <span className={cn("text-body-sm text-danger", className)} {...props} />;
}
