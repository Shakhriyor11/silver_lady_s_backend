package com.portfolio.silver_lady_s.dto.sitesettings;

import com.portfolio.silver_lady_s.entity.SiteSettings;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SiteSettingsResponse {
    private boolean leavesEffectEnabled;

    public static SiteSettingsResponse from(SiteSettings s) {
        return new SiteSettingsResponse(s.isLeavesEffectEnabled());
    }
}
