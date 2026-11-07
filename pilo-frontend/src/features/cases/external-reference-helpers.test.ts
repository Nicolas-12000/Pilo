import { describe, expect, it } from "vitest";
import {
  compactExternalId,
  externalLinkLabel,
  isHttpUrl,
  referenceHeadline,
} from "@/features/cases/external-reference-helpers";
import type { ExternalReference } from "@/lib/api/types";

const base: ExternalReference = {
  sourceName: "datos.gob.es",
  externalId: "http://hdl.handle.net/10261/123",
  title: "Registro de Convenios de Aragón 2007",
  url: "http://hdl.handle.net/10261/123",
  snippet: "Descripción breve.",
  demo: false,
};

describe("external reference display helpers", () => {
  it("detects http urls", () => {
    expect(isHttpUrl("https://boe.es/foo")).toBe(true);
    expect(isHttpUrl("BOE-A-2026-1")).toBe(false);
  });

  it("hides url-like external ids", () => {
    expect(compactExternalId(base.externalId)).toBeNull();
    expect(compactExternalId("BOE-A-2026-20528")).toBe("BOE-A-2026-20528");
  });

  it("uses human titles for headlines", () => {
    expect(referenceHeadline(base)).toBe(base.title);
    expect(
      referenceHeadline({
        ...base,
        title: "https://doi.pangaea.de/10.1594/PANGAEA.123",
      }),
    ).toBe("Recurso en doi.pangaea.de");
  });

  it("labels duplicate url title links", () => {
    expect(
      externalLinkLabel({
        ...base,
        title: base.url!,
      }),
    ).toBe("Abrir enlace");
  });
});
