import type { NextConfig } from "next";

const apiUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
const isDev = process.env.NODE_ENV !== "production";

// Turbopack/webpack dev mode needs 'unsafe-eval' (source maps, HMR) and a websocket connection
// for live reload. Neither is present in a production build, so both are dev-only.
const scriptSrc = isDev ? "'self' 'unsafe-inline' 'unsafe-eval'" : "'self' 'unsafe-inline'";
const connectSrc = isDev ? `'self' ${apiUrl} ws:` : `'self' ${apiUrl}`;

const securityHeaders = [
  { key: "X-Content-Type-Options", value: "nosniff" },
  { key: "X-Frame-Options", value: "DENY" },
  { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
  { key: "Permissions-Policy", value: "camera=(), microphone=(), geolocation=()" },
  {
    key: "Content-Security-Policy",
    value: [
      "default-src 'self'",
      `script-src ${scriptSrc}`,
      "style-src 'self' 'unsafe-inline'",
      "img-src 'self' data:",
      "font-src 'self' data:",
      `connect-src ${connectSrc}`,
      "frame-ancestors 'none'",
      "object-src 'none'",
      "base-uri 'self'",
      "form-action 'self'",
    ].join("; "),
  },
];

const nextConfig: NextConfig = {
  async headers() {
    return [{ source: "/:path*", headers: securityHeaders }];
  },
  async redirects() {
    return [
      { source: "/tramites", destination: "/procedures", permanent: true },
      { source: "/tramites/:id", destination: "/procedures/:id", permanent: true },
      { source: "/mis-expedientes", destination: "/my-cases", permanent: true },
      { source: "/expedientes/:id", destination: "/cases/:id", permanent: true },
    ];
  },
};

export default nextConfig;
