import { cn } from "@/lib/utils/cn";

export function Skeleton({ className }: { className?: string }) {
  return <div className={cn("animate-pulse rounded-sm bg-surface-container-high", className)} />;
}

export function SkeletonCard() {
  return (
    <div className="rounded-lg bg-surface p-md shadow-card md:p-lg">
      <Skeleton className="h-3 w-24" />
      <Skeleton className="mt-3 h-5 w-2/3" />
      <Skeleton className="mt-4 h-2 w-full rounded-full" />
      <div className="mt-4 flex gap-4">
        <Skeleton className="h-3 w-20" />
        <Skeleton className="h-3 w-20" />
      </div>
    </div>
  );
}

export function SkeletonList({ count = 3 }: { count?: number }) {
  return (
    <div className="grid gap-4">
      {Array.from({ length: count }, (_, index) => (
        <SkeletonCard key={index} />
      ))}
    </div>
  );
}
