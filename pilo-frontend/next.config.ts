import type { NextConfig } from "next";

const explicitApiUrl = process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "") ?? "";
const isDev = process.env.NODE_ENV !== "production";
const apiConnect = explicitApiUrl || (isDev ? "http://localhost:8080" : "");

// Turbopack/webpack dev mode needs 'unsafe-eval' (source maps, HMR) and a websocket connection
// for live reload. Neither is present in a production build, so both are dev-only.
const scriptSrc = isDev ? "'self' 'unsafe-inline' 'unsafe-eval'" : "'self' 'unsafe-inline'";
const connectSrc = [
  "'self'",
  apiConnect,
  isDev ? "ws:" : "",
  // Presigned PUT/GET go straight to the documents bucket, not to Amplify.
  "https://*.amazonaws.com",
  "https://s3.*.amazonaws.com",
]
  .filter(Boolean)
  .join(" ");

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
      "img-src 'self' data: blob:",
      "font-src 'self' data:",
      `connect-src ${connectSrc}`,
      "frame-src 'self' blob:",
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
