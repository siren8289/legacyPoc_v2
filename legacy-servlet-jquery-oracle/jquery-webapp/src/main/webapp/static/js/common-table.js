// 공통 컬럼/렌더링 모델을 재사용하는 화면용 테이블 유틸리티.
// order-status, settlement, dashboard 화면이 동일한 columns 정의를 공유한다.
window.LegacyGrid = (function () {
    // TB_WAREHOUSE_IO_SCREEN 기준 공통 표시 컬럼
    var columns = [
        { key: "warehouseIoId", label: "WAREHOUSE_IO_ID" },
        { key: "warehouseName", label: "WAREHOUSE_NAME" },
        { key: "productCode", label: "PRODUCT_CODE" },
        { key: "productName", label: "PRODUCT_NAME" },
        { key: "inQty", label: "IN_QTY" },
        { key: "status", label: "STATUS" }
    ];

    function initPage(options) {
        // 테이블, 검색 input, 조회 버튼을 하나의 동작으로 연결한다.
        var $table = $(options.tableSelector);
        var $input = $(options.inputSelector);
        var $button = $(options.buttonSelector);

        renderHeader($table);

        $button.on("click", function () {
            loadRows($table, options.apiUrl, options.searchParam, $input.val());
        });

        loadRows($table, options.apiUrl, options.searchParam, $input.val());
    }

    function loadRows($table, apiUrl, searchParam, keyword) {
        // 여러 화면에서 공통으로 쓰는 조회 API 호출 함수.
        var query = {};
        query[searchParam || "name"] = keyword;

        $.ajax({
            url: apiUrl,
            method: "GET",
            data: query,
            success: function (response) {
                renderRows($table, response.data || []);
            },
            error: function () {
                alert("조회 중 오류가 발생했습니다.");
            }
        });
    }

    function renderHeader($table) {
        // 공통 컬럼 설정을 기준으로 헤더를 구성한다.
        var html = "<tr>";
        $.each(columns, function (_, col) {
            html += "<th>" + col.label + "</th>";
        });
        html += "</tr>";
        $table.find("thead").html(html);
    }

    function renderRows($table, items) {
        // 공통 key 매핑을 기준으로 바디 행을 렌더링한다.
        var $tbody = $table.find("tbody");
        $tbody.empty();

        if (!items.length) {
            $tbody.append("<tr><td colspan='" + columns.length + "'>데이터가 없습니다.</td></tr>");
            return;
        }

        $.each(items, function (_, item) {
            var row = "<tr>";
            $.each(columns, function (_, col) {
                row += "<td>" + nvl(item[col.key]) + "</td>";
            });
            row += "</tr>";
            $tbody.append(row);
        });
    }

    function nvl(value) {
        return value === null || value === undefined ? "" : value;
    }

    return {
        initPage: initPage
    };
})();
