import { PageContainer } from "@/components/layout/page-container";
import { Skeleton } from "@/components/ui/skeleton";

export default function ProcedureDetailLoading() {
  return (
    <PageContainer>
      <Skeleton className="h-3 w-28" />
      <Skeleton className="mt-3 h-8 w-3/4" />
      <Skeleton className="mt-3 h-4 w-full max-w-2xl" />
      <div className="mt-xl grid gap-lg lg:grid-cols-[1fr_296px]">
        <div className="rounded-lg bg-surface p-md shadow-card md:p-lg">
          <Skeleton className="h-5 w-32" />
          <Skeleton className="mt-4 h-3 w-full" />
          <div className="mt-md flex flex-col gap-xs">
            {Array.from({ length: 4 }, (_, index) => (
              <Skeleton key={index} className="h-16 w-full rounded-sm" />
            ))}
          </div>
        </div>
        <div className="flex flex-col gap-lg">
          <div className="rounded-lg bg-surface p-md shadow-card md:p-lg">
            <Skeleton className="h-5 w-24" />
            <Skeleton className="mt-4 h-10 w-full" />
            <Skeleton className="mt-2 h-10 w-full" />
          </div>
          <Skeleton className="h-12 w-full rounded-md" />
        </div>
      </div>
    </PageContainer>
  );
}
