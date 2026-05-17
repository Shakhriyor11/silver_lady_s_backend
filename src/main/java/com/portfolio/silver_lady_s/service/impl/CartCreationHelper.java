package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.entity.Cart;
import com.portfolio.silver_lady_s.entity.User;
import com.portfolio.silver_lady_s.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cart yaratish uchun alohida tranzaksiya konteksti.
 *
 * REQUIRES_NEW — tashqi tranzaksiyadan mustaqil yangi tranzaksiya ochadi.
 * Bu pattern kerak, chunki bir xil ClassServiceImpl ichida self-call orqali
 * @Transactional(REQUIRES_NEW) Spring proxy tomonidan ushlana olmaydi.
 *
 * Race condition stsenariysi:
 *   Ikkita parallel so'rov birinchi marta cart yaratmoqchi:
 *   1. Ikkalasi ham findByUserId → bo'sh
 *   2. Biri saveAndFlush → muvaffaqiyat
 *   3. Ikkinchisi saveAndFlush → DataIntegrityViolationException (uk_cart_user)
 *   4. Ichki tranzaksiya rollback — TASHQI tranzaksiya hali sog'lom
 *   5. catch blokida tashqi tranzaksiyada findByUserId → birinchi yaratilgani qaytadi
 */
@Component
@RequiredArgsConstructor
public class CartCreationHelper {

    private final CartRepository cartRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Cart createCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.saveAndFlush(cart);
    }
}
