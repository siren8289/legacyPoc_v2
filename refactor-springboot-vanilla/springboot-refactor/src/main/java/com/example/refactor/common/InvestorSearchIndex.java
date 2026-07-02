package com.example.refactor.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 투자자 이름 첫 글자 버킷 인덱스 (FR-012). */
public final class InvestorSearchIndex {

    private final Map<Character, List<Long>> byFirstChar = new HashMap<>();

    public void register(long id, String name) {
        if (name == null || name.isEmpty()) {
            return;
        }
        char first = Character.toLowerCase(name.trim().charAt(0));
        byFirstChar.computeIfAbsent(first, k -> new ArrayList<>()).add(id);
    }

    public List<Long> candidateIds(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        char first = Character.toLowerCase(keyword.trim().charAt(0));
        List<Long> ids = byFirstChar.get(first);
        return ids == null ? List.of() : ids;
    }
}
