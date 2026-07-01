package com.example.refactor.common;

// 응답 포맷을 { data: ... } 형태로 통일하기 위한 공통 래퍼.
public class ApiResponse<T> {
    private T data;

    public ApiResponse(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
