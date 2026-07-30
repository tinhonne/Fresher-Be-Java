package com.example.demo.dto.response;

import com.example.demo.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse <T>{

    private int code;
    private String message;
    private T result;

    /**
     * Creates a successful API envelope using the configured success code and message.
     * The result is preserved as supplied and may be {@code null}.
     *
     * @param result the response payload
     * @param <T> the payload type
     * @return a successful response envelope
     */
    public static <T> ApiResponse<T> success(T result){
        return ApiResponse.<T>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message(ErrorCode.SUCCESS.getMessage())
                .result(result)
                .build();
    }
    /**
     * Creates an error envelope from an error code, leaving the result unset.
     *
     * @param errorCode the source of the response code and message
     * @return an error response envelope
     */
    public static ApiResponse<?> error(ErrorCode errorCode){
        return ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }
    /**
     * Creates an error envelope using an error code's numeric code and a replacement
     * message, leaving the result unset.
     *
     * @param errorCode the source of the response code
     * @param message the message exposed in the envelope
     * @return an error response envelope
     */
    public static ApiResponse<?> error(ErrorCode errorCode, String message){
        return ApiResponse.builder()
                .code(errorCode.getCode())
                .message(message)
                .build();
    }
}
