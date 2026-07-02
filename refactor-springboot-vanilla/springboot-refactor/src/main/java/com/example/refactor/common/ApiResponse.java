package com.example.refactor.common;

// 조회 API 응답을 { data: T } 형태로 통일하는 공통 래퍼.
public class ApiResponse<T> {
    private T data;

    public ApiResponse(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
