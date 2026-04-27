import Link from "next/link";

// 기능별 라우트(투자자/입출고)로 이동하는 홈 페이지.
export default function HomePage() {
  return (
    <main className="container">
      <h1>Next.js + Spring Boot 리팩토링</h1>
      <ul>
        <li><Link href="/investor">투자자 기능</Link></li>
        <li><Link href="/warehouse">입출고 기능</Link></li>
      </ul>
    </main>
  );
}
