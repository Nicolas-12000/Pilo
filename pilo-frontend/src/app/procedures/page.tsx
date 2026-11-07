import type { Metadata } from "next";
import { PageContainer } from "@/components/layout/page-container";
import { ProceduresPanel } from "@/features/procedures/procedures-panel";
import { listProcedureTypes } from "@/lib/api/procedures";
import { catalogListMetadata } from "@/lib/seo";

export const metadata: Metadata = catalogListMetadata();

export const revalidate = 60;

export default async function ProceduresPage() {
  let initialProcedures;
  try {
    initialProcedures = await listProcedureTypes();
  } catch {
    initialProcedures = undefined;
  }

  return (
    <PageContainer>
      <ProceduresPanel initialProcedures={initialProcedures} />
    </PageContainer>
  );
}
