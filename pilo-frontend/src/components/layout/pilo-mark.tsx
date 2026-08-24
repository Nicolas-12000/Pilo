import { cn } from "@/lib/utils/cn";

type PiloMarkProps = {
  className?: string;
};

/**
 * Brand mark: a terracotta seal holding a document and a three-step path.
 * Reads as "we guide official paperwork", not a letter in a box.
 */
export function PiloMark({ className }: PiloMarkProps) {
  return (
    <svg
      viewBox="0 0 36 36"
      fill="none"
      aria-hidden
      className={cn("size-9 shrink-0", className)}
    >
      <rect width="36" height="36" rx="10" className="fill-primary" />
      <path
        d="M11.5 9.5h9.2L25.5 14.3V26a1.5 1.5 0 0 1-1.5 1.5H11.5A1.5 1.5 0 0 1 10 26V11a1.5 1.5 0 0 1 1.5-1.5Z"
        className="fill-on-primary/95"
      />
      <path d="M20.7 9.5V13a1.3 1.3 0 0 0 1.3 1.3h3.5" className="stroke-primary" strokeWidth="1.4" />
      <path
        d="M13.4 18.2h6.4M13.4 21.2h4.6"
        className="stroke-primary/55"
        strokeWidth="1.35"
        strokeLinecap="round"
      />
      <circle cx="14" cy="14.6" r="1.15" className="fill-primary" />
      <circle cx="17.4" cy="14.6" r="1.15" className="fill-primary/70" />
      <circle cx="20.8" cy="14.6" r="1.15" className="fill-primary/40" />
    </svg>
  );
}
