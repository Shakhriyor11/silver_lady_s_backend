package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.sitesettings.SiteSettingsResponse;
import com.portfolio.silver_lady_s.dto.sitesettings.SiteSettingsUpdateRequest;

public interface SiteSettingsService {
    SiteSettingsResponse getSettings();
    SiteSettingsResponse update(SiteSettingsUpdateRequest dto);
}
