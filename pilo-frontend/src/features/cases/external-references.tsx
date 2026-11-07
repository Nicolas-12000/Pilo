"use client";

import { ExternalLink, Landmark, Loader2, Search } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";
import {
  compactExternalId,
  externalLinkLabel,
  referenceHeadline,
} from "@/features/cases/external-reference-helpers";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardDescription, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/field";
import { searchBoe, searchDatos, searchSia } from "@/lib/api/integrations";
import type { ExternalReference } from "@/lib/api/types";
import { cn } from "@/lib/utils/cn";

type ExternalReferencesProps = {
  defaultQuery: string;
};

type Results = {
  boe: ExternalReference[];
  datos: ExternalReference[];
  sia: ExternalReference[];
};

export function ExternalReferences({ defaultQuery }: ExternalReferencesProps) {
  const [query, setQuery] = useState(defaultQuery);
  const [loading, setLoading] = useState(false);
  const [results, setResults] = useState<Results | null>(null);

  async function handleSearch(event: React.FormEvent) {
    event.preventDefault();
    const term = query.trim();
    if (!term) {
      return;
    }

    setLoading(true);
    try {
      const [boe, datos, sia] = await Promise.all([
        searchBoe(term),
        searchDatos(term),
        searchSia(term),
      ]);
      setResults({ boe, datos, sia });
    } catch {
      toast.error("No se han podido consultar las fuentes externas.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Card>
      <div className="flex items-start gap-sm">
        <Landmark size={24} strokeWidth={1.75} className="mt-0.5 shrink-0 text-secondary" />
        <div className="min-w-0">
          <CardTitle>Referencias externas</CardTitle>
          <CardDescription className="mt-1">
            Normativa del BOE, datasets de datos.gob.es y referencias del SIA relacionadas con el
            trámite.
          </CardDescription>
        </div>
      </div>

      <form onSubmit={handleSearch} className="mt-md flex flex-wrap gap-sm">
        <Input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Buscar normativa, datasets o procedimientos…"
          aria-label="Término de búsqueda"
          className="min-w-0 flex-1 sm:min-w-56"
        />
        <Button type="submit" variant="secondary" disabled={loading}>
          {loading ? (
            <Loader2 size={20} strokeWidth={1.75} className="animate-spin" />
          ) : (
            <Search size={20} strokeWidth={1.75} />
          )}
          {loading ? "Buscando…" : "Buscar"}
        </Button>
      </form>

      {results ? (
        <div className="mt-md grid min-w-0 gap-lg md:grid-cols-1 xl:grid-cols-3">
          <ResultList source="BOE" results={results.boe} />
          <ResultList source="datos.gob.es" results={results.datos} />
          <ResultList source="SIA" results={results.sia} />
        </div>
      ) : null}
    </Card>
  );
}

function ResultList({ source, results }: { source: string; results: ExternalReference[] }) {
  const liveCount = results.filter((item) => !item.demo).length;
  const demoCount = results.length - liveCount;

  return (
    <div className="min-w-0">
      <div className="flex flex-wrap items-center gap-xs">
        <Badge tone="external">{source}</Badge>
        {liveCount > 0 ? (
          <span className="text-body-sm text-on-surface-variant">{liveCount} en vivo</span>
        ) : null}
        {demoCount > 0 ? <Badge tone="under-review">{demoCount} demo</Badge> : null}
      </div>
      {results.length === 0 ? (
        <p className="mt-sm text-body-sm text-on-surface-variant">Sin resultados.</p>
      ) : (
        <ul className="mt-sm flex flex-col gap-sm">
          {results.map((item) => (
            <ReferenceItem key={`${item.sourceName}-${item.externalId}`} item={item} />
          ))}
        </ul>
      )}
    </div>
  );
}

function ReferenceItem({ item }: { item: ExternalReference }) {
  const headline = referenceHeadline(item);
  const metaId = compactExternalId(item.externalId);

  return (
    <li className="overflow-hidden rounded-md border border-outline-variant/60 bg-surface-container-low p-md">
      <div className="flex flex-wrap items-start justify-between gap-sm">
        <p className="min-w-0 flex-1 wrap-anywhere font-title-sm text-on-surface line-clamp-3">
          {headline}
        </p>
        {item.demo ? (
          <Badge tone="under-review" className="shrink-0">
            Demo
          </Badge>
        ) : null}
      </div>

      {metaId ? (
        <p className="mt-1.5 wrap-anywhere font-code text-body-sm text-on-surface-variant">
          {metaId}
        </p>
      ) : null}

      {item.snippet ? (
        <p className="mt-sm text-body-sm leading-relaxed text-on-surface-variant line-clamp-3">
          {item.snippet}
        </p>
      ) : null}

      {item.url ? (
        <a
          href={item.url}
          target="_blank"
          rel="noreferrer"
          className={cn(
            "mt-sm inline-flex max-w-full items-center gap-1.5 text-body-sm font-label text-primary",
            "hover:text-primary-hover hover:underline",
          )}
        >
          <ExternalLink size={16} strokeWidth={1.75} className="shrink-0" />
          <span className="truncate">{externalLinkLabel(item)}</span>
        </a>
      ) : null}
    </li>
  );
}
