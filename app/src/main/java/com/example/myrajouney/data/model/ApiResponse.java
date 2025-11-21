package com.example.myrajouney.data.model;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("error")
    private ApiError error;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public ApiError getError() { return error; }

    public static class ApiError {
        @SerializedName("code")
        private int code;
        @SerializedName("message")
        private String message;

        public String getMessage() { return message; }
    }
}