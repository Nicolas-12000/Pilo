import type { Metadata } from "next";
import { privatePageMetadata } from "@/lib/seo";

export const metadata: Metadata = privatePageMetadata("Administración");

export default function AdminLayout({ children }: LayoutProps<"/admin">) {
  return children;
}
