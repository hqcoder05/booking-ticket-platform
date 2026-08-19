package com.booking_ticket_platform.shared.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.OffsetDateTime;
import com.booking_ticket_platform.shared.dto.ApiResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.HttpStatus;


@RestControllerAdvice
public class GlobalExceptionHandler {



    //1. Bat loi cho cac exception ke thua tu BusinessException
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(exception.getStatus().value())
                .message(exception.getMessage())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(exception.getStatus()).body(apiResponse);
    }



    //2. Bat loi ValidationException du lieu dau vao (@Valid, @NotBlank, @Email...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        String errorMessage = exception.getFieldErrors() != null
                ? exception.getFieldError().getDefaultMessage()
                : "Dữ liệu không hợp lệ";

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(errorMessage)
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }




    //3. Bat tat ca cac loi Crash he thong khong mong muon
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Da xay ra loi he thong: " + exception.getMessage())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    //4. Bat loi khong tim thay resource hoac API
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(org.springframework.web.servlet.resource.NoResourceFoundException exception) {
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message("Khong tim thay API hoac Resource nay: " + exception.getResourcePath())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
    }
}
