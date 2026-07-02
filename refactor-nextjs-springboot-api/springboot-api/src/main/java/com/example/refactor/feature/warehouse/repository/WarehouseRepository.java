package com.example.refactor.feature.warehouse.repository;

import com.example.refactor.common.PageResult;
import com.example.refactor.feature.warehouse.model.WarehouseIo;
import com.example.refactor.feature.warehouse.model.WarehouseListItem;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository {

    WarehouseIo save(WarehouseIo warehouseIo);

    Optional<WarehouseIo> findById(Long id);

    List<WarehouseIo> findAll();

    PageResult<WarehouseListItem> findActivePage(int page, int size);

    WarehouseIo updateField(Long id, String fieldName, Object value);

    void deleteById(Long id);
}
