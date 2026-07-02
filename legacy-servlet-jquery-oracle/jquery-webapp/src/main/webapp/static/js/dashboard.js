// 대시보드 화면 — warehouse 목록 API를 공통 그리드로 표시한다.
$(document).ready(function () {
    LegacyGrid.initPage({
        tableSelector: "#dashboardTable",
        inputSelector: "#searchName",
        buttonSelector: "#searchBtn",
        apiUrl: "/api/warehouse-io",
        searchParam: "name"
    });
});
