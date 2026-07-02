package com.example.legacy.servlet;

import com.example.legacy.dao.WarehouseIoDao;
import com.example.legacy.dto.WarehouseIoDto;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 입출고 action API 서블릿.
 * 서비스 계층 없이 DAO를 직접 호출하며, 쿼리 파라미터 action으로 동작을 분기한다.
 */
public class WarehouseIoServlet extends HttpServlet {

    private final WarehouseIoDao warehouseIoDao = new WarehouseIoDao();
    private final Map<String, Function<Map<String, String>, String>> actions = buildActions();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String body = processAction(req.getParameter("action"), new RequestParamReader(req));
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(body);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doGet(req, resp);
    }

    public String processAction(String action, Map<String, String> params) {
        if (action == null || action.isEmpty()) {
            action = "list";
        }
        Function<Map<String, String>, String> handler = actions.get(action);
        if (handler == null) {
            return "{\"result\":\"FAIL\",\"message\":\"Unknown action\"}";
        }
        return handler.apply(params);
    }

    // action 이름 → 핸들러 매핑. 프론트 warehouse-io.js의 API 호출과 1:1 대응한다.
    private Map<String, Function<Map<String, String>, String>> buildActions() {
        Map<String, Function<Map<String, String>, String>> map = new HashMap<>();
        map.put("list", p -> LegacyJsonWriter.warehouseList(warehouseIoDao.list()));
        map.put("detail", p -> {
            WarehouseIoDto dto = warehouseIoDao.detail(toLong(p.get("id")));
            return "{\"data\":" + LegacyJsonWriter.warehouseDetail(dto) + "}";
        });
        map.put("createEmptyRow", p -> {
            long id = warehouseIoDao.createEmptyRow();
            return "{\"result\":\"OK\",\"warehouseIoId\":" + id + "}";
        });
        map.put("updateWarehouseName", p -> LegacyJsonWriter.ok(
                warehouseIoDao.updateWarehouseName(toLong(p.get("id")), p.get("warehouseName"))));
        map.put("updateProductCode", p -> LegacyJsonWriter.ok(
                warehouseIoDao.updateProductCode(toLong(p.get("id")), p.get("productCode"))));
        map.put("updateProductName", p -> LegacyJsonWriter.ok(
                warehouseIoDao.updateProductName(toLong(p.get("id")), p.get("productName"))));
        map.put("updateProductCategory", p -> LegacyJsonWriter.ok(
                warehouseIoDao.updateProductCategory(toLong(p.get("id")), p.get("productCategory"))));
        map.put("updateInQty", p -> LegacyJsonWriter.ok(
                warehouseIoDao.updateInQty(toLong(p.get("id")), toInt(p.get("inQty")))));
        map.put("updateOutQty", p -> LegacyJsonWriter.ok(
                warehouseIoDao.updateOutQty(toLong(p.get("id")), toInt(p.get("outQty")))));
        map.put("updateCurrentStock", p -> LegacyJsonWriter.ok(
                warehouseIoDao.updateCurrentStock(toLong(p.get("id")), toInt(p.get("currentStock")))));
        map.put("updateClientName", p -> LegacyJsonWriter.ok(
                warehouseIoDao.updateClientName(toLong(p.get("id")), p.get("clientName"))));
        map.put("updateManagerName", p -> LegacyJsonWriter.ok(
                warehouseIoDao.updateManagerName(toLong(p.get("id")), p.get("managerName"))));
        map.put("updateManagerPhone", p -> LegacyJsonWriter.ok(
                warehouseIoDao.updateManagerPhone(toLong(p.get("id")), p.get("managerPhone"))));
        map.put("updateIoDate", p -> LegacyJsonWriter.ok(
                warehouseIoDao.updateIoDate(toLong(p.get("id")), p.get("ioDate"))));
        map.put("updateStatus", p -> LegacyJsonWriter.ok(
                warehouseIoDao.updateStatus(toLong(p.get("id")), p.get("status"))));
        map.put("updateMemo", p -> LegacyJsonWriter.ok(
                warehouseIoDao.updateMemo(toLong(p.get("id")), p.get("memo"))));
        map.put("delete", p -> LegacyJsonWriter.ok(warehouseIoDao.delete(toLong(p.get("id")))));
        return map;
    }

    private long toLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private int toInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
