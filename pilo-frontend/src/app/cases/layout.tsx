import type { Metadata } from "next";
import { privatePageMetadata } from "@/lib/seo";

export const metadata: Metadata = privatePageMetadata("Expediente");

export default function CasesLayout({ children }: LayoutProps<"/cases">) {
  return children;
}
