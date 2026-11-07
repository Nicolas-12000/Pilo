/**
 * Base URL of the Spring API.
 *
 * Local `next dev`: defaults to http://localhost:8080.
 * Amplify production: leave NEXT_PUBLIC_API_URL unset. The browser stays same-origin
 * (`/api/v1/...`, proxied by the Next rewrite), while Server Components call
 * PILO_BACKEND_ORIGIN directly because Node's fetch cannot resolve relative URLs.
 */
export function getApiUrl() {
  const configured = process.env.NEXT_PUBLIC_API_URL;
  if (configured != null && configured !== "") {
    return configured.replace(/\/$/, "");
  }
  if (typeof window === "undefined" && process.env.PILO_BACKEND_ORIGIN) {
    return process.env.PILO_BACKEND_ORIGIN;
  }
  if (process.env.NODE_ENV === "production") {
    return "";
  }
  return "http://localhost:8080";
}
