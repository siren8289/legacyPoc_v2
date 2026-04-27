package com.example.legacy.servlet;

import com.example.legacy.dao.InvestorDao;
import com.example.legacy.dto.InvestorDto;

import java.util.List;
import java.util.Map;

public class InvestorServlet {
    // /investor 엔드포인트에서 사용하는 투자자 action 처리기.
    private final InvestorDao investorDao = new InvestorDao();

    public String processAction(String action, Map<String, String> params) {
        // action이 없으면 기본값 list로 처리해 화면 초기 진입을 단순화한다.
        if (action == null || action.isEmpty()) {
            action = "list";
        }

        // warehouse 흐름과 비교 가능하도록 레거시 action 분기 스타일을 유지한다.
        if ("list".equals(action)) {
            return toJsonList(investorDao.list(params.get("name")));
        } else if ("detail".equals(action)) {
            InvestorDto dto = investorDao.detail(toLong(params.get("id")));
            return "{\"data\":" + toJson(dto) + "}";
        } else if ("create".equals(action)) {
            long id = investorDao.create(
                    params.get("investorName"),
                    params.get("investorGrade"),
                    toLong(params.get("totalAmount")),
                    params.get("lastProductName"),
                    params.get("screenMemo")
            );
            return "{\"result\":\"OK\",\"investorId\":" + id + "}";
        }
        return "{\"result\":\"FAIL\",\"message\":\"Unknown action\"}";
    }

    private String toJsonList(List<InvestorDto> data) {
        // 레거시 방식에 맞춰 JSON 문자열을 수동 생성한다.
        StringBuilder sb = new StringBuilder();
        sb.append("{\"data\":[");
        for (int i = 0; i < data.size(); i++) {
            sb.append(toJson(data.get(i)));
            if (i < data.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    private String toJson(InvestorDto dto) {
        if (dto == null) return "null";
        return "{"
                + "\"investorId\":" + dto.getInvestorId() + ","
                + "\"investorName\":\"" + escape(dto.getInvestorName()) + "\","
                + "\"investorGrade\":\"" + escape(dto.getInvestorGrade()) + "\","
                + "\"totalAmount\":" + dto.getTotalAmount() + ","
                + "\"lastProductName\":\"" + escape(dto.getLastProductName()) + "\","
                + "\"screenMemo\":\"" + escape(dto.getScreenMemo()) + "\""
                + "}";
    }

    private long toLong(String value) {
        // 쿼리 문자열 숫자 필드를 안전하게 파싱한다.
        if (value == null || value.trim().isEmpty()) return 0L;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String escape(String value) {
        // 수동 JSON 문자열 생성을 위한 최소 이스케이프 처리.
        if (value == null) return "";
        return value.replace("\"", "\\\"");
    }
}
