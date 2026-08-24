import { getApiUrl } from "@/lib/api/config";
import type { ExternalReference } from "@/lib/api/types";

export async function searchBoe(query: string): Promise<ExternalReference[]> {
  const response = await fetch(
    `${getApiUrl()}/api/v1/integrations/boe/search?query=${encodeURIComponent(query)}`,
    { cache: "no-store" },
  );

  if (!response.ok) {
    return [];
  }

  return (await response.json()) as ExternalReference[];
}

export async function searchDatos(query: string): Promise<ExternalReference[]> {
  const response = await fetch(
    `${getApiUrl()}/api/v1/integrations/datos/search?query=${encodeURIComponent(query)}`,
    { cache: "no-store" },
  );

  if (!response.ok) {
    return [];
  }

  return (await response.json()) as ExternalReference[];
}
