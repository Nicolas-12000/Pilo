"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { getStoredUser } from "@/lib/auth/session";
import type { User } from "@/lib/api/types";

export function SiteHeader() {
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    setUser(getStoredUser());
  }, []);

  return (
    <header className="border-b border-outline-variant/60 bg-surface/90 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-5xl items-center justify-between px-4">
        <Link href="/" className="font-headline-md text-on-surface">
          PILO
        </Link>
        <nav className="flex items-center gap-4">
          <Link href="/tramites" className="font-label text-on-surface-variant hover:text-on-surface">
            Trámites
          </Link>
          {user ? (
            <span className="rounded-full bg-surface-container px-3 py-1.5 text-body-sm text-on-surface">
              {user.fullName}
            </span>
          ) : (
            <Link
              href="/login"
              className="inline-flex h-10 items-center rounded-md bg-primary px-4 font-label text-on-primary hover:bg-primary-hover"
            >
              Iniciar sesión
            </Link>
          )}
        </nav>
      </div>
    </header>
  );
}
