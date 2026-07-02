package com.example.refactor.feature.warehouse.persistence;

import com.example.refactor.feature.warehouse.model.WarehouseListItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SpringDataWarehouseJpaRepository extends JpaRepository<WarehouseJpaEntity, Long> {

    Optional<WarehouseJpaEntity> findByWarehouseIoIdAndDeletedYn(Long warehouseIoId, String deletedYn);

    List<WarehouseJpaEntity> findByDeletedYnOrderByWarehouseIoIdDesc(String deletedYn);

    @Query("""
            SELECT new com.example.refactor.feature.warehouse.model.WarehouseListItem(
                w.warehouseIoId, w.warehouseName, w.productName, w.currentStock
            )
            FROM WarehouseJpaEntity w
            WHERE w.deletedYn = 'N'
            ORDER BY w.warehouseIoId DESC
            """)
    List<WarehouseListItem> findActiveListItems(Pageable pageable);

    @Query("SELECT COUNT(w) FROM WarehouseJpaEntity w WHERE w.deletedYn = 'N'")
    long countActive();
}
