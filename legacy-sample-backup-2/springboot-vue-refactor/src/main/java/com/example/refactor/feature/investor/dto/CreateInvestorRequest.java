package com.example.refactor.feature.investor.dto;

// 투자자 등록 API 요청 DTO.
public record CreateInvestorRequest(
        String investorName,
        String investorGrade,
        Long totalAmount,
        String lastProductName,
        String screenMemo
) {
}
