package com.example.refactor.feature.investor.persistence;

import com.example.refactor.feature.investor.model.InvestorListItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataInvestorJpaRepository extends JpaRepository<InvestorJpaEntity, Long> {

    List<InvestorJpaEntity> findAllByOrderByInvestorIdAsc();

    List<InvestorJpaEntity> findByInvestorNameContainingOrderByInvestorIdAsc(String investorName);

    @Query("""
            SELECT new com.example.refactor.feature.investor.model.InvestorListItem(
                i.investorId, i.investorName, i.investorGrade, i.totalAmount
            )
            FROM InvestorJpaEntity i
            WHERE (:keyword IS NULL OR :keyword = '' OR i.investorName LIKE CONCAT('%', :keyword, '%'))
            ORDER BY i.investorId ASC
            """)
    List<InvestorListItem> findListItems(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT COUNT(i) FROM InvestorJpaEntity i
            WHERE (:keyword IS NULL OR :keyword = '' OR i.investorName LIKE CONCAT('%', :keyword, '%'))
            """)
    long countByKeyword(@Param("keyword") String keyword);
}
