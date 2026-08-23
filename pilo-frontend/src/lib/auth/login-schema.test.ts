import { describe, expect, it } from "vitest";
import { loginSchema } from "./login-schema";

describe("loginSchema", () => {
  it("accepts a valid email and password", () => {
    const result = loginSchema.safeParse({
      email: "user@pilo.test",
      password: "Password123!",
    });

    expect(result.success).toBe(true);
  });

  it("rejects an invalid email", () => {
    const result = loginSchema.safeParse({
      email: "not-an-email",
      password: "Password123!",
    });

    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.error.issues[0]?.message).toBe("Introduce un correo válido.");
    }
  });

  it("rejects an empty password", () => {
    const result = loginSchema.safeParse({
      email: "user@pilo.test",
      password: "",
    });

    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.error.issues[0]?.message).toBe("La contraseña es obligatoria.");
    }
  });
});
