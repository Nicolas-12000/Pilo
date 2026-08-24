import Link from "next/link";
import { PiloMark } from "@/components/layout/pilo-mark";
import { cn } from "@/lib/utils/cn";

export function Brand({ className }: { className?: string }) {
  return (
    <Link
      href="/"
      className={cn("inline-flex items-center gap-2.5 text-on-surface", className)}
      aria-label="PILO, inicio"
    >
      <PiloMark />
      <span className="flex flex-col leading-none">
        <span className="font-title-md tracking-tight">PILO</span>
        <span className="mt-0.5 font-caps text-secondary">Trámites</span>
      </span>
    </Link>
  );
}
