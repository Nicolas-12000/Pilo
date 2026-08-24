import { cn } from "@/lib/utils/cn";

export function Skeleton({ className }: { className?: string }) {
  return <div className={cn("skeleton-fill rounded-sm", className)} />;
}

export function SkeletonCard() {
  return (
    <div className="elevation-card rounded-lg p-md md:p-lg">
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

export function SkeletonList({ count = 3, columns = 1 }: { count?: number; columns?: 1 | 2 }) {
  return (
    <div className={cn("grid gap-md", columns === 2 && "md:grid-cols-2")}>
      {Array.from({ length: count }, (_, index) => (
        <SkeletonCard key={index} />
      ))}
    </div>
  );
}
