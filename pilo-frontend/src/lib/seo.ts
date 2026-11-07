import type { Metadata } from "next";
import { getSiteUrl } from "@/lib/site";

export const SITE_NAME = "PILO";

const DEFAULT_DESCRIPTION =
  "Explora trámites administrativos, requisitos y plazos. Abre un expediente, sube documentos y sigue la validación y la revisión en un solo sitio.";

export const rootMetadata: Metadata = {
  title: {
    default: `${SITE_NAME} · Trámites administrativos`,
    template: `%s · ${SITE_NAME}`,
  },
  description: DEFAULT_DESCRIPTION,
  metadataBase: new URL(getSiteUrl()),
  openGraph: {
    type: "website",
    locale: "es_ES",
    siteName: SITE_NAME,
    title: `${SITE_NAME} · Trámites administrativos`,
    description: DEFAULT_DESCRIPTION,
  },
};

/** Authenticated or operational screens — not meant for search indexes. */
export function privatePageMetadata(title: string): Metadata {
  return {
    title,
    robots: {
      index: false,
      follow: false,
      googleBot: { index: false, follow: false },
    },
  };
}

export function homeMetadata(): Metadata {
  return {
    title: "Trámites administrativos, de extremo a extremo",
    description: DEFAULT_DESCRIPTION,
    alternates: { canonical: "/" },
    openGraph: {
      title: `${SITE_NAME} · Trámites administrativos`,
      description: DEFAULT_DESCRIPTION,
      url: "/",
    },
  };
}

export function catalogListMetadata(): Metadata {
  const description =
    "Catálogo público de trámites: requisitos, plazos objetivo y detalle de cada procedimiento.";
  return {
    title: "Trámites disponibles",
    description,
    alternates: { canonical: "/procedures" },
    openGraph: {
      title: `Trámites disponibles · ${SITE_NAME}`,
      description,
      url: "/procedures",
    },
  };
}

export function procedureDetailMetadata(input: {
  title: string;
  description: string | null;
  id: string;
  targetDays: number;
}): Metadata {
  const summary =
    input.description?.trim() ||
    `Trámite con plazo objetivo de ${input.targetDays} días. Consulta requisitos e inicia un expediente.`;
  return {
    title: input.title,
    description: summary,
    alternates: { canonical: `/procedures/${input.id}` },
    openGraph: {
      title: `${input.title} · ${SITE_NAME}`,
      description: summary,
      url: `/procedures/${input.id}`,
    },
  };
}
