import { differenceInCalendarDays, format, formatDistanceToNowStrict } from "date-fns";
import { es } from "date-fns/locale";

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
};

export function describeDeadline(value: string): DeadlineInfo {
  const daysLeft = differenceInCalendarDays(new Date(value), new Date());
  const overdue = daysLeft < 0;
  const urgent = !overdue && daysLeft <= 3;

  if (overdue) {
    return { label: `Vencido ${formatRelative(value)}`, daysLeft, urgent: false, overdue };
  }
  if (daysLeft === 0) {
    return { label: "Vence hoy", daysLeft, urgent: true, overdue };
  }
  return {
    label: `${daysLeft} ${daysLeft === 1 ? "día" : "días"} restantes`,
    daysLeft,
    urgent,
    overdue,
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
