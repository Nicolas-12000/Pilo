import type { ComponentProps } from "react";
import { cn } from "@/lib/utils/cn";

export function Card({ className, ...props }: ComponentProps<"div">) {
  return (
    <div
      className={cn("elevation-card rounded-lg p-md md:p-lg", className)}
      {...props}
    />
  );
}

export function CardHeader({ className, ...props }: ComponentProps<"div">) {
  return <div className={cn("flex flex-wrap items-start justify-between gap-3", className)} {...props} />;
}

export function CardTitle({ className, ...props }: ComponentProps<"h2">) {
  return <h2 className={cn("font-title-md text-on-surface", className)} {...props} />;
}

export function CardDescription({ className, ...props }: ComponentProps<"p">) {
  return <p className={cn("text-body-sm text-on-surface-variant", className)} {...props} />;
}

export function Section({ className, ...props }: ComponentProps<"section">) {
  return <section className={cn("mt-xl", className)} {...props} />;
}

export function SectionHeading({ className, ...props }: ComponentProps<"h2">) {
  return <h2 className={cn("font-headline-md text-on-surface", className)} {...props} />;
}

export function Divider({ className, ...props }: ComponentProps<"hr">) {
  return <hr className={cn("border-0 border-t border-outline-variant", className)} {...props} />;
}
