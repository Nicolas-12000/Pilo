"use client";

import { Moon, Sun } from "lucide-react";
import { useTheme } from "next-themes";
import { Button } from "@/components/ui/button";
import { useIsHydrated } from "@/lib/utils/use-is-hydrated";

export function ThemeToggle() {
  const { resolvedTheme, setTheme } = useTheme();
  const isHydrated = useIsHydrated();
  const isDark = isHydrated && resolvedTheme === "dark";

  return (
    <Button
      variant="secondary"
      size="icon"
      aria-label={isDark ? "Activar tema claro" : "Activar tema oscuro"}
      onClick={() => setTheme(isDark ? "light" : "dark")}
      className="bg-surface-container ring-0 hover:bg-surface-container-high"
    >
      {isDark ? <Moon size={20} strokeWidth={1.75} /> : <Sun size={20} strokeWidth={1.75} />}
    </Button>
  );
}
