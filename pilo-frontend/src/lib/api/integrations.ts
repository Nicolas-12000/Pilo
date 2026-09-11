import { getApiUrl } from "@/lib/api/config";
import type { ExternalReference } from "@/lib/api/types";

async function searchIntegration(path: string, query: string): Promise<ExternalReference[]> {
  const response = await fetch(
    `${getApiUrl()}/api/v1/integrations/${path}/search?query=${encodeURIComponent(query)}`,
    { cache: "no-store" },
  );

  if (!response.ok) {
    return [];
  }

  return (await response.json()) as ExternalReference[];
}

export async function searchBoe(query: string): Promise<ExternalReference[]> {
  return searchIntegration("boe", query);
}

export async function searchDatos(query: string): Promise<ExternalReference[]> {
  return searchIntegration("datos", query);
}

export async function searchSia(query: string): Promise<ExternalReference[]> {
  return searchIntegration("sia", query);
}
