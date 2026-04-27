package com.example.legacy.servlet;

import com.example.legacy.dao.WarehouseIoDao;
import com.example.legacy.dto.WarehouseIoDto;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.List;

public class WarehouseIoServlet extends HttpServlet {

    // 레거시 스타일: Servlet이 DAO를 직접 호출한다(서비스 계층 의도적으로 생략).
    private final WarehouseIoDao warehouseIoDao = new WarehouseIoDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 모든 동작은 쿼리 파라미터 action 값으로 선택한다.
        String action = req.getParameter("action");
        String body = processAction(action, new RequestReader(req));
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(body);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doGet(req, resp);
    }

    public String processAction(String action, Map<String, String> params) {
        // 최소 파라미터로도 목록 화면이 열리도록 기본 action은 list로 둔다.
        if (action == null || action.isEmpty()) {
            action = "list";
        }

        // 유지보수 어려운 레거시 스타일을 재현하기 위해 긴 분기 체인을 유지한다.
        if ("list".equals(action)) {
            return toJsonList(warehouseIoDao.list());
        } else if ("detail".equals(action)) {
            long id = toLong(params.get("id"));
            WarehouseIoDto dto = warehouseIoDao.detail(id);
            return "{\"data\":" + toJsonDetail(dto) + "}";
        } else if ("createEmptyRow".equals(action)) {
            long id = warehouseIoDao.createEmptyRow();
            return "{\"result\":\"OK\",\"warehouseIoId\":" + id + "}";
        } else if ("updateWarehouseName".equals(action)) {
            return ok(warehouseIoDao.updateWarehouseName(toLong(params.get("id")), params.get("warehouseName")));
        } else if ("updateProductCode".equals(action)) {
            return ok(warehouseIoDao.updateProductCode(toLong(params.get("id")), params.get("productCode")));
        } else if ("updateProductName".equals(action)) {
            return ok(warehouseIoDao.updateProductName(toLong(params.get("id")), params.get("productName")));
        } else if ("updateProductCategory".equals(action)) {
            return ok(warehouseIoDao.updateProductCategory(toLong(params.get("id")), params.get("productCategory")));
        } else if ("updateInQty".equals(action)) {
            return ok(warehouseIoDao.updateInQty(toLong(params.get("id")), toInt(params.get("inQty"))));
        } else if ("updateOutQty".equals(action)) {
            return ok(warehouseIoDao.updateOutQty(toLong(params.get("id")), toInt(params.get("outQty"))));
        } else if ("updateCurrentStock".equals(action)) {
            return ok(warehouseIoDao.updateCurrentStock(toLong(params.get("id")), toInt(params.get("currentStock"))));
        } else if ("updateClientName".equals(action)) {
            return ok(warehouseIoDao.updateClientName(toLong(params.get("id")), params.get("clientName")));
        } else if ("updateManagerName".equals(action)) {
            return ok(warehouseIoDao.updateManagerName(toLong(params.get("id")), params.get("managerName")));
        } else if ("updateManagerPhone".equals(action)) {
            return ok(warehouseIoDao.updateManagerPhone(toLong(params.get("id")), params.get("managerPhone")));
        } else if ("updateIoDate".equals(action)) {
            return ok(warehouseIoDao.updateIoDate(toLong(params.get("id")), params.get("ioDate")));
        } else if ("updateStatus".equals(action)) {
            return ok(warehouseIoDao.updateStatus(toLong(params.get("id")), params.get("status")));
        } else if ("updateMemo".equals(action)) {
            return ok(warehouseIoDao.updateMemo(toLong(params.get("id")), params.get("memo")));
        } else if ("delete".equals(action)) {
            return ok(warehouseIoDao.delete(toLong(params.get("id"))));
        }

        return "{\"result\":\"FAIL\",\"message\":\"Unknown action\"}";
    }

    private String toJsonList(List<WarehouseIoDto> data) {
        // 수동 JSON 문자열 조합(레거시 스타일, object mapper 미사용).
        StringBuilder sb = new StringBuilder();
        sb.append("{\"data\":[");

        for (int i = 0; i < data.size(); i++) {
            sb.append(toJsonDetail(data.get(i)));
            if (i < data.size() - 1) {
                sb.append(",");
            }
        }

        sb.append("]}");
        return sb.toString();
    }

    private String toJsonDetail(WarehouseIoDto dto) {
        if (dto == null) {
            return "null";
        }
        return "{" +
                "\"warehouseIoId\":" + dto.getWarehouseIoId() + "," +
                "\"warehouseName\":\"" + escape(dto.getWarehouseName()) + "\"," +
                "\"productCode\":\"" + escape(dto.getProductCode()) + "\"," +
                "\"productName\":\"" + escape(dto.getProductName()) + "\"," +
                "\"productCategory\":\"" + escape(dto.getProductCategory()) + "\"," +
                "\"inQty\":" + dto.getInQty() + "," +
                "\"outQty\":" + dto.getOutQty() + "," +
                "\"currentStock\":" + dto.getCurrentStock() + "," +
                "\"clientName\":\"" + escape(dto.getClientName()) + "\"," +
                "\"managerName\":\"" + escape(dto.getManagerName()) + "\"," +
                "\"managerPhone\":\"" + escape(dto.getManagerPhone()) + "\"," +
                "\"ioDate\":\"" + escape(dto.getIoDate()) + "\"," +
                "\"status\":\"" + escape(dto.getStatus()) + "\"," +
                "\"memo\":\"" + escape(dto.getMemo()) + "\"" +
                "}";
    }

    private String ok(int updated) {
        // 필드 단위 업데이트 응답 포맷을 통일한다.
        return "{\"result\":\"OK\",\"updated\":" + updated + "}";
    }

    private long toLong(String value) {
        if (value == null || value.trim().isEmpty()) return 0L;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private int toInt(String value) {
        if (value == null || value.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String escape(String value) {
        // 수동 JSON 문자열의 유효성을 위한 최소 이스케이프 처리.
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\\\"");
    }

    private static class RequestReader implements Map<String, String> {
        // HttpServletRequest 파라미터 조회를 Map#get 형태로 감싸는 어댑터.
        private final HttpServletRequest req;

        private RequestReader(HttpServletRequest req) {
            this.req = req;
        }

        @Override
        public String get(Object key) {
            if (key == null) return null;
            return req.getParameter(String.valueOf(key));
        }

        // 이 레거시 샘플에 필요한 최소 Map 동작만 구현.
        @Override public int size() { return 0; }
        @Override public boolean isEmpty() { return false; }
        @Override public boolean containsKey(Object key) { return false; }
        @Override public boolean containsValue(Object value) { return false; }
        @Override public String put(String key, String value) { throw new UnsupportedOperationException(); }
        @Override public String remove(Object key) { throw new UnsupportedOperationException(); }
        @Override public void putAll(Map<? extends String, ? extends String> m) { throw new UnsupportedOperationException(); }
        @Override public void clear() { throw new UnsupportedOperationException(); }
        @Override public java.util.Set<String> keySet() { throw new UnsupportedOperationException(); }
        @Override public java.util.Collection<String> values() { throw new UnsupportedOperationException(); }
        @Override public java.util.Set<Entry<String, String>> entrySet() { throw new UnsupportedOperationException(); }
    }
}
