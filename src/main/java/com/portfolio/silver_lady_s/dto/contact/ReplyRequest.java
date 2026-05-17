package com.portfolio.silver_lady_s.dto.contact;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplyRequest {

    @NotBlank
    @Size(max = 3000)
    private String reply;
}
