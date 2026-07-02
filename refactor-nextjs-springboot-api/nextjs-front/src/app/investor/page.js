"use client";

// 투자자 기능 페이지 — Spring Boot API(/api/investors)와 연동한다.
import { useEffect, useState } from "react";
import { apiGet, apiPost } from "@/src/lib/api";

export default function InvestorPage() {
  const [keyword, setKeyword] = useState("");
  const [rows, setRows] = useState([]);
  const [form, setForm] = useState({
    investorName: "",
    investorGrade: "",
    totalAmount: 0,
    lastProductName: "",
    screenMemo: ""
  });

  async function loadRows() {
    // 투자자 목록 조회 API 호출 경로.
    const json = await apiGet(`/api/investors?name=${encodeURIComponent(keyword)}`);
    setRows(json.data || []);
  }

  async function createRow() {
    // 투자자 등록 API 호출 경로.
    await apiPost("/api/investors", form);
    setForm({ investorName: "", investorGrade: "", totalAmount: 0, lastProductName: "", screenMemo: "" });
    loadRows();
  }

  useEffect(() => { loadRows(); }, []);

  return (
    <main className="container">
      <h1>투자자 기능 (Next.js)</h1>
      <div className="toolbar">
        <input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="이름 검색" />
        <button onClick={loadRows}>조회</button>
      </div>
      <table>
        <thead><tr><th>ID</th><th>이름</th><th>등급</th><th>총투자금</th><th>최근상품</th><th>메모</th></tr></thead>
        <tbody>
          {rows.length === 0 ? (
            <tr><td colSpan={6}>데이터가 없습니다.</td></tr>
          ) : rows.map((item) => (
            <tr key={item.investorId}>
              <td>{item.investorId}</td><td>{item.investorName}</td><td>{item.investorGrade}</td><td>{item.totalAmount}</td><td>{item.lastProductName}</td><td>{item.screenMemo}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <h3>신규 등록</h3>
      <div className="toolbar">
        <input placeholder="이름" value={form.investorName} onChange={(e) => setForm({ ...form, investorName: e.target.value })} />
        <input placeholder="등급" value={form.investorGrade} onChange={(e) => setForm({ ...form, investorGrade: e.target.value })} />
        <input type="number" placeholder="총투자금" value={form.totalAmount} onChange={(e) => setForm({ ...form, totalAmount: Number(e.target.value || 0) })} />
        <input placeholder="최근상품" value={form.lastProductName} onChange={(e) => setForm({ ...form, lastProductName: e.target.value })} />
        <input placeholder="메모" value={form.screenMemo} onChange={(e) => setForm({ ...form, screenMemo: e.target.value })} />
        <button onClick={createRow}>등록</button>
      </div>
    </main>
  );
}
