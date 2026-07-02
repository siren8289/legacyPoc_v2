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

/**
 * 입출고 화면 전용 DAO.
 * TB_WAREHOUSE_IO_SCREEN 비정규화 테이블에 직접 JDBC로 접근하며,
 * DB 연결 실패 시 메모리 저장소로 자동 전환한다.
 */
public class WarehouseIoDao {

    private static final String TABLE = "TB_WAREHOUSE_IO_SCREEN";
    private static final String SELECT_COLUMNS =
            "WAREHOUSE_IO_ID, WAREHOUSE_NAME, PRODUCT_CODE, PRODUCT_NAME, PRODUCT_CATEGORY, "
                    + "IN_QTY, OUT_QTY, CURRENT_STOCK, CLIENT_NAME, MANAGER_NAME, MANAGER_PHONE, "
                    + "IO_DATE, STATUS, MEMO, DELETED_YN";
    private static final String SQL_LIST =
            "SELECT " + SELECT_COLUMNS + " FROM " + TABLE + " WHERE DELETED_YN = 'N' ORDER BY WAREHOUSE_IO_ID DESC";
    private static final String SQL_DETAIL =
            "SELECT " + SELECT_COLUMNS + " FROM " + TABLE + " WHERE WAREHOUSE_IO_ID = ?";
    // 레거시 등록 흐름: 빈 행을 먼저 INSERT하고 이후 필드별 UPDATE로 채운다.
    private static final String SQL_INSERT_EMPTY =
            "INSERT INTO " + TABLE + " ("
                    + "WAREHOUSE_NAME, PRODUCT_CODE, PRODUCT_NAME, PRODUCT_CATEGORY, IN_QTY, OUT_QTY, CURRENT_STOCK, "
                    + "CLIENT_NAME, MANAGER_NAME, MANAGER_PHONE, IO_DATE, STATUS, MEMO, DELETED_YN, UPDATED_AT"
                    + ") VALUES ('', '', '', '', 0, 0, 0, '', '', '', '', 'DRAFT', '', 'N', CURRENT_TIMESTAMP)";
    private static final String SQL_SOFT_DELETE =
            "UPDATE " + TABLE + " SET DELETED_YN = 'Y', UPDATED_AT = CURRENT_TIMESTAMP WHERE WAREHOUSE_IO_ID = ?";

    private final DbConnection dbConnection = new DbConnection();
    // DB 미연결 환경에서도 UI 데모가 동작하도록 하는 메모리 대체 저장소
    private static final Map<Long, WarehouseIoDto> MEMORY_TABLE = new LinkedHashMap<>();
    private static long MEMORY_SEQ = 1000L;

    static {
        seedMemoryRow("서울1창고", "P-100", "산업용 센서", "전자부품", 140, 30, 110,
                "한빛유통", "김대리", "010-1234-5678", "2026-04-27", "ACTIVE", "우선 출고 요청");
        seedMemoryRow("부산2창고", "P-220", "모터 모듈", "기계부품", 90, 12, 78,
                "남해물산", "이과장", "010-2345-6789", "2026-04-26", "ACTIVE", "특이사항 없음");
    }

    public List<WarehouseIoDto> list() {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_LIST);
             ResultSet rs = ps.executeQuery()) {
            List<WarehouseIoDto> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            return listMemory();
        }
    }

    public WarehouseIoDto detail(long id) {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DETAIL)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            WarehouseIoDto dto = MEMORY_TABLE.get(id);
            return dto == null ? null : cloneDto(dto);
        }
    }

    public long createEmptyRow() {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT_EMPTY, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            return -1L;
        } catch (SQLException e) {
            WarehouseIoDto dto = new WarehouseIoDto();
            dto.setWarehouseIoId(nextId());
            dto.setStatus("DRAFT");
            dto.setDeletedYn("N");
            MEMORY_TABLE.put(dto.getWarehouseIoId(), dto);
            return dto.getWarehouseIoId();
        }
    }

    // 아래 public update* 메서드는 프론트 필드 change 이벤트와 1:1 대응한다.
    public int updateWarehouseName(long id, String value) { return updateColumn(id, WarehouseIoColumn.WAREHOUSE_NAME, value); }
    public int updateProductCode(long id, String value) { return updateColumn(id, WarehouseIoColumn.PRODUCT_CODE, value); }
    public int updateProductName(long id, String value) { return updateColumn(id, WarehouseIoColumn.PRODUCT_NAME, value); }
    public int updateProductCategory(long id, String value) { return updateColumn(id, WarehouseIoColumn.PRODUCT_CATEGORY, value); }
    public int updateInQty(long id, int value) { return updateColumn(id, WarehouseIoColumn.IN_QTY, value); }
    public int updateOutQty(long id, int value) { return updateColumn(id, WarehouseIoColumn.OUT_QTY, value); }
    public int updateCurrentStock(long id, int value) { return updateColumn(id, WarehouseIoColumn.CURRENT_STOCK, value); }
    public int updateClientName(long id, String value) { return updateColumn(id, WarehouseIoColumn.CLIENT_NAME, value); }
    public int updateManagerName(long id, String value) { return updateColumn(id, WarehouseIoColumn.MANAGER_NAME, value); }
    public int updateManagerPhone(long id, String value) { return updateColumn(id, WarehouseIoColumn.MANAGER_PHONE, value); }
    public int updateIoDate(long id, String value) { return updateColumn(id, WarehouseIoColumn.IO_DATE, value); }
    public int updateStatus(long id, String value) { return updateColumn(id, WarehouseIoColumn.STATUS, value); }
    public int updateMemo(long id, String value) { return updateColumn(id, WarehouseIoColumn.MEMO, value); }

    public int delete(long id) {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SOFT_DELETE)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            WarehouseIoDto dto = MEMORY_TABLE.get(id);
            if (dto == null) {
                return 0;
            }
            dto.setDeletedYn("Y");
            return 1;
        }
    }

    // WarehouseIoColumn enum으로 허용 컬럼만 UPDATE하여 SQL 인젝션 위험을 줄인다.
    private int updateColumn(long id, WarehouseIoColumn column, Object value) {
        String sql = "UPDATE " + TABLE + " SET " + column.sqlName() + " = ?, UPDATED_AT = CURRENT_TIMESTAMP WHERE WAREHOUSE_IO_ID = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindValue(ps, 1, value);
            ps.setLong(2, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            WarehouseIoDto dto = MEMORY_TABLE.get(id);
            if (dto == null) {
                return 0;
            }
            column.apply(dto, value);
            return 1;
        }
    }

    private static void bindValue(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value instanceof Integer) {
            ps.setInt(index, (Integer) value);
        } else {
            ps.setString(index, value == null ? "" : String.valueOf(value));
        }
    }

    private static void seedMemoryRow(String warehouseName, String productCode, String productName,
                                      String productCategory, int inQty, int outQty, int currentStock,
                                      String clientName, String managerName, String managerPhone,
                                      String ioDate, String status, String memo) {
        WarehouseIoDto row = new WarehouseIoDto();
        row.setWarehouseIoId(nextId());
        row.setWarehouseName(warehouseName);
        row.setProductCode(productCode);
        row.setProductName(productName);
        row.setProductCategory(productCategory);
        row.setInQty(inQty);
        row.setOutQty(outQty);
        row.setCurrentStock(currentStock);
        row.setClientName(clientName);
        row.setManagerName(managerName);
        row.setManagerPhone(managerPhone);
        row.setIoDate(ioDate);
        row.setStatus(status);
        row.setMemo(memo);
        row.setDeletedYn("N");
        MEMORY_TABLE.put(row.getWarehouseIoId(), row);
    }

    private List<WarehouseIoDto> listMemory() {
        List<WarehouseIoDto> rows = new ArrayList<>();
        for (WarehouseIoDto dto : MEMORY_TABLE.values()) {
            if (!"Y".equals(dto.getDeletedYn())) {
                rows.add(cloneDto(dto));
            }
        }
        return rows;
    }

    private WarehouseIoDto mapRow(ResultSet rs) throws SQLException {
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

    // 메모리 원본이 외부에서 변경되지 않도록 복사본을 반환한다.
    private WarehouseIoDto cloneDto(WarehouseIoDto src) {
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
