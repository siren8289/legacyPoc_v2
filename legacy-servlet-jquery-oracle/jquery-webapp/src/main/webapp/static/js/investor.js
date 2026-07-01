// 투자자 화면 컨트롤러:
// - 목록 조회/검색
// - 행 클릭 상세조회
// - 모달 기반 신규 등록
$(document).ready(function () {
    bindInvestorEvents();
    loadInvestorList();
});

function bindInvestorEvents() {
    // 조회 버튼 동작
    $("#searchBtn").on("click", loadInvestorList);
    $("#createInvestorBtn").on("click", function () {
        $("#investorModal").show();
    });
    $("#closeInvestorModalBtn").on("click", function () {
        $("#investorModal").hide();
    });
    $("#saveInvestorBtn").on("click", createInvestor);

    $("#investorTable").on("click", "tr[data-id]", function () {
        // 행 클릭 시 상세 조회 후 모달을 연다.
        var id = $(this).data("id");
        loadInvestorDetail(id);
    });
}

function loadInvestorList() {
    // 투자자 목록 API 호출.
    $.ajax({
        url: "/investor",
        method: "GET",
        data: { action: "list", name: $("#investorName").val() },
        success: function (response) {
            renderInvestorRows(response.data || []);
        },
        error: function () {
            alert("투자자 조회 실패");
        }
    });
}

function renderInvestorRows(items) {
    // 테이블 수동 렌더링(레거시/jQuery 방식).
    var $tbody = $("#investorTable tbody");
    $tbody.empty();
    if (!items.length) {
        $tbody.append("<tr><td colspan='6'>데이터가 없습니다.</td></tr>");
        return;
    }
    $.each(items, function (_, item) {
        var row = ""
            + "<tr data-id='" + nvl(item.investorId) + "'>"
            + "<td>" + nvl(item.investorId) + "</td>"
            + "<td>" + nvl(item.investorName) + "</td>"
            + "<td>" + nvl(item.investorGrade) + "</td>"
            + "<td>" + nvl(item.totalAmount) + "</td>"
            + "<td>" + nvl(item.lastProductName) + "</td>"
            + "<td>" + nvl(item.screenMemo) + "</td>"
            + "</tr>";
        $tbody.append(row);
    });
}

function createInvestor() {
    // 모달 입력값으로 신규 등록 요청을 보낸다.
    $.ajax({
        url: "/investor",
        method: "GET",
        data: {
            action: "create",
            investorName: $("#mInvestorName").val(),
            investorGrade: $("#mInvestorGrade").val(),
            totalAmount: $("#mTotalAmount").val(),
            lastProductName: $("#mLastProductName").val(),
            screenMemo: $("#mScreenMemo").val()
        },
        success: function () {
            $("#investorModal").hide();
            clearInvestorModal();
            loadInvestorList();
        },
        error: function () {
            alert("투자자 등록 실패");
        }
    });
}

function loadInvestorDetail(id) {
    // 상세 API 응답으로 모달 입력값을 채운다.
    $.ajax({
        url: "/investor",
        method: "GET",
        data: { action: "detail", id: id },
        success: function (response) {
            var data = response.data || {};
            $("#mInvestorName").val(nvl(data.investorName));
            $("#mInvestorGrade").val(nvl(data.investorGrade));
            $("#mTotalAmount").val(nvl(data.totalAmount));
            $("#mLastProductName").val(nvl(data.lastProductName));
            $("#mScreenMemo").val(nvl(data.screenMemo));
            $("#investorModal").show();
        },
        error: function () {
            alert("투자자 상세조회 실패");
        }
    });
}

function clearInvestorModal() {
    // 등록 성공 후 모달 입력 상태를 초기화한다.
    $("#mInvestorName").val("");
    $("#mInvestorGrade").val("");
    $("#mTotalAmount").val("");
    $("#mLastProductName").val("");
    $("#mScreenMemo").val("");
}

function nvl(value) {
    return value === null || value === undefined ? "" : value;
}
