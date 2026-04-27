package com.example.legacy.dao;

import com.example.legacy.dto.InvestorDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InvestorDao {
    // 이 샘플의 투자자 화면에서 사용하는 메모리 테이블.
    private static final Map<Long, InvestorDto> INVESTOR_TABLE = new LinkedHashMap<>();
    private static long INVESTOR_SEQ = 2000L;

    static {
        createSeed("김민수", "VIP", 850000000L, "글로벌인컴펀드", "최근 요청: 월간 리포트 이메일 발송");
        createSeed("이서연", "GOLD", 320000000L, "국내배당주랩", "최근 문의: 수익률 변동 사유");
        createSeed("박준호", "SILVER", 120000000L, "안정형채권플랜", "만기 도래 예정 상품 설명 필요");
    }

    public List<InvestorDto> list(String keyword) {
        // 이름 필터링을 DAO에서 직접 처리한다(단순/레거시 방식).
        String q = keyword == null ? "" : keyword.trim();
        List<InvestorDto> rows = new ArrayList<>();
        for (InvestorDto dto : INVESTOR_TABLE.values()) {
            if (q.isEmpty() || safe(dto.getInvestorName()).contains(q)) {
                rows.add(cloneDto(dto));
            }
        }
        return rows;
    }

    public InvestorDto detail(long investorId) {
        // 투자자 목록 행 클릭 시 상세조회에 사용된다.
        InvestorDto dto = INVESTOR_TABLE.get(investorId);
        return dto == null ? null : cloneDto(dto);
    }

    public long create(String investorName, String investorGrade, long totalAmount, String lastProductName, String screenMemo) {
        // 모달 "등록" 버튼에서 호출하는 생성 로직.
        InvestorDto dto = new InvestorDto();
        dto.setInvestorId(nextId());
        dto.setInvestorName(safe(investorName));
        dto.setInvestorGrade(safe(investorGrade));
        dto.setTotalAmount(totalAmount);
        dto.setLastProductName(safe(lastProductName));
        dto.setScreenMemo(safe(screenMemo));
        INVESTOR_TABLE.put(dto.getInvestorId(), dto);
        return dto.getInvestorId();
    }

    private static void createSeed(String name, String grade, long amount, String product, String memo) {
        InvestorDto dto = new InvestorDto();
        dto.setInvestorId(nextId());
        dto.setInvestorName(name);
        dto.setInvestorGrade(grade);
        dto.setTotalAmount(amount);
        dto.setLastProductName(product);
        dto.setScreenMemo(memo);
        INVESTOR_TABLE.put(dto.getInvestorId(), dto);
    }

    private static synchronized long nextId() {
        INVESTOR_SEQ++;
        return INVESTOR_SEQ;
    }

    private InvestorDto cloneDto(InvestorDto src) {
        // 저장소 원본 보호를 위해 복사본을 반환한다.
        InvestorDto dto = new InvestorDto();
        dto.setInvestorId(src.getInvestorId());
        dto.setInvestorName(src.getInvestorName());
        dto.setInvestorGrade(src.getInvestorGrade());
        dto.setTotalAmount(src.getTotalAmount());
        dto.setLastProductName(src.getLastProductName());
        dto.setScreenMemo(src.getScreenMemo());
        return dto;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
