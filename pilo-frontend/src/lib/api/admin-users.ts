import { getApiUrl } from "@/lib/api/config";
import { authFetch, readApiError } from "@/lib/api/client";
import type { AdminUser } from "@/lib/api/types";

export async function listAdminUsers(): Promise<AdminUser[]> {
  const response = await authFetch(`${getApiUrl()}/api/v1/admin/users`, {
    cache: "no-store",
  });
  if (!response.ok) {
    await readApiError(response);
  }
  return (await response.json()) as AdminUser[];
}

export async function setAdminUserEnabled(id: string, enabled: boolean): Promise<AdminUser> {
  const response = await authFetch(`${getApiUrl()}/api/v1/admin/users/${id}/enabled`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ enabled }),
  });
  if (!response.ok) {
    await readApiError(response);
  }
  return (await response.json()) as AdminUser;
}
