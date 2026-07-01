const rowsEl = document.getElementById("rows");
const modal = document.getElementById("modal");

// 바닐라 JS 입출고 화면 컨트롤러: fetch 기반 조회/등록 흐름을 담당한다.
document.getElementById("loadBtn").addEventListener("click", loadRows);
document.getElementById("createBtn").addEventListener("click", () => modal.classList.remove("hidden"));
document.getElementById("closeBtn").addEventListener("click", () => modal.classList.add("hidden"));
document.getElementById("saveBtn").addEventListener("click", createWarehouse);

async function loadRows() {
    const res = await fetch("/api/warehouses");
    const json = await res.json();
    renderRows(json.data || []);
}

function renderRows(items) {
    rowsEl.innerHTML = "";
    if (!items.length) {
        rowsEl.innerHTML = "<tr><td colspan='8'>데이터가 없습니다.</td></tr>";
        return;
    }
    items.forEach(item => {
        const tr = document.createElement("tr");
        tr.innerHTML = `<td>${nvl(item.warehouseIoId)}</td><td>${nvl(item.warehouseName)}</td><td>${nvl(item.productCode)}</td><td>${nvl(item.productName)}</td><td>${nvl(item.inQty)}</td><td>${nvl(item.outQty)}</td><td>${nvl(item.currentStock)}</td><td>${nvl(item.status)}</td>`;
        rowsEl.appendChild(tr);
    });
}

async function createWarehouse() {
    const payload = {
        warehouseName: document.getElementById("warehouseName").value,
        productCode: document.getElementById("productCode").value,
        productName: document.getElementById("productName").value,
        productCategory: document.getElementById("productCategory").value,
        inQty: Number(document.getElementById("inQty").value || 0),
        outQty: Number(document.getElementById("outQty").value || 0),
        currentStock: Number(document.getElementById("currentStock").value || 0),
        clientName: document.getElementById("clientName").value,
        status: document.getElementById("status").value
    };

    await fetch("/api/warehouses", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });

    modal.classList.add("hidden");
    loadRows();
}

function nvl(v) {
    return v === null || v === undefined ? "" : v;
}

loadRows();
