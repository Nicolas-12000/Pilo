"use client";

import { Moon, Sun } from "lucide-react";
import { useTheme } from "next-themes";
import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils/cn";
import { useIsHydrated } from "@/lib/utils/use-is-hydrated";

export function ThemeToggle({ className }: { className?: string }) {
  const { resolvedTheme, setTheme } = useTheme();
  const isHydrated = useIsHydrated();
  const isDark = isHydrated && resolvedTheme === "dark";
  const [iconKey, setIconKey] = useState(0);

  useEffect(() => {
    if (isHydrated) {
      setIconKey((key) => key + 1);
    }
  }, [isDark, isHydrated]);

  return (
    <Button
      variant="secondary"
      size="icon"
      aria-label={isDark ? "Activar tema claro" : "Activar tema oscuro"}
      onClick={() => setTheme(isDark ? "light" : "dark")}
      className={cn(
        "relative overflow-hidden ring-0 transition-[background-color,box-shadow] duration-content",
        isDark
          ? "bg-surface-container-high hover:bg-surface-container-highest"
          : "bg-primary-container/60 hover:bg-primary-container",
        className,
      )}
    >
      <span
        key={iconKey}
        className="grid place-items-center animate-theme-icon"
        aria-hidden
      >
        {isDark ? (
          <Moon size={20} strokeWidth={1.75} className="text-on-surface" />
        ) : (
          <Sun size={20} strokeWidth={1.75} className="text-primary" />
        )}
      </span>
    </Button>
  );
}
