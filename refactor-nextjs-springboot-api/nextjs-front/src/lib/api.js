const API_BASE = process.env.NEXT_PUBLIC_API_BASE || "http://localhost:8083";

// Next.js 프론트 → Spring Boot API(:8083) 호출 공통 유틸
export async function apiGet(path) {
  // 기능 페이지에서 base URL 처리 중복을 막기 위한 공통 GET 유틸.
  const res = await fetch(`${API_BASE}${path}`, { cache: "no-store" });
  return res.json();
}

export async function apiPost(path, payload) {
  // 등록 폼에서 공통으로 사용하는 JSON POST 유틸.
  const res = await fetch(`${API_BASE}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
  return res.json();
}
