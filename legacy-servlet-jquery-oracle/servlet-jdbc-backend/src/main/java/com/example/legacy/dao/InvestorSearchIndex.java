package com.example.legacy.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 투자자 이름 검색용 보조 인덱스.
 * 이름 첫 글자 버킷으로 후보 ID를 좁힌 뒤 contains 검증한다 (FR-012).
 */
final class InvestorSearchIndex {

    private final Map<Character, List<Long>> byFirstChar = new HashMap<>();

    void register(long id, String name) {
        if (name == null || name.isEmpty()) {
            return;
        }
        char first = Character.toLowerCase(name.trim().charAt(0));
        byFirstChar.computeIfAbsent(first, k -> new ArrayList<>()).add(id);
    }

    /**
     * @return null이면 전체 조회, 빈 리스트면 후보 없음(풀스캔 fallback)
     */
    List<Long> candidateIds(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        String q = keyword.trim();
        char first = Character.toLowerCase(q.charAt(0));
        List<Long> ids = byFirstChar.get(first);
        return ids == null ? List.of() : ids;
    }
}
