"use client";

import { Landmark, Loader2, Search } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardDescription, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/field";
import { searchBoe, searchDatos, searchSia } from "@/lib/api/integrations";
import type { ExternalReference } from "@/lib/api/types";

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
        <div>
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
          className="min-w-55 flex-1"
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
        <div className="mt-md grid gap-md lg:grid-cols-3">
          <ResultList source="BOE" results={results.boe} />
          <ResultList source="datos.gob.es" results={results.datos} />
          <ResultList source="SIA" results={results.sia} />
        </div>
      ) : null}
    </Card>
  );
}

function ResultList({ source, results }: { source: string; results: ExternalReference[] }) {
  return (
    <div>
      <Badge tone="external">{source}</Badge>
      {results.length === 0 ? (
        <p className="mt-xs text-body-sm text-on-surface-variant">Sin resultados.</p>
      ) : (
        <ul className="mt-xs flex flex-col gap-xs">
          {results.map((item) => (
            <li
              key={`${item.sourceName}-${item.externalId}`}
              className="rounded-sm bg-surface-container-low p-sm"
            >
              <p className="font-code text-secondary">{item.externalId}</p>
              {item.url ? (
                <a
                  href={item.url}
                  target="_blank"
                  rel="noreferrer"
                  className="mt-1 block text-body-sm text-primary hover:underline"
                >
                  {item.title}
                </a>
              ) : (
                <p className="mt-1 text-body-sm text-on-surface">{item.title}</p>
              )}
              {item.snippet ? (
                <p className="mt-1 text-body-sm text-on-surface-variant">{item.snippet}</p>
              ) : null}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
