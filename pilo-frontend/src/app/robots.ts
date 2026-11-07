import type { MetadataRoute } from "next";
import { getSiteUrl } from "@/lib/site";

export default function robots(): MetadataRoute.Robots {
  const siteUrl = getSiteUrl();
  return {
    rules: {
      userAgent: "*",
      allow: ["/", "/procedures", "/procedures/"],
      disallow: [
        "/login",
        "/my-cases",
        "/cases/",
        "/review",
        "/profile",
        "/admin/",
      ],
    },
    sitemap: `${siteUrl}/sitemap.xml`,
  };
}
