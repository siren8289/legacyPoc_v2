package com.example.refactor.feature.warehouse.persistence;

import com.example.refactor.persistence.ChangeTrackedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * {@code tb_warehouse_io} 테이블 JPA 매핑.
 * <p>persistence 계층 전용 — Service는 {@link com.example.refactor.feature.warehouse.model.WarehouseIo}만 사용한다.
 */
@Entity
@Table(name = "tb_warehouse_io")
public class WarehouseJpaEntity extends ChangeTrackedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_io_id")
    private Long warehouseIoId;

    @Column(name = "warehouse_name", length = 100)
    private String warehouseName;

    @Column(name = "product_code", length = 40)
    private String productCode;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "product_category", length = 60)
    private String productCategory;

    @Column(name = "in_qty")
    private Integer inQty;

    @Column(name = "out_qty")
    private Integer outQty;

    @Column(name = "current_stock")
    private Integer currentStock;

    @Column(name = "client_name", length = 100)
    private String clientName;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "deleted_yn", length = 1)
    private String deletedYn = "N";

    protected WarehouseJpaEntity() {
    }

    public Long getWarehouseIoId() {
        return warehouseIoId;
    }

    public void setWarehouseIoId(Long warehouseIoId) {
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

    public Integer getInQty() {
        return inQty;
    }

    public void setInQty(Integer inQty) {
        this.inQty = inQty;
    }

    public Integer getOutQty() {
        return outQty;
    }

    public void setOutQty(Integer outQty) {
        this.outQty = outQty;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeletedYn() {
        return deletedYn;
    }

    public void setDeletedYn(String deletedYn) {
        this.deletedYn = deletedYn;
    }
}
