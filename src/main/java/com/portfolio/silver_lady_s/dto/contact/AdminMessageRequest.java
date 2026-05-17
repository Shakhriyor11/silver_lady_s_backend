package com.portfolio.silver_lady_s.dto.contact;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminMessageRequest {

    @NotNull
    private Long targetUserId;

    @NotBlank
    @Size(max = 200)
    private String subject;

    @NotBlank
    @Size(max = 3000)
    private String message;
}
