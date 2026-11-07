"use client";

import { useQuery } from "@tanstack/react-query";
import { ChevronRight } from "lucide-react";
import Link from "next/link";
import { SkeletonList } from "@/components/ui/skeleton";
import { ProcedureCard } from "@/features/procedures/procedure-card";
import { listProcedureTypes } from "@/lib/api/procedures";
import { procedureKeys } from "@/lib/api/queries";
import { routes } from "@/lib/routes";

const MAX_FEATURED = 4;

export function FeaturedProcedures() {
  const { data, isPending, error } = useQuery({
    queryKey: procedureKeys.list(),
    queryFn: listProcedureTypes,
    staleTime: 60_000,
  });

  // Procedures without requirements are drafts an admin is still configuring.
  const featured = (data ?? []).filter((p) => p.requirementCount > 0).slice(0, MAX_FEATURED);

  if (error || (!isPending && featured.length === 0)) {
    return null;
  }

  return (
    <section className="mt-section" aria-labelledby="featured-procedures-heading">
      <div className="flex items-baseline justify-between gap-sm">
        <h2 id="featured-procedures-heading" className="font-headline-md text-on-surface">
          Trámites disponibles
        </h2>
        <Link
          href={routes.procedures}
          className="inline-flex items-center gap-1 font-label text-primary-hover"
        >
          Ver catálogo
          <ChevronRight size={16} strokeWidth={1.75} />
        </Link>
      </div>

      <div className="mt-lg">
        {isPending ? (
          <SkeletonList count={2} columns={2} />
        ) : (
          <div className="grid gap-md md:grid-cols-2">
            {featured.map((procedure) => (
              <ProcedureCard key={procedure.id} procedure={procedure} />
            ))}
          </div>
        )}
      </div>
    </section>
  );
}
