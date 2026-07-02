package com.example.refactor.feature.investor.dto;

public record CreateInvestorRequest(
        String investorName,
        String investorGrade,
        Long totalAmount,
        String lastProductName,
        String screenMemo
) {
}
