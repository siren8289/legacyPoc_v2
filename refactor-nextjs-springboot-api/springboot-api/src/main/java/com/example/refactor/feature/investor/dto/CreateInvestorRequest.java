package com.example.refactor.feature.investor.dto;

// 등록 API 요청 바디 DTO.
public record CreateInvestorRequest(
        String investorName,
        String investorGrade,
        Long totalAmount,
        String lastProductName,
        String screenMemo
) {
}
