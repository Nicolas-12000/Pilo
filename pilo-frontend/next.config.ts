import type { NextConfig } from "next";

const nextConfig: NextConfig = {
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
