import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "RunMile",
  description: "Daegu Marathon reward and local spending impact demo"
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}

