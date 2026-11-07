import { cn } from "@/lib/utils/cn";

type PiloMarkProps = {
  className?: string;
};

/**
 * Brand mark: a "P" drawn as one continuous route that closes on a stamp dot,
 * the moment a paperwork trail ends in a resolution. Keep in sync with app/icon.svg.
 */
export function PiloMark({ className }: PiloMarkProps) {
  return (
    <svg
      viewBox="0 0 36 36"
      fill="none"
      aria-hidden
      className={cn("size-9 shrink-0", className)}
    >
      <rect width="36" height="36" rx="9" className="fill-primary" />
      <path
        d="M12.5 27V10.5h6.5a5.75 5.75 0 0 1 0 11.5h-6.5"
        className="stroke-on-primary"
        strokeWidth="3.4"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <circle cx="25" cy="26.2" r="2.2" className="fill-on-primary/70" />
    </svg>
  );
}
