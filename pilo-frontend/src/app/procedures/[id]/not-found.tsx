import { Landmark } from "lucide-react";
import { PageContainer } from "@/components/layout/page-container";
import { ButtonLink } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";
import { routes } from "@/lib/routes";

export default function ProcedureNotFound() {
  return (
    <PageContainer>
      <EmptyState
        icon={Landmark}
        title="Trámite no encontrado"
        description="Ese trámite no existe o ya no está disponible en el catálogo."
        action={<ButtonLink href={routes.procedures}>Volver al catálogo</ButtonLink>}
      />
    </PageContainer>
  );
}
