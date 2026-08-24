import type { ComponentProps } from "react";
import { cn } from "@/lib/utils/cn";

export function PageContainer({ className, ...props }: ComponentProps<"main">) {
  return (
    <main
      className={cn("mx-auto w-full max-w-240 px-md py-xl lg:px-xl lg:py-xxl", className)}
      {...props}
    />
  );
}
