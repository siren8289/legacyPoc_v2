package com.example.legacy.servlet;

import javax.servlet.http.HttpServletRequest;
import java.util.AbstractMap;
import java.util.Set;

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
