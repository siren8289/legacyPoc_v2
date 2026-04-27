package com.example.refactor.feature.investor.dto;

// 투자자 신규 등록 요청 바디 모델.
public record CreateInvestorRequest(
        String investorName,
        String investorGrade,
        Long totalAmount,
        String lastProductName,
        String screenMemo
) {
}
