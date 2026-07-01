const rowsEl = document.getElementById("rows");
const modal = document.getElementById("modal");

// 바닐라 JS 투자자 화면 컨트롤러: 목록 렌더링과 등록 흐름을 담당한다.
document.getElementById("searchBtn").addEventListener("click", loadRows);
document.getElementById("createBtn").addEventListener("click", () => modal.classList.remove("hidden"));
document.getElementById("closeBtn").addEventListener("click", () => modal.classList.add("hidden"));
document.getElementById("saveBtn").addEventListener("click", createInvestor);

async function loadRows() {
    // 조회 흐름: 목록 API 호출 후 tbody를 다시 그린다.
    const name = document.getElementById("nameKeyword").value || "";
    const res = await fetch(`/api/investors?name=${encodeURIComponent(name)}`);
    const json = await res.json();
    renderRows(json.data || []);
}

function renderRows(items) {
    rowsEl.innerHTML = "";
    if (!items.length) {
        rowsEl.innerHTML = "<tr><td colspan='6'>데이터가 없습니다.</td></tr>";
        return;
    }
    items.forEach(item => {
        const tr = document.createElement("tr");
        tr.innerHTML = `<td>${nvl(item.investorId)}</td><td>${nvl(item.investorName)}</td><td>${nvl(item.investorGrade)}</td><td>${nvl(item.totalAmount)}</td><td>${nvl(item.lastProductName)}</td><td>${nvl(item.screenMemo)}</td>`;
        rowsEl.appendChild(tr);
    });
}

async function createInvestor() {
    // 등록 흐름: 폼 전송 후 목록을 재조회한다.
    const payload = {
        investorName: document.getElementById("investorName").value,
        investorGrade: document.getElementById("investorGrade").value,
        totalAmount: Number(document.getElementById("totalAmount").value || 0),
        lastProductName: document.getElementById("lastProductName").value,
        screenMemo: document.getElementById("screenMemo").value
    };

    await fetch("/api/investors", {
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
