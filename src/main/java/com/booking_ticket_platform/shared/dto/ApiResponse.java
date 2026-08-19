package com.booking_ticket_platform.shared.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    @Builder.Default
    private int code = 200;

    private String message;

    private T result;

    @Builder.Default
    private OffsetDateTime timestamp = OffsetDateTime.now();
}
