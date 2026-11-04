package com.project.backend.dto.response;

/**
 * Standard API response wrapper for successful requests.
 *
 * @param <T> Type of the response data payload
 */
public class ApiResponse<T> {

    private T data;

    public ApiResponse() {
    }

    public ApiResponse(T data) {
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
