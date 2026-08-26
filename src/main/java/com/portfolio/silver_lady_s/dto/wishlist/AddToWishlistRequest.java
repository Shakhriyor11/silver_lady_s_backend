package com.portfolio.silver_lady_s.dto.wishlist;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToWishlistRequest {
    @NotNull
    private Long productId;
}
