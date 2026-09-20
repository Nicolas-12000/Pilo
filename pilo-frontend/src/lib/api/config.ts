/**
 * Browser calls to the Spring API.
 *
 * Local `next dev`: defaults to http://localhost:8080.
 * Amplify production: leave NEXT_PUBLIC_API_URL unset so requests stay
 * same-origin (`/api/v1/...`) and Amplify rewrites them to the EC2 Elastic IP.
 */
export function getApiUrl() {
  const configured = process.env.NEXT_PUBLIC_API_URL;
  if (configured != null && configured !== "") {
    return configured.replace(/\/$/, "");
  }
  if (process.env.NODE_ENV === "production") {
    return "";
  }
  return "http://localhost:8080";
}
