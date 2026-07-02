package com.example.refactor.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

/**
 * 운영/DE 변경 추적 컬럼 — 도메인 model에는 노출하지 않는다.
 * <p>증분 추출: {@code WHERE updated_at > :last_loaded_at ORDER BY updated_at, id}
 */
@MappedSuperclass
public abstract class ChangeTrackedEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "source_system", length = 50, nullable = false)
    private String sourceSystem = "refactor-api";

    @Column(name = "etl_batch_id", length = 50)
    private String etlBatchId;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (sourceSystem == null || sourceSystem.isBlank()) {
            sourceSystem = "refactor-api";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getEtlBatchId() {
        return etlBatchId;
    }

    public void setEtlBatchId(String etlBatchId) {
        this.etlBatchId = etlBatchId;
    }
}
