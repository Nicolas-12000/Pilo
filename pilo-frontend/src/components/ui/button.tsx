import Link from "next/link";
import type { ComponentProps, ReactNode } from "react";
import { cn } from "@/lib/utils/cn";

type ButtonVariant = "primary" | "secondary" | "ghost" | "danger";
type ButtonSize = "md" | "sm" | "icon";

const base =
  "inline-flex items-center justify-center gap-2 font-label transition-[color,transform,background-color] duration-feedback active:scale-[0.98] disabled:cursor-not-allowed disabled:active:scale-100";

const variants: Record<ButtonVariant, string> = {
  primary:
    "rounded-md bg-primary text-on-primary enabled:hover:bg-primary-hover enabled:active:bg-primary-active disabled:bg-primary-disabled disabled:text-on-primary-disabled",
  secondary:
    "rounded-md bg-surface text-on-surface ring-1 ring-outline enabled:hover:bg-surface-container-high disabled:text-on-primary-disabled",
  ghost:
    "rounded-md bg-transparent text-primary-hover enabled:hover:bg-surface-container disabled:text-on-primary-disabled",
  danger:
    "rounded-md bg-danger text-on-danger enabled:hover:brightness-110 disabled:bg-primary-disabled disabled:text-on-primary-disabled",
};

const sizes: Record<ButtonSize, string> = {
  md: "h-12 px-5",
  sm: "h-10 px-4",
  icon: "size-11 rounded-full",
};

function classesFor(variant: ButtonVariant, size: ButtonSize, className?: string) {
  return cn(base, variants[variant], sizes[size], size === "icon" && "px-0", className);
}

type ButtonProps = ComponentProps<"button"> & {
  variant?: ButtonVariant;
  size?: ButtonSize;
};

export function Button({
  variant = "primary",
  size = "md",
  className,
  type = "button",
  ...props
}: ButtonProps) {
  return <button type={type} className={classesFor(variant, size, className)} {...props} />;
}

type ButtonLinkProps = ComponentProps<typeof Link> & {
  variant?: ButtonVariant;
  size?: ButtonSize;
  children: ReactNode;
};

export function ButtonLink({
  variant = "primary",
  size = "md",
  className,
  ...props
}: ButtonLinkProps) {
  return <Link className={classesFor(variant, size, className)} {...props} />;
}
