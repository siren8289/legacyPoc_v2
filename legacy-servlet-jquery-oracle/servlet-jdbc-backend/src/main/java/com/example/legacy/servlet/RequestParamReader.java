package com.example.legacy.servlet;

import javax.servlet.http.HttpServletRequest;
import java.util.AbstractMap;
import java.util.Set;

/**
 * HttpServletRequest 파라미터를 Map#get 형태로 읽기 위한 어댑터.
 * LegacySampleServer와 단위 테스트에서 processAction 호출 시 재사용한다.
 */
final class RequestParamReader extends AbstractMap<String, String> {

    private final HttpServletRequest request;

    RequestParamReader(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String get(Object key) {
        if (key == null) {
            return null;
        }
        return request.getParameter(String.valueOf(key));
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
