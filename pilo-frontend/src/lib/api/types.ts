export type Role = "USER" | "REVIEWER" | "ADMIN";

export type User = {
  id: string;
  email: string;
  fullName: string;
  role: Role;
};

export type LoginResponse = {
  accessToken: string;
  tokenType: "Bearer";
  expiresIn: number;
  user: User;
};

export type ApiErrorBody = {
  error: string;
  message: string;
};

export type ProcedureTypeSummary = {
  id: string;
  title: string;
  description: string;
  targetDays: number;
  requirementCount: number;
};

export type Requirement = {
  id: string;
  code: string;
  name: string;
  description: string;
  mandatory: boolean;
};

export type ProcedureTypeDetail = ProcedureTypeSummary & {
  requirements: Requirement[];
};
