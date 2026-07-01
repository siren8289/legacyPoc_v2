package com.example.refactor.common;

// 뷰 화면에서 공통으로 사용하는 응답 구조.
public class ApiResponse<T> {
    private T data;

    public ApiResponse(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
