package com.example.refactor.feature.investor.repository;

import com.example.refactor.common.PageResult;
import com.example.refactor.feature.investor.model.Investor;
import com.example.refactor.feature.investor.model.InvestorListItem;

import java.util.List;
import java.util.Optional;

public interface InvestorRepository {

    Investor save(Investor investor);

    Optional<Investor> findById(Long id);

    List<Investor> findAll(String keyword);

    PageResult<InvestorListItem> findPage(String keyword, int page, int size);

    void deleteById(Long id);
}
