export const routes = {
  home: "/",
  login: "/login",
  procedures: "/procedures",
  procedure: (id: string) => `/procedures/${id}`,
  myCases: "/my-cases",
  case: (id: string) => `/cases/${id}`,
  adminProcedures: "/admin/procedures",
  adminProcedureNew: "/admin/procedures/new",
  adminProcedure: (id: string) => `/admin/procedures/${id}`,
  loginNext: (nextPath: string) => `/login?next=${encodeURIComponent(nextPath)}`,
} as const;
