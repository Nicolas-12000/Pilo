import { afterEach, describe, expect, it } from "vitest";
import { getSiteUrl } from "@/lib/site";

describe("getSiteUrl", () => {
  const originalSiteUrl = process.env.NEXT_PUBLIC_SITE_URL;
  const originalVercel = process.env.VERCEL_URL;

  afterEach(() => {
    if (originalSiteUrl === undefined) {
      delete process.env.NEXT_PUBLIC_SITE_URL;
    } else {
      process.env.NEXT_PUBLIC_SITE_URL = originalSiteUrl;
    }
    if (originalVercel === undefined) {
      delete process.env.VERCEL_URL;
    } else {
      process.env.VERCEL_URL = originalVercel;
    }
  });

  it("prefers NEXT_PUBLIC_SITE_URL", () => {
    process.env.NEXT_PUBLIC_SITE_URL = "https://pilo.example.com/";
    delete process.env.VERCEL_URL;
    expect(getSiteUrl()).toBe("https://pilo.example.com");
  });

  it("falls back to localhost in development", () => {
    delete process.env.NEXT_PUBLIC_SITE_URL;
    delete process.env.VERCEL_URL;
    expect(getSiteUrl()).toBe("http://localhost:3000");
  });
});
