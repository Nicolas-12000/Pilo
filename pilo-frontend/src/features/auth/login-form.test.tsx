import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { ApiError } from "@/lib/api/auth";
import { LoginForm } from "./login-form";

vi.mock("@/lib/api/auth", async () => {
  const actual = await vi.importActual<typeof import("@/lib/api/auth")>("@/lib/api/auth");
  return {
    ...actual,
    login: vi.fn(),
  };
});

vi.mock("@/lib/auth/session", () => ({
  persistSession: vi.fn(),
}));

import { login } from "@/lib/api/auth";
import { persistSession } from "@/lib/auth/session";

const loginMock = vi.mocked(login);
const persistSessionMock = vi.mocked(persistSession);

describe("LoginForm", () => {
  beforeEach(() => {
    loginMock.mockReset();
    persistSessionMock.mockReset();
  });

  it("shows a validation message for an invalid email", async () => {
    const user = userEvent.setup();
    render(<LoginForm />);

    await user.type(screen.getByLabelText("Correo electrónico"), "not-an-email");
    await user.type(screen.getByLabelText("Contraseña"), "Password123!");
    await user.click(screen.getByRole("button", { name: "Entrar" }));

    expect(await screen.findByText("Introduce un correo válido.")).toBeInTheDocument();
    expect(loginMock).not.toHaveBeenCalled();
  });

  it("persists the session after a successful login", async () => {
    const user = userEvent.setup();
    const onSuccess = vi.fn();
    const session = {
      accessToken: "token",
      tokenType: "Bearer" as const,
      expiresIn: 3600,
      user: {
        id: "1",
        email: "user@pilo.test",
        fullName: "Ana Usuario",
        role: "USER" as const,
      },
    };
    loginMock.mockResolvedValue(session);

    render(<LoginForm onSuccess={onSuccess} />);

    await user.type(screen.getByLabelText("Correo electrónico"), "user@pilo.test");
    await user.type(screen.getByLabelText("Contraseña"), "Password123!");
    await user.click(screen.getByRole("button", { name: "Entrar" }));

    expect(loginMock).toHaveBeenCalledWith({
      email: "user@pilo.test",
      password: "Password123!",
    });
    expect(persistSessionMock).toHaveBeenCalledWith(session);
    expect(onSuccess).toHaveBeenCalled();
  });

  it("shows the API error when credentials are rejected", async () => {
    const user = userEvent.setup();
    loginMock.mockRejectedValue(
      new ApiError("El correo o la contraseña no son correctos.", 401, "INVALID_CREDENTIALS"),
    );

    render(<LoginForm />);

    await user.type(screen.getByLabelText("Correo electrónico"), "user@pilo.test");
    await user.type(screen.getByLabelText("Contraseña"), "WrongPassword!");
    await user.click(screen.getByRole("button", { name: "Entrar" }));

    expect(
      await screen.findByText("El correo o la contraseña no son correctos."),
    ).toBeInTheDocument();
    expect(persistSessionMock).not.toHaveBeenCalled();
  });
});
