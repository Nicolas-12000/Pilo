import { differenceInCalendarDays, format, formatDistanceToNowStrict } from "date-fns";
import { es } from "date-fns/locale";
import type { CaseStatus } from "@/lib/api/types";

export function formatDate(value: string) {
  return format(new Date(value), "d MMM yyyy", { locale: es });
}

export function formatDateTime(value: string) {
  return format(new Date(value), "d MMM yyyy, HH:mm", { locale: es });
}

export function formatRelative(value: string) {
  return formatDistanceToNowStrict(new Date(value), { locale: es, addSuffix: true });
}

export type DeadlineInfo = {
  label: string;
  daysLeft: number;
  urgent: boolean;
  overdue: boolean;
  closed: boolean;
};

const CLOSED_STATUSES: ReadonlySet<CaseStatus> = new Set(["APPROVED", "REJECTED"]);

export function describeDeadline(value: string, status?: CaseStatus, now = new Date()): DeadlineInfo {
  const daysLeft = differenceInCalendarDays(new Date(value), now);
  if (status && CLOSED_STATUSES.has(status)) {
    return { label: "Expediente cerrado", daysLeft, urgent: false, overdue: false, closed: true };
  }
  const overdue = daysLeft < 0;
  const urgent = !overdue && daysLeft <= 3;

  if (overdue) {
    const daysAgo = -daysLeft;
    return {
      label: `Vencido hace ${daysAgo} ${daysAgo === 1 ? "día" : "días"}`,
      daysLeft,
      urgent: false,
      overdue,
      closed: false,
    };
  }
  if (daysLeft === 0) {
    return { label: "Vence hoy", daysLeft, urgent: true, overdue, closed: false };
  }
  return {
    label: daysLeft === 1 ? "1 día restante" : `${daysLeft} días restantes`,
    daysLeft,
    urgent,
    overdue,
    closed: false,
  };
}

export function formatFileSize(bytes: number) {
  if (bytes < 1024) {
    return `${bytes} B`;
  }
  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(0)} KB`;
  }
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}
