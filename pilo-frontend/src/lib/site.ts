const DEFAULT_DEV_ORIGIN = "http://localhost:3000";

/**
 * Canonical origin for sitemap, Open Graph, and metadataBase.
 * Set NEXT_PUBLIC_SITE_URL in Amplify (e.g. https://main.xxx.amplifyapp.com).
 */
export function getSiteUrl() {
  const configured = process.env.NEXT_PUBLIC_SITE_URL?.replace(/\/$/, "");
  if (configured) {
    return configured;
  }
  const vercel = process.env.VERCEL_URL;
  if (vercel) {
    return `https://${vercel.replace(/\/$/, "")}`;
  }
  return DEFAULT_DEV_ORIGIN;
}
