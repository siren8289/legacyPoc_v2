package com.example.legacy.dto;

/** 투자자 화면 전용 DTO. DB 없이 메모리 저장소에서만 사용한다. */
public class InvestorDto {
    private long investorId;
    private String investorName;
    private String investorGrade;
    private long totalAmount;
    private String lastProductName;
    private String screenMemo;

    public long getInvestorId() {
        return investorId;
    }

    public void setInvestorId(long investorId) {
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

    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(long totalAmount) {
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
