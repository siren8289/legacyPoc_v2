package com.example.legacy.servlet;

import com.example.legacy.dto.InvestorDto;
import com.example.legacy.dto.WarehouseIoDto;

import java.util.List;

/**
 * 레거시 수동 JSON 직렬화 유틸.
 * Jackson 등 라이브러리 없이 문자열 조합으로 응답을 생성한다.
 */
final class LegacyJsonWriter {

    private LegacyJsonWriter() {
    }

    static String warehouseList(List<WarehouseIoDto> data) {
        StringBuilder sb = new StringBuilder("{\"data\":[");
        for (int i = 0; i < data.size(); i++) {
            sb.append(warehouseDetail(data.get(i)));
            if (i < data.size() - 1) {
                sb.append(",");
            }
        }
        return sb.append("]}").toString();
    }

    static String warehouseDetail(WarehouseIoDto dto) {
        if (dto == null) {
            return "null";
        }
        return "{"
                + "\"warehouseIoId\":" + dto.getWarehouseIoId() + ","
                + "\"warehouseName\":\"" + escape(dto.getWarehouseName()) + "\","
                + "\"productCode\":\"" + escape(dto.getProductCode()) + "\","
                + "\"productName\":\"" + escape(dto.getProductName()) + "\","
                + "\"productCategory\":\"" + escape(dto.getProductCategory()) + "\","
                + "\"inQty\":" + dto.getInQty() + ","
                + "\"outQty\":" + dto.getOutQty() + ","
                + "\"currentStock\":" + dto.getCurrentStock() + ","
                + "\"clientName\":\"" + escape(dto.getClientName()) + "\","
                + "\"managerName\":\"" + escape(dto.getManagerName()) + "\","
                + "\"managerPhone\":\"" + escape(dto.getManagerPhone()) + "\","
                + "\"ioDate\":\"" + escape(dto.getIoDate()) + "\","
                + "\"status\":\"" + escape(dto.getStatus()) + "\","
                + "\"memo\":\"" + escape(dto.getMemo()) + "\""
                + "}";
    }

    static String investorList(List<InvestorDto> data) {
        StringBuilder sb = new StringBuilder("{\"data\":[");
        for (int i = 0; i < data.size(); i++) {
            sb.append(investorDetail(data.get(i)));
            if (i < data.size() - 1) {
                sb.append(",");
            }
        }
        return sb.append("]}").toString();
    }

    static String investorDetail(InvestorDto dto) {
        if (dto == null) {
            return "null";
        }
        return "{"
                + "\"investorId\":" + dto.getInvestorId() + ","
                + "\"investorName\":\"" + escape(dto.getInvestorName()) + "\","
                + "\"investorGrade\":\"" + escape(dto.getInvestorGrade()) + "\","
                + "\"totalAmount\":" + dto.getTotalAmount() + ","
                + "\"lastProductName\":\"" + escape(dto.getLastProductName()) + "\","
                + "\"screenMemo\":\"" + escape(dto.getScreenMemo()) + "\""
                + "}";
    }

    static String ok(int updated) {
        return "{\"result\":\"OK\",\"updated\":" + updated + "}";
    }

    // 수동 JSON 생성 시 최소한의 이스케이프 처리
    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\\\"");
    }
}
