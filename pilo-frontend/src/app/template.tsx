import { PageEnter } from "@/components/motion/page-enter";

export default function RootTemplate({ children }: LayoutProps<"/">) {
  return <PageEnter>{children}</PageEnter>;
}
