import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import type { ReactNode } from "react";

type PageHeaderProps = {
  eyebrow?: string;
  title: string;
  description?: string;
  actions?: ReactNode;
  backHref?: string;
  backLabel?: string;
};

export function PageHeader({
  eyebrow,
  title,
  description,
  actions,
  backHref,
  backLabel = "Volver",
}: PageHeaderProps) {
  return (
    <header>
      {backHref ? (
        <Link
          href={backHref}
          className="inline-flex items-center gap-1.5 font-label text-primary-hover transition-colors duration-feedback hover:text-primary"
        >
          <ArrowLeft size={16} strokeWidth={1.75} />
          {backLabel}
        </Link>
      ) : null}

      <div className={`flex flex-wrap items-end justify-between gap-md ${backHref ? "mt-lg" : ""}`}>
        <div className="min-w-0">
          {eyebrow ? <p className="font-caps text-secondary">{eyebrow}</p> : null}
          <h1 className="mt-2 font-headline-lg text-on-surface md:font-display">{title}</h1>
          {description ? (
            <p className="mt-sm max-w-2xl text-body-lg text-on-surface-variant">{description}</p>
          ) : null}
        </div>
        {actions ? <div className="flex shrink-0 gap-sm">{actions}</div> : null}
      </div>
    </header>
  );
}
