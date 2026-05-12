package com.portfolio.silver_lady_s.dto.contact;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Foydalanuvchi sotuvchiga yuboradigan xabar.
 */
@Getter
@Setter
public class ContactRequest {

    /** Bog'liq mahsulot (ixtiyoriy) */
    private Long productId;

    @NotBlank @Size(max = 200)
    private String subject;

    @NotBlank @Size(max = 3000)
    private String message;
}
