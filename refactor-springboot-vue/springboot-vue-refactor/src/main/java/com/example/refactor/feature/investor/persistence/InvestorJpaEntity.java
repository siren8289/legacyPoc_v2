package com.example.refactor.feature.investor.persistence;

import com.example.refactor.persistence.ChangeTrackedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * {@code tb_investor} 테이블 JPA 매핑.
 * <p>persistence 계층 전용 — Service는 {@link com.example.refactor.feature.investor.model.Investor}만 사용한다.
 */
@Entity
@Table(name = "tb_investor")
public class InvestorJpaEntity extends ChangeTrackedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "investor_id")
    private Long investorId;

    @Column(name = "investor_name", length = 100)
    private String investorName;

    @Column(name = "investor_grade", length = 20)
    private String investorGrade;

    @Column(name = "total_amount")
    private Long totalAmount;

    @Column(name = "last_product_name", length = 100)
    private String lastProductName;

    @Column(name = "screen_memo", length = 500)
    private String screenMemo;

    protected InvestorJpaEntity() {
    }

    public Long getInvestorId() {
        return investorId;
    }

    public void setInvestorId(Long investorId) {
        this.investorId = investorId;
    }

    public String getInvestorName() {
        return investorName;
    }

    public void setInvestorName(String investorName) {
        this.investorName = investorName;
    }

    public String getInvestorGrade() {
        return investorGrade;
    }

    public void setInvestorGrade(String investorGrade) {
        this.investorGrade = investorGrade;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getLastProductName() {
        return lastProductName;
    }

    public void setLastProductName(String lastProductName) {
        this.lastProductName = lastProductName;
    }

    public String getScreenMemo() {
        return screenMemo;
    }

    public void setScreenMemo(String screenMemo) {
        this.screenMemo = screenMemo;
    }
}
