$(document).ready(function () {
    LegacyGrid.initPage({
        tableSelector: "#orderTable",
        inputSelector: "#searchName",
        buttonSelector: "#searchBtn",
        apiUrl: "/api/warehouse-io",
        searchParam: "name"
    });
});
