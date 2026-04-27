package com.example.refactor.common;

// 투자자/입출고 API가 공통으로 사용하는 응답 래퍼.
public class ApiResponse<T> {
    private T data;

    public ApiResponse(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
