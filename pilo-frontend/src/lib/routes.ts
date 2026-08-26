export const routes = {
  home: "/",
  login: "/login",
  procedures: "/procedures",
  procedure: (id: string) => `/procedures/${id}`,
  myCases: "/my-cases",
  profile: "/profile",
  case: (id: string) => `/cases/${id}`,
  adminProcedures: "/admin/procedures",
  adminProcedureNew: "/admin/procedures/new",
  adminProcedure: (id: string) => `/admin/procedures/${id}`,
  loginNext: (nextPath: string) => `/login?next=${encodeURIComponent(nextPath)}`,
} as const;

/**
 * Guards the post-login `?next=` redirect target. Must be a same-site, relative path:
 * rejects absolute URLs and protocol-relative URLs (e.g. `//evil.com`) that browsers would
 * otherwise treat as an off-site redirect (open-redirect prevention).
 */
export function isSafeRedirectPath(path: string | null): path is string {
  return Boolean(path) && path!.startsWith("/") && !path!.startsWith("//") && !path!.startsWith("/\\");
}
