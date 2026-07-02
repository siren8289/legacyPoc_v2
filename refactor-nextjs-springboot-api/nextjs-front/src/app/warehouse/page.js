"use client";

// 입출고 기능 페이지 — 레거시 필드별 update 대신 일괄 POST 등록을 사용한다.
import { useEffect, useState } from "react";
import { apiGet, apiPost } from "@/src/lib/api";

export default function WarehousePage() {
  const [rows, setRows] = useState([]);
  const [form, setForm] = useState({
    warehouseName: "",
    productCode: "",
    productName: "",
    productCategory: "",
    inQty: 0,
    outQty: 0,
    currentStock: 0,
    clientName: "",
    status: "ACTIVE"
  });

  async function loadRows() {
    // 입출고 목록 조회 API 호출 경로.
    const json = await apiGet("/api/warehouses");
    setRows(json.data || []);
  }

  async function createRow() {
    // 입출고 등록 API 호출 경로.
    await apiPost("/api/warehouses", form);
    setForm({
      warehouseName: "",
      productCode: "",
      productName: "",
      productCategory: "",
      inQty: 0,
      outQty: 0,
      currentStock: 0,
      clientName: "",
      status: "ACTIVE"
    });
    loadRows();
  }

  useEffect(() => { loadRows(); }, []);

  return (
    <main className="container">
      <h1>입출고 기능 (Next.js)</h1>
      <div className="toolbar">
        <button onClick={loadRows}>조회</button>
      </div>
      <table>
        <thead><tr><th>ID</th><th>창고명</th><th>상품코드</th><th>상품명</th><th>입고</th><th>출고</th><th>재고</th><th>상태</th></tr></thead>
        <tbody>
          {rows.length === 0 ? (
            <tr><td colSpan={8}>데이터가 없습니다.</td></tr>
          ) : rows.map((item) => (
            <tr key={item.warehouseIoId}>
              <td>{item.warehouseIoId}</td><td>{item.warehouseName}</td><td>{item.productCode}</td><td>{item.productName}</td><td>{item.inQty}</td><td>{item.outQty}</td><td>{item.currentStock}</td><td>{item.status}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <h3>신규 등록</h3>
      <div className="toolbar">
        <input placeholder="창고명" value={form.warehouseName} onChange={(e) => setForm({ ...form, warehouseName: e.target.value })} />
        <input placeholder="상품코드" value={form.productCode} onChange={(e) => setForm({ ...form, productCode: e.target.value })} />
        <input placeholder="상품명" value={form.productName} onChange={(e) => setForm({ ...form, productName: e.target.value })} />
        <input placeholder="분류" value={form.productCategory} onChange={(e) => setForm({ ...form, productCategory: e.target.value })} />
        <input type="number" placeholder="입고" value={form.inQty} onChange={(e) => setForm({ ...form, inQty: Number(e.target.value || 0) })} />
        <input type="number" placeholder="출고" value={form.outQty} onChange={(e) => setForm({ ...form, outQty: Number(e.target.value || 0) })} />
        <input type="number" placeholder="재고" value={form.currentStock} onChange={(e) => setForm({ ...form, currentStock: Number(e.target.value || 0) })} />
        <input placeholder="거래처" value={form.clientName} onChange={(e) => setForm({ ...form, clientName: e.target.value })} />
        <input placeholder="상태" value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })} />
        <button onClick={createRow}>등록</button>
      </div>
    </main>
  );
}
