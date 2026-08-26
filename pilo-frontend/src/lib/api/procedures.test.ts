import { afterEach, describe, expect, it, vi } from "vitest";
import { getApiUrl } from "./config";
import { listProcedureTypes } from "./procedures";

describe("listProcedureTypes", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("loads the public catalog without authentication", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        json: async () => [
          {
            id: "1",
            title: "Registro de contrato de alquiler",
            description: "Descripción",
            targetDays: 15,
            requirementCount: 3,
          },
        ],
      }),
    );

    const procedures = await listProcedureTypes();

    expect(fetch).toHaveBeenCalledWith(
      `${getApiUrl()}/api/v1/procedures/types`,
      expect.objectContaining({ next: { revalidate: 60 } }),
    );
    expect(procedures).toHaveLength(1);
    expect(procedures[0]?.title).toBe("Registro de contrato de alquiler");
  });
});
