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
