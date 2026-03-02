package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.about.AboutResponse;
import com.portfolio.silver_lady_s.dto.about.AboutUpdateRequest;

public interface AboutService {
    AboutResponse getAboutInfo();
    AboutResponse update(AboutUpdateRequest dto);
}
