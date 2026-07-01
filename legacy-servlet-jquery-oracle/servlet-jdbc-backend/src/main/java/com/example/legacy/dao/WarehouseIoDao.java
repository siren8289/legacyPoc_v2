package com.example.legacy.dao;

import com.example.legacy.dto.WarehouseIoDto;
import com.example.legacy.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WarehouseIoDao {

    private final DbConnection dbConnection = new DbConnection();
    // DB 드라이버/스키마가 없는 환경에서도 동작하도록 메모리 대체 저장소를 사용한다.
    private static final Map<Long, WarehouseIoDto> MEMORY_TABLE = new LinkedHashMap<>();
    private static long MEMORY_SEQ = 1000L;

    static {
        WarehouseIoDto row1 = new WarehouseIoDto();
        row1.setWarehouseIoId(nextId());
        row1.setWarehouseName("서울1창고");
        row1.setProductCode("P-100");
        row1.setProductName("산업용 센서");
        row1.setProductCategory("전자부품");
        row1.setInQty(140);
        row1.setOutQty(30);
        row1.setCurrentStock(110);
        row1.setClientName("한빛유통");
        row1.setManagerName("김대리");
        row1.setManagerPhone("010-1234-5678");
        row1.setIoDate("2026-04-27");
        row1.setStatus("ACTIVE");
        row1.setMemo("우선 출고 요청");
        row1.setDeletedYn("N");
        MEMORY_TABLE.put(row1.getWarehouseIoId(), row1);

        WarehouseIoDto row2 = new WarehouseIoDto();
        row2.setWarehouseIoId(nextId());
        row2.setWarehouseName("부산2창고");
        row2.setProductCode("P-220");
        row2.setProductName("모터 모듈");
        row2.setProductCategory("기계부품");
        row2.setInQty(90);
        row2.setOutQty(12);
        row2.setCurrentStock(78);
        row2.setClientName("남해물산");
        row2.setManagerName("이과장");
        row2.setManagerPhone("010-2345-6789");
        row2.setIoDate("2026-04-26");
        row2.setStatus("ACTIVE");
        row2.setMemo("특이사항 없음");
        row2.setDeletedYn("N");
        MEMORY_TABLE.put(row2.getWarehouseIoId(), row2);
    }

    public List<WarehouseIoDto> list() {
        // 기본 경로: 비정규화 화면 테이블에서 조회한다.
        List<WarehouseIoDto> result = new ArrayList<>();
        String sql = "SELECT WAREHOUSE_IO_ID, WAREHOUSE_NAME, PRODUCT_CODE, PRODUCT_NAME, PRODUCT_CATEGORY, " +
                "IN_QTY, OUT_QTY, CURRENT_STOCK, CLIENT_NAME, MANAGER_NAME, MANAGER_PHONE, IO_DATE, STATUS, MEMO, DELETED_YN " +
                "FROM TB_WAREHOUSE_IO_SCREEN WHERE DELETED_YN = 'N' ORDER BY WAREHOUSE_IO_ID DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            // 대체 경로: DB 초기화 실패 시에도 UI가 동작하도록 메모리 데이터를 반환한다.
            return listMemory();
        }
        return result;
    }

    public WarehouseIoDto detail(long id) {
        // 상세 조회: 목록 행 클릭 시 모달 데이터 로딩에 사용된다.
        String sql = "SELECT WAREHOUSE_IO_ID, WAREHOUSE_NAME, PRODUCT_CODE, PRODUCT_NAME, PRODUCT_CATEGORY, " +
                "IN_QTY, OUT_QTY, CURRENT_STOCK, CLIENT_NAME, MANAGER_NAME, MANAGER_PHONE, IO_DATE, STATUS, MEMO, DELETED_YN " +
                "FROM TB_WAREHOUSE_IO_SCREEN WHERE WAREHOUSE_IO_ID = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            // 메모리 저장소에서도 동일한 상세 조회 동작을 수행한다.
            WarehouseIoDto dto = MEMORY_TABLE.get(id);
            return dto == null ? null : cloneDto(dto);
        }
    }

    public long createEmptyRow() {
        // 레거시 등록 흐름: 빈 행 생성 후 각 필드를 개별 API로 업데이트한다.
        String sql = "INSERT INTO TB_WAREHOUSE_IO_SCREEN (" +
                "WAREHOUSE_NAME, PRODUCT_CODE, PRODUCT_NAME, PRODUCT_CATEGORY, IN_QTY, OUT_QTY, CURRENT_STOCK, " +
                "CLIENT_NAME, MANAGER_NAME, MANAGER_PHONE, IO_DATE, STATUS, MEMO, DELETED_YN, UPDATED_AT" +
                ") VALUES ('', '', '', '', 0, 0, 0, '', '', '', '', 'DRAFT', '', 'N', CURRENT_TIMESTAMP)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            return -1L;
        } catch (SQLException e) {
            // 대체 경로: 메모리 저장소에 DRAFT 행을 생성하고 ID를 반환한다.
            WarehouseIoDto dto = new WarehouseIoDto();
            dto.setWarehouseIoId(nextId());
            dto.setStatus("DRAFT");
            dto.setDeletedYn("N");
            MEMORY_TABLE.put(dto.getWarehouseIoId(), dto);
            return dto.getWarehouseIoId();
        }
    }

    public int updateWarehouseName(long id, String value) { return updateStringColumn(id, "WAREHOUSE_NAME", value); }
    public int updateProductCode(long id, String value) { return updateStringColumn(id, "PRODUCT_CODE", value); }
    public int updateProductName(long id, String value) { return updateStringColumn(id, "PRODUCT_NAME", value); }
    public int updateProductCategory(long id, String value) { return updateStringColumn(id, "PRODUCT_CATEGORY", value); }
    public int updateInQty(long id, int value) { return updateIntColumn(id, "IN_QTY", value); }
    public int updateOutQty(long id, int value) { return updateIntColumn(id, "OUT_QTY", value); }
    public int updateCurrentStock(long id, int value) { return updateIntColumn(id, "CURRENT_STOCK", value); }
    public int updateClientName(long id, String value) { return updateStringColumn(id, "CLIENT_NAME", value); }
    public int updateManagerName(long id, String value) { return updateStringColumn(id, "MANAGER_NAME", value); }
    public int updateManagerPhone(long id, String value) { return updateStringColumn(id, "MANAGER_PHONE", value); }
    public int updateIoDate(long id, String value) { return updateStringColumn(id, "IO_DATE", value); }
    public int updateStatus(long id, String value) { return updateStringColumn(id, "STATUS", value); }
    public int updateMemo(long id, String value) { return updateStringColumn(id, "MEMO", value); }

    public int delete(long id) {
        // 이력 보존 성격을 위해 물리 삭제 대신 소프트 삭제를 사용한다.
        String sql = "UPDATE TB_WAREHOUSE_IO_SCREEN SET DELETED_YN = 'Y', UPDATED_AT = CURRENT_TIMESTAMP WHERE WAREHOUSE_IO_ID = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            WarehouseIoDto dto = MEMORY_TABLE.get(id);
            if (dto == null) return 0;
            dto.setDeletedYn("Y");
            return 1;
        }
    }

    private int updateStringColumn(long id, String column, String value) {
        // 문자열 컬럼 단위 업데이트(입력 필드 변경마다 1회 호출).
        String sql = "UPDATE TB_WAREHOUSE_IO_SCREEN SET " + column + " = ?, UPDATED_AT = CURRENT_TIMESTAMP WHERE WAREHOUSE_IO_ID = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setLong(2, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            WarehouseIoDto dto = MEMORY_TABLE.get(id);
            if (dto == null) return 0;
            applyString(dto, column, value);
            return 1;
        }
    }

    private int updateIntColumn(long id, String column, int value) {
        // 숫자 컬럼용 필드 단위 업데이트.
        String sql = "UPDATE TB_WAREHOUSE_IO_SCREEN SET " + column + " = ?, UPDATED_AT = CURRENT_TIMESTAMP WHERE WAREHOUSE_IO_ID = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, value);
            ps.setLong(2, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            WarehouseIoDto dto = MEMORY_TABLE.get(id);
            if (dto == null) return 0;
            applyInt(dto, column, value);
            return 1;
        }
    }

    private void applyString(WarehouseIoDto dto, String column, String value) {
        if ("WAREHOUSE_NAME".equals(column)) dto.setWarehouseName(value);
        else if ("PRODUCT_CODE".equals(column)) dto.setProductCode(value);
        else if ("PRODUCT_NAME".equals(column)) dto.setProductName(value);
        else if ("PRODUCT_CATEGORY".equals(column)) dto.setProductCategory(value);
        else if ("CLIENT_NAME".equals(column)) dto.setClientName(value);
        else if ("MANAGER_NAME".equals(column)) dto.setManagerName(value);
        else if ("MANAGER_PHONE".equals(column)) dto.setManagerPhone(value);
        else if ("IO_DATE".equals(column)) dto.setIoDate(value);
        else if ("STATUS".equals(column)) dto.setStatus(value);
        else if ("MEMO".equals(column)) dto.setMemo(value);
    }

    private void applyInt(WarehouseIoDto dto, String column, int value) {
        if ("IN_QTY".equals(column)) dto.setInQty(value);
        else if ("OUT_QTY".equals(column)) dto.setOutQty(value);
        else if ("CURRENT_STOCK".equals(column)) dto.setCurrentStock(value);
    }

    private List<WarehouseIoDto> listMemory() {
        // 대체 모드에서도 소프트 삭제된 행은 제외한다.
        List<WarehouseIoDto> rows = new ArrayList<>();
        for (WarehouseIoDto dto : MEMORY_TABLE.values()) {
            if (!"Y".equals(dto.getDeletedYn())) {
                rows.add(cloneDto(dto));
            }
        }
        return rows;
    }

    private WarehouseIoDto mapRow(ResultSet rs) throws SQLException {
        // ResultSet을 DTO로 매핑하며 DB 컬럼 의미를 그대로 유지한다.
        WarehouseIoDto dto = new WarehouseIoDto();
        dto.setWarehouseIoId(rs.getLong("WAREHOUSE_IO_ID"));
        dto.setWarehouseName(rs.getString("WAREHOUSE_NAME"));
        dto.setProductCode(rs.getString("PRODUCT_CODE"));
        dto.setProductName(rs.getString("PRODUCT_NAME"));
        dto.setProductCategory(rs.getString("PRODUCT_CATEGORY"));
        dto.setInQty(rs.getInt("IN_QTY"));
        dto.setOutQty(rs.getInt("OUT_QTY"));
        dto.setCurrentStock(rs.getInt("CURRENT_STOCK"));
        dto.setClientName(rs.getString("CLIENT_NAME"));
        dto.setManagerName(rs.getString("MANAGER_NAME"));
        dto.setManagerPhone(rs.getString("MANAGER_PHONE"));
        dto.setIoDate(rs.getString("IO_DATE"));
        dto.setStatus(rs.getString("STATUS"));
        dto.setMemo(rs.getString("MEMO"));
        dto.setDeletedYn(rs.getString("DELETED_YN"));
        return dto;
    }

    private WarehouseIoDto cloneDto(WarehouseIoDto src) {
        // 호출 측이 메모리 원본을 직접 변경하지 못하도록 복사본을 반환한다.
        WarehouseIoDto dto = new WarehouseIoDto();
        dto.setWarehouseIoId(src.getWarehouseIoId());
        dto.setWarehouseName(src.getWarehouseName());
        dto.setProductCode(src.getProductCode());
        dto.setProductName(src.getProductName());
        dto.setProductCategory(src.getProductCategory());
        dto.setInQty(src.getInQty());
        dto.setOutQty(src.getOutQty());
        dto.setCurrentStock(src.getCurrentStock());
        dto.setClientName(src.getClientName());
        dto.setManagerName(src.getManagerName());
        dto.setManagerPhone(src.getManagerPhone());
        dto.setIoDate(src.getIoDate());
        dto.setStatus(src.getStatus());
        dto.setMemo(src.getMemo());
        dto.setDeletedYn(src.getDeletedYn());
        return dto;
    }

    private static synchronized long nextId() {
        MEMORY_SEQ++;
        return MEMORY_SEQ;
    }
}
