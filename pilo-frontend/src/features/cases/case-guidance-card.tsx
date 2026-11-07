"use client";

import { Sparkles } from "lucide-react";
import { Card, CardDescription, CardTitle } from "@/components/ui/card";
import { cn } from "@/lib/utils/cn";

type CaseGuidanceCardProps = {
  title: string;
  body: string;
  variant?: "default" | "reviewer";
  className?: string;
};

export function CaseGuidanceCard({
  title,
  body,
  variant = "default",
  className,
}: CaseGuidanceCardProps) {
  if (!body.trim()) {
    return null;
  }

  return (
    <Card
      className={cn(
        "border-outline-variant",
        variant === "reviewer" ? "bg-surface-container-high" : "bg-surface-container",
        className,
      )}
    >
      <div className="flex items-start gap-sm">
        <Sparkles
          size={20}
          strokeWidth={1.75}
          className="mt-0.5 shrink-0 text-primary"
          aria-hidden
        />
        <div className="min-w-0">
          <CardTitle className="text-title-sm">{title}</CardTitle>
          <CardDescription className="mt-sm whitespace-pre-wrap text-body-sm text-on-surface">
            {body}
          </CardDescription>
        </div>
      </div>
    </Card>
  );
}
