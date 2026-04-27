$(document).ready(function () {
    LegacyGrid.initPage({
        tableSelector: "#settlementTable",
        inputSelector: "#searchName",
        buttonSelector: "#searchBtn",
        apiUrl: "/api/warehouse-io",
        searchParam: "name"
    });
});
