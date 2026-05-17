package com.portfolio.silver_lady_s.service;

public interface SmsService {

    /**
     * SMS yuboradi. Raqam formati: 998XXXXXXXXX
     */
    void send(String phone, String message);
}
