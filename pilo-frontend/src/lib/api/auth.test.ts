import { afterEach, describe, expect, it, vi } from "vitest";
import { ApiError, login } from "./auth";

describe("login", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("posts credentials and returns the session payload", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        accessToken: "token",
        tokenType: "Bearer",
        expiresIn: 3600,
        user: {
          id: "1",
          email: "user@pilo.test",
          fullName: "Ana Usuario",
          role: "USER",
        },
      }),
    });
    vi.stubGlobal("fetch", fetchMock);

    const result = await login({
      email: "user@pilo.test",
      password: "Password123!",
    });

    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/auth/login",
      expect.objectContaining({ method: "POST" }),
    );
    expect(result.accessToken).toBe("token");
    expect(result.user.role).toBe("USER");
  });

  it("maps an unauthorized API error to a user-facing message", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: false,
        status: 401,
        json: async () => ({
          error: "INVALID_CREDENTIALS",
          message: "El correo o la contraseña no son correctos.",
        }),
      }),
    );

    await expect(
      login({ email: "user@pilo.test", password: "wrong" }),
    ).rejects.toMatchObject({
      name: "ApiError",
      message: "El correo o la contraseña no son correctos.",
      status: 401,
    } satisfies Partial<ApiError>);
  });
});
