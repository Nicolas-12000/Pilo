import type { ExternalReference } from "@/lib/api/types";

export function isHttpUrl(value: string): boolean {
  return /^https?:\/\//i.test(value.trim());
}

/** Catalog APIs often put the URI in `externalId`; show it only when it reads like a human id (e.g. BOE-A-…). */
export function compactExternalId(externalId: string): string | null {
  const trimmed = externalId.trim();
  if (!trimmed || isHttpUrl(trimmed)) {
    return null;
  }
  if (trimmed.length > 56) {
    return `${trimmed.slice(0, 53)}…`;
  }
  return trimmed;
}

export function referenceHeadline(item: ExternalReference): string {
  const title = item.title.trim();
  if (title && !isHttpUrl(title)) {
    return title;
  }
  const urlForHost = isHttpUrl(title) ? title : item.url;
  if (urlForHost) {
    try {
      const host = new URL(urlForHost).hostname.replace(/^www\./, "");
      return host ? `Recurso en ${host}` : item.sourceName;
    } catch {
      // ignore
    }
  }
  return item.sourceName;
}

export function externalLinkLabel(item: ExternalReference): string {
  if (item.url && isHttpUrl(item.title) && item.title.trim() === item.url.trim()) {
    return "Abrir enlace";
  }
  return "Abrir en nueva pestaña";
}
