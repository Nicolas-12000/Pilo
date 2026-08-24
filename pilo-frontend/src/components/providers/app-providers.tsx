"use client";

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ThemeProvider } from "next-themes";
import type { ReactNode } from "react";
import { useState } from "react";
import { Toaster } from "sonner";

export function AppProviders({ children }: { children: ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 15_000,
            retry: 1,
            refetchOnWindowFocus: false,
          },
        },
      }),
  );

  return (
    <ThemeProvider attribute="class" defaultTheme="light" enableSystem={false} disableTransitionOnChange>
      <QueryClientProvider client={queryClient}>
        {children}
        <Toaster
          position="top-center"
          toastOptions={{
            classNames: {
              toast:
                "rounded-md bg-surface text-on-surface shadow-elevated ring-1 ring-outline-variant",
              success: "bg-success-container text-on-success-container ring-0",
              error: "bg-danger-container text-on-danger-container ring-0",
              info: "bg-info-container text-on-info-container ring-0",
            },
          }}
        />
      </QueryClientProvider>
    </ThemeProvider>
  );
}
