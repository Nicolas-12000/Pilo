import { Badge } from "@/components/ui/badge";
import type { StatusDescriptor } from "@/lib/cases/status";

export function StatusBadge({
  status,
  className,
}: {
  status: StatusDescriptor;
  className?: string;
}) {
  return (
    <Badge tone={status.tone} icon={status.icon} spinIcon={status.spin} className={className}>
      {status.label}
    </Badge>
  );
}
