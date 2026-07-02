// 주문현황 화면 — 전용 백엔드 없이 warehouse 목록 API(/api/warehouse-io)를 재사용한다.
$(document).ready(function () {
    LegacyGrid.initPage({
        tableSelector: "#orderTable",
        inputSelector: "#searchName",
        buttonSelector: "#searchBtn",
        apiUrl: "/api/warehouse-io",
        searchParam: "name"
    });
});
