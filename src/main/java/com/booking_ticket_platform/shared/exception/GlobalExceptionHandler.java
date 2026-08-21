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




    //3. Bat loi JSON Parse (VD: sai format UUID, sai format Date)
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(org.springframework.http.converter.HttpMessageNotReadableException exception) {
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Dữ liệu đầu vào không đúng định dạng (JSON Parse Error). Vui lòng kiểm tra lại kiểu dữ liệu (VD: UUID, Date, Enum).")
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    //4. Bat tat ca cac loi Crash he thong khong mong muon
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Da xay ra loi he thong: " + exception.getMessage())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    // Handle IllegalStateException
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException exception) {
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(exception.getMessage())
                .timestamp(OffsetDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
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
