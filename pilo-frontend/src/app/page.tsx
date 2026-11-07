import type { Metadata } from "next";
import { homeMetadata } from "@/lib/seo";
import { PageContainer } from "@/components/layout/page-container";
import { FeaturedProcedures } from "@/features/home/featured-procedures";
import { HomeHero } from "@/features/home/home-hero";
import { HowItWorks } from "@/features/home/how-it-works";

export const metadata: Metadata = homeMetadata();

export default function Home() {
  return (
    <PageContainer>
      <HomeHero />
      <FeaturedProcedures />
      <HowItWorks />
    </PageContainer>
  );
}
