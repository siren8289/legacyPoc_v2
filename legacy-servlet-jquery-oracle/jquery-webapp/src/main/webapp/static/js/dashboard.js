$(document).ready(function () {
    LegacyGrid.initPage({
        tableSelector: "#dashboardTable",
        inputSelector: "#searchName",
        buttonSelector: "#searchBtn",
        apiUrl: "/api/warehouse-io",
        searchParam: "name"
    });
});
