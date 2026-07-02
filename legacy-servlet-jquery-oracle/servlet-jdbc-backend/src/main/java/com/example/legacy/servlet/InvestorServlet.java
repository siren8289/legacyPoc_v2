package com.example.legacy.servlet;

import com.example.legacy.dao.InvestorDao;
import com.example.legacy.dto.InvestorDto;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 투자자 action API 처리기.
 * /investor?action=list|detail|create 형태로 호출된다.
 */
public class InvestorServlet {

    private final InvestorDao investorDao = new InvestorDao();
    private final Map<String, Function<Map<String, String>, String>> actions = buildActions();

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

    private Map<String, Function<Map<String, String>, String>> buildActions() {
        Map<String, Function<Map<String, String>, String>> map = new HashMap<>();
        map.put("list", p -> LegacyJsonWriter.investorList(investorDao.list(p.get("name"))));
        map.put("detail", p -> {
            InvestorDto dto = investorDao.detail(toLong(p.get("id")));
            return "{\"data\":" + LegacyJsonWriter.investorDetail(dto) + "}";
        });
        map.put("create", p -> {
            long id = investorDao.create(
                    p.get("investorName"),
                    p.get("investorGrade"),
                    toLong(p.get("totalAmount")),
                    p.get("lastProductName"),
                    p.get("screenMemo")
            );
            return "{\"result\":\"OK\",\"investorId\":" + id + "}";
        });
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
}
