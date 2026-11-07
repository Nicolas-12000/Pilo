import { Inter, JetBrains_Mono, Plus_Jakarta_Sans } from "next/font/google";
import { AppShell } from "@/components/layout/app-shell";
import { AppProviders } from "@/components/providers/app-providers";
import { rootMetadata } from "@/lib/seo";
import "./globals.css";

const jakarta = Plus_Jakarta_Sans({
  variable: "--font-jakarta",
  subsets: ["latin"],
  weight: ["600", "700"],
});

const inter = Inter({
  variable: "--font-inter",
  subsets: ["latin"],
  weight: ["400", "600", "700"],
});

const jetBrainsMono = JetBrains_Mono({
  variable: "--font-mono-code",
  subsets: ["latin"],
  weight: ["500"],
});

export const metadata = rootMetadata;

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html
      lang="es"
      suppressHydrationWarning
      className={`${jakarta.variable} ${inter.variable} ${jetBrainsMono.variable} h-full antialiased`}
    >
      <body className="min-h-full bg-background font-body text-on-surface">
        <AppProviders>
          <AppShell>{children}</AppShell>
        </AppProviders>
      </body>
    </html>
  );
}
