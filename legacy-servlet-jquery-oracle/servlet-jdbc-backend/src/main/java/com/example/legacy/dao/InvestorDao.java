package com.example.legacy.dao;

import com.example.legacy.dto.InvestorDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 투자자 DAO.
 * JDBC 없이 메모리 Map으로 동작하며, 이름 검색은 SQL이 아닌 Java에서 필터링한다.
 */
public class InvestorDao {

    private static final Map<Long, InvestorDto> INVESTOR_TABLE = new LinkedHashMap<>();
    private static final InvestorSearchIndex NAME_INDEX = new InvestorSearchIndex();
    private static long INVESTOR_SEQ = 2000L;

    static {
        createSeed("김민수", "VIP", 850000000L, "글로벌인컴펀드", "최근 요청: 월간 리포트 이메일 발송");
        createSeed("이서연", "GOLD", 320000000L, "국내배당주랩", "최근 문의: 수익률 변동 사유");
        createSeed("박준호", "SILVER", 120000000L, "안정형채권플랜", "만기 도래 예정 상품 설명 필요");
    }

    public List<InvestorDto> list(String keyword) {
        String q = keyword == null ? "" : keyword.trim();
        List<Long> candidates = NAME_INDEX.candidateIds(keyword);
        List<InvestorDto> rows = new ArrayList<>();

        if (candidates == null) {
            for (InvestorDto dto : INVESTOR_TABLE.values()) {
                rows.add(cloneDto(dto));
            }
            return rows;
        }

        Iterable<Long> idsToScan = candidates.isEmpty()
                ? INVESTOR_TABLE.keySet()
                : candidates;

        for (Long id : idsToScan) {
            InvestorDto dto = INVESTOR_TABLE.get(id);
            if (dto == null) {
                continue;
            }
            if (q.isEmpty() || safe(dto.getInvestorName()).contains(q)) {
                rows.add(cloneDto(dto));
            }
        }
        return rows;
    }

    public InvestorDto detail(long investorId) {
        InvestorDto dto = INVESTOR_TABLE.get(investorId);
        return dto == null ? null : cloneDto(dto);
    }

    public long create(String investorName, String investorGrade, long totalAmount, String lastProductName, String screenMemo) {
        InvestorDto dto = new InvestorDto();
        dto.setInvestorId(nextId());
        dto.setInvestorName(safe(investorName));
        dto.setInvestorGrade(safe(investorGrade));
        dto.setTotalAmount(totalAmount);
        dto.setLastProductName(safe(lastProductName));
        dto.setScreenMemo(safe(screenMemo));
        INVESTOR_TABLE.put(dto.getInvestorId(), dto);
        NAME_INDEX.register(dto.getInvestorId(), dto.getInvestorName());
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
        NAME_INDEX.register(dto.getInvestorId(), name);
    }

    private static synchronized long nextId() {
        INVESTOR_SEQ++;
        return INVESTOR_SEQ;
    }

    private InvestorDto cloneDto(InvestorDto src) {
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
