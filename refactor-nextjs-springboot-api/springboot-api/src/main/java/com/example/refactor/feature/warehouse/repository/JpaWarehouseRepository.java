package com.example.refactor.feature.warehouse.repository;

import com.example.refactor.feature.warehouse.model.WarehouseIo;
import com.example.refactor.feature.warehouse.model.WarehouseListItem;
import com.example.refactor.feature.warehouse.persistence.SpringDataWarehouseJpaRepository;
import com.example.refactor.feature.warehouse.persistence.WarehouseJpaEntity;
import com.example.refactor.feature.warehouse.persistence.WarehouseJpaMapper;
import com.example.refactor.common.PageResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA 저장소 — Entity 조회·저장 후 항상 {@link WarehouseIo} 도메인으로 변환해 반환한다.
 */
@Repository
@ConditionalOnProperty(name = "app.repository.type", havingValue = "jpa")
public class JpaWarehouseRepository implements WarehouseRepository {

    private final SpringDataWarehouseJpaRepository jpa;

    public JpaWarehouseRepository(SpringDataWarehouseJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<WarehouseIo> findAll() {
        return jpa.findByDeletedYnOrderByWarehouseIoIdDesc("N").stream()
                .map(WarehouseJpaMapper::toDomain)
                .toList();
    }

    @Override
    public PageResult<WarehouseListItem> findActivePage(int page, int size) {
        int safeSize = Math.max(1, Math.min(size, 100));
        int safePage = Math.max(0, page);
        List<WarehouseListItem> items = jpa.findActiveListItems(PageRequest.of(safePage, safeSize));
        long total = jpa.countActive();
        return PageResult.of(items, total, safePage, safeSize);
    }

    @Override
    public Optional<WarehouseIo> findById(Long id) {
        return jpa.findByWarehouseIoIdAndDeletedYn(id, "N").map(WarehouseJpaMapper::toDomain);
    }

    @Override
    public WarehouseIo save(WarehouseIo item) {
        WarehouseJpaEntity entity = item.warehouseIoId() == null
                ? WarehouseJpaMapper.toNewEntity(item)
                : jpa.findByWarehouseIoIdAndDeletedYn(item.warehouseIoId(), "N")
                        .map(existing -> {
                            WarehouseJpaMapper.merge(existing, item);
                            return existing;
                        })
                        .orElseGet(() -> WarehouseJpaMapper.toNewEntity(item));
        return WarehouseJpaMapper.toDomain(jpa.save(entity));
    }

    @Override
    public WarehouseIo updateField(Long id, String fieldName, Object value) {
        WarehouseJpaEntity entity = jpa.findByWarehouseIoIdAndDeletedYn(id, "N").orElse(null);
        if (entity == null) {
            return null;
        }
        WarehouseJpaMapper.applyField(entity, fieldName, value);
        return WarehouseJpaMapper.toDomain(jpa.save(entity));
    }

    @Override
    public void deleteById(Long id) {
        jpa.findByWarehouseIoIdAndDeletedYn(id, "N").ifPresent(entity -> {
            entity.setDeletedYn("Y");
            entity.setDeletedAt(LocalDateTime.now());
            jpa.save(entity);
        });
    }
}
