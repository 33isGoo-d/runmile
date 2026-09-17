import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "RunMile | 완주를 지역소비로",
  description: "대구마라톤 완주 보상과 지역상권 효과 분석 서비스"
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}

