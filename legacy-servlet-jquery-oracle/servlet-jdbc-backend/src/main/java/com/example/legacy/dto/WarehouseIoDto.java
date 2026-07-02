package com.example.legacy.dto;

/** TB_WAREHOUSE_IO_SCREEN 테이블과 1:1 매핑되는 화면 전용 DTO. */
public class WarehouseIoDto {
    private long warehouseIoId;
    private String warehouseName;
    private String productCode;
    private String productName;
    private String productCategory;
    private int inQty;
    private int outQty;
    private int currentStock;
    private String clientName;
    private String managerName;
    private String managerPhone;
    private String ioDate;
    private String status;
    private String memo;
    private String deletedYn;

    public long getWarehouseIoId() {
        return warehouseIoId;
    }

    public void setWarehouseIoId(long warehouseIoId) {
        this.warehouseIoId = warehouseIoId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public int getInQty() {
        return inQty;
    }

    public void setInQty(int inQty) {
        this.inQty = inQty;
    }

    public int getOutQty() {
        return outQty;
    }

    public void setOutQty(int outQty) {
        this.outQty = outQty;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(int currentStock) {
        this.currentStock = currentStock;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getManagerPhone() {
        return managerPhone;
    }

    public void setManagerPhone(String managerPhone) {
        this.managerPhone = managerPhone;
    }

    public String getIoDate() {
        return ioDate;
    }

    public void setIoDate(String ioDate) {
        this.ioDate = ioDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getDeletedYn() {
        return deletedYn;
    }

    public void setDeletedYn(String deletedYn) {
        this.deletedYn = deletedYn;
    }
}
