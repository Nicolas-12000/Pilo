"use client";

import { useQueryClient } from "@tanstack/react-query";
import { useEffect } from "react";
import { listProcedureTypes } from "@/lib/api/procedures";
import { procedureKeys } from "@/lib/api/queries";

/** Warms the procedure catalog cache so /procedures feels instant after landing. */
export function PrefetchCatalog() {
  const queryClient = useQueryClient();

  useEffect(() => {
    void queryClient.prefetchQuery({
      queryKey: procedureKeys.list(),
      queryFn: listProcedureTypes,
      staleTime: 60_000,
    });
  }, [queryClient]);

  return null;
}
