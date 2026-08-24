import { PageContainer } from "@/components/layout/page-container";
import { Skeleton } from "@/components/ui/skeleton";
import { SkeletonList } from "@/components/ui/skeleton";

export default function ProceduresLoading() {
  return (
    <PageContainer>
      <Skeleton className="h-3 w-20" />
      <Skeleton className="mt-3 h-8 w-2/3" />
      <Skeleton className="mt-3 h-4 w-full max-w-xl" />
      <div className="mt-xl">
        <SkeletonList count={2} columns={2} />
      </div>
    </PageContainer>
  );
}
