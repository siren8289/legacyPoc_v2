import "./globals.css";

// 이 프로젝트의 모든 Next.js 라우트에서 공유하는 레이아웃.
export const metadata = {
  title: "Next.js Refactor",
  description: "Feature-based Next.js frontend"
};

export default function RootLayout({ children }) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
