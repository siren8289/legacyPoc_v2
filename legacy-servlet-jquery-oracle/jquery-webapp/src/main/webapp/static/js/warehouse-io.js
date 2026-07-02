var dataProvider;
var gridView;
var isFallbackMode = false;

// 입출고 화면 컨트롤러:
// - 목록 렌더링(RealGrid 또는 폴백 테이블)
// - 생성/상세/수정/삭제 API 흐름 제어
$(document).ready(function () {
    bindEvents();
    initRealGrid();
    loadList();
});

function bindEvents() {
    // 상단 버튼 동작 연결
    $("#searchBtn").on("click", loadList);
    $("#createBtn").on("click", createEmptyRowAndOpenModal);
    $("#closeModalBtn").on("click", closeModal);
    $("#modalSaveBtn").on("click", function () {
        // 레거시 동작: 대부분 필드는 change 시점에 이미 저장된 상태다.
        loadList();
        closeModal();
    });
    $("#ioFallbackTable").on("click", "tr[data-id]", function (e) {
        // 행 클릭 시 상세 모달을 연다.
        if ($(e.target).hasClass("delete-btn")) {
            return;
        }
        openDetailModal($(this).data("id"));
    });
    $("#ioFallbackTable").on("click", ".delete-btn", function () {
        // 행 단위 삭제 동작.
        var id = $(this).data("id");
        deleteRow(id);
    });

    // 필드 변경 즉시 저장 이벤트 연결(레거시 패턴).
    $("#WAREHOUSE_NAME").on("change", function () { updateField("updateWarehouseName", "warehouseName", $(this).val()); });
    $("#PRODUCT_CODE").on("change", function () { updateField("updateProductCode", "productCode", $(this).val()); });
    $("#PRODUCT_NAME").on("change", function () { updateField("updateProductName", "productName", $(this).val()); });
    $("#PRODUCT_CATEGORY").on("change", function () { updateField("updateProductCategory", "productCategory", $(this).val()); });
    $("#IN_QTY").on("change", function () { updateField("updateInQty", "inQty", $(this).val()); });
    $("#OUT_QTY").on("change", function () { updateField("updateOutQty", "outQty", $(this).val()); });
    $("#CURRENT_STOCK").on("change", function () { updateField("updateCurrentStock", "currentStock", $(this).val()); });
    $("#CLIENT_NAME").on("change", function () { updateField("updateClientName", "clientName", $(this).val()); });
    $("#MANAGER_NAME").on("change", function () { updateField("updateManagerName", "managerName", $(this).val()); });
    $("#MANAGER_PHONE").on("change", function () { updateField("updateManagerPhone", "managerPhone", $(this).val()); });
    $("#IO_DATE").on("change", function () { updateField("updateIoDate", "ioDate", $(this).val()); });
    $("#STATUS").on("change", function () { updateField("updateStatus", "status", $(this).val()); });
    $("#MEMO").on("change", function () { updateField("updateMemo", "memo", $(this).val()); });
}

function initRealGrid() {
    if (!window.RealGrid) {
        // RealGrid 미로딩 시 안정적으로 폴백 테이블 모드로 전환한다.
        isFallbackMode = true;
        $("#ioGrid").hide();
        $("#gridFallbackWrap").show();
        return;
    }

    dataProvider = new RealGrid.LocalDataProvider();
    gridView = new RealGrid.GridView("ioGrid");
    gridView.setDataSource(dataProvider);

    dataProvider.setFields([
        { fieldName: "warehouseIoId", dataType: "number" },
        { fieldName: "warehouseName" },
        { fieldName: "productCode" },
        { fieldName: "productName" },
        { fieldName: "productCategory" },
        { fieldName: "inQty", dataType: "number" },
        { fieldName: "outQty", dataType: "number" },
        { fieldName: "currentStock", dataType: "number" },
        { fieldName: "clientName" },
        { fieldName: "status" }
    ]);

    gridView.setColumns([
        { name: "warehouseIoId", fieldName: "warehouseIoId", header: { text: "WAREHOUSE_IO_ID" }, width: 110 },
        { name: "warehouseName", fieldName: "warehouseName", header: { text: "WAREHOUSE_NAME" }, width: 130 },
        { name: "productCode", fieldName: "productCode", header: { text: "PRODUCT_CODE" }, width: 110 },
        { name: "productName", fieldName: "productName", header: { text: "PRODUCT_NAME" }, width: 120 },
        { name: "productCategory", fieldName: "productCategory", header: { text: "PRODUCT_CATEGORY" }, width: 120 },
        { name: "inQty", fieldName: "inQty", header: { text: "IN_QTY" }, width: 90 },
        { name: "outQty", fieldName: "outQty", header: { text: "OUT_QTY" }, width: 90 },
        { name: "currentStock", fieldName: "currentStock", header: { text: "CURRENT_STOCK" }, width: 120 },
        { name: "clientName", fieldName: "clientName", header: { text: "CLIENT_NAME" }, width: 120 },
        { name: "status", fieldName: "status", header: { text: "STATUS" }, width: 90 },
        { name: "deleteBtn", header: { text: "삭제" }, width: 70, renderer: { type: "button" } }
    ]);

    gridView.onCellClicked = function (grid, clickData) {
        // 삭제 버튼이 아닌 셀 클릭 시 상세조회 API를 호출한다.
        if (!clickData || clickData.dataRow < 0) return;
        if (clickData.column === "deleteBtn") return;
        var item = dataProvider.getJsonRow(clickData.dataRow);
        if (item && item.warehouseIoId) {
            openDetailModal(item.warehouseIoId);
        }
    };

    gridView.onCellButtonClicked = function (grid, index, column) {
        if (column === "deleteBtn") {
            var row = dataProvider.getJsonRow(index.dataRow);
            if (row && row.warehouseIoId) {
                deleteRow(row.warehouseIoId);
            }
        }
    };
}

function loadList() {
    // 목록 API 호출 및 렌더링 진입점.
    $.ajax({
        url: "/warehouse",
        method: "GET",
        data: { action: "list" },
        success: function (response) {
            renderGrid(response.data || []);
        },
        error: function () {
            alert("목록 조회 실패");
        }
    });
}

function renderGrid(items) {
    if (isFallbackMode) {
        renderFallbackTable(items);
        return;
    }
    if (!dataProvider) {
        return;
    }
    dataProvider.setRows(items);
}

function renderFallbackTable(items) {
    // RealGrid 미사용 시 동작하는 일반 테이블 렌더러.
    var $tbody = $("#ioFallbackTable tbody");
    $tbody.empty();

    if (!items.length) {
        $tbody.append("<tr><td colspan='11'>데이터가 없습니다.</td></tr>");
        return;
    }

    $.each(items, function (_, item) {
        var row = ""
            + "<tr data-id='" + nvl(item.warehouseIoId) + "'>"
            + "<td>" + nvl(item.warehouseIoId) + "</td>"
            + "<td>" + nvl(item.warehouseName) + "</td>"
            + "<td>" + nvl(item.productCode) + "</td>"
            + "<td>" + nvl(item.productName) + "</td>"
            + "<td>" + nvl(item.productCategory) + "</td>"
            + "<td>" + nvl(item.inQty) + "</td>"
            + "<td>" + nvl(item.outQty) + "</td>"
            + "<td>" + nvl(item.currentStock) + "</td>"
            + "<td>" + nvl(item.clientName) + "</td>"
            + "<td>" + nvl(item.status) + "</td>"
            + "<td><button type='button' class='delete-btn' data-id='" + nvl(item.warehouseIoId) + "'>삭제</button></td>"
            + "</tr>";
        $tbody.append(row);
    });
}

function createEmptyRowAndOpenModal() {
    // 레거시 등록 흐름: 빈 행을 먼저 만들고 필드를 개별 수정한다.
    $.ajax({
        url: "/warehouse",
        method: "GET",
        data: { action: "createEmptyRow" },
        success: function (response) {
            var id = response.warehouseIoId;
            if (!id) {
                alert("빈 row 생성 실패");
                return;
            }
            openDetailModal(id);
            loadList();
        },
        error: function () {
            alert("신규등록 실패");
        }
    });
}

function openDetailModal(id) {
    // 상세 API 조회 후 모달 데이터를 채운다.
    $.ajax({
        url: "/warehouse",
        method: "GET",
        data: { action: "detail", id: id },
        success: function (response) {
            setFormData(response.data || {});
            $("#detailModal").show();
        },
        error: function () {
            alert("상세 조회 실패");
        }
    });
}

function setFormData(data) {
    // 상세 응답 payload를 모달 input에 바인딩한다.
    $("#WAREHOUSE_IO_ID").val(nvl(data.warehouseIoId));
    $("#WAREHOUSE_NAME").val(nvl(data.warehouseName));
    $("#PRODUCT_CODE").val(nvl(data.productCode));
    $("#PRODUCT_NAME").val(nvl(data.productName));
    $("#PRODUCT_CATEGORY").val(nvl(data.productCategory));
    $("#IN_QTY").val(nvl(data.inQty));
    $("#OUT_QTY").val(nvl(data.outQty));
    $("#CURRENT_STOCK").val(nvl(data.currentStock));
    $("#CLIENT_NAME").val(nvl(data.clientName));
    $("#MANAGER_NAME").val(nvl(data.managerName));
    $("#MANAGER_PHONE").val(nvl(data.managerPhone));
    $("#IO_DATE").val(nvl(data.ioDate));
    $("#STATUS").val(nvl(data.status));
    $("#MEMO").val(nvl(data.memo));
}

function updateField(action, key, value) {
    // 레거시 패턴: 필드 변경마다 독립적인 update API를 호출한다.
    var id = $("#WAREHOUSE_IO_ID").val();
    var payload = { action: action, id: id };
    payload[key] = value;

    $.ajax({
        url: "/warehouse",
        method: "GET",
        data: payload,
        success: function () {
            patchGridRow(id, key, value);
        },
        error: function () {
            alert("필드 저장 실패: " + action);
        }
    });
}

var GRID_FIELD_COLUMN_INDEX = {
    warehouseName: 1,
    productCode: 2,
    productName: 3,
    productCategory: 4,
    inQty: 5,
    outQty: 6,
    currentStock: 7,
    clientName: 8,
    status: 9
};

var NUMERIC_GRID_FIELDS = { inQty: true, outQty: true, currentStock: true };

function patchGridRow(id, fieldKey, value) {
    if (!GRID_FIELD_COLUMN_INDEX.hasOwnProperty(fieldKey)) {
        return;
    }

    if (isFallbackMode) {
        patchFallbackRow(id, fieldKey, value);
        return;
    }

    if (!dataProvider) {
        return;
    }

    var parsed = NUMERIC_GRID_FIELDS[fieldKey] ? (parseInt(value, 10) || 0) : value;
    var rowCount = dataProvider.getRowCount();
    for (var i = 0; i < rowCount; i++) {
        var rowId = dataProvider.getValue(i, "warehouseIoId");
        if (String(rowId) === String(id)) {
            dataProvider.setValue(i, fieldKey, parsed);
            break;
        }
    }
}

function patchFallbackRow(id, fieldKey, value) {
    var colIndex = GRID_FIELD_COLUMN_INDEX[fieldKey];
    var $row = $("#ioFallbackTable tr[data-id='" + id + "']");
    if (!$row.length) {
        return;
    }
    $row.find("td").eq(colIndex).text(value);
}

function deleteRow(id) {
    // 삭제 후 목록을 다시 읽어 UI를 동기화한다.
    if (!confirm("삭제하시겠습니까?")) {
        return;
    }
    $.ajax({
        url: "/warehouse",
        method: "GET",
        data: { action: "delete", id: id },
        success: function () {
            loadList();
        },
        error: function () {
            alert("삭제 실패");
        }
    });
}

function closeModal() {
    $("#detailModal").hide();
}

function nvl(value) {
    return value === null || value === undefined ? "" : value;
}
