import type { MetadataRoute } from "next";
import { listProcedureTypes } from "@/lib/api/procedures";
import { getSiteUrl } from "@/lib/site";

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const siteUrl = getSiteUrl();
  const now = new Date();

  const staticEntries: MetadataRoute.Sitemap = [
    { url: siteUrl, lastModified: now, changeFrequency: "weekly", priority: 1 },
    {
      url: `${siteUrl}/procedures`,
      lastModified: now,
      changeFrequency: "daily",
      priority: 0.9,
    },
  ];

  let procedureEntries: MetadataRoute.Sitemap = [];
  try {
    const procedures = await listProcedureTypes();
    procedureEntries = procedures.map((procedure) => ({
      url: `${siteUrl}/procedures/${procedure.id}`,
      lastModified: now,
      changeFrequency: "weekly" as const,
      priority: 0.8,
    }));
  } catch {
    // Build or deploy may run without the API; static entries are still valid.
  }

  return [...staticEntries, ...procedureEntries];
}
