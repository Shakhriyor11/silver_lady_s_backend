package com.portfolio.silver_lady_s.dto.sitesettings;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SiteSettingsUpdateRequest {
    // Ixtiyoriy: faqat yuborilgan maydon yangilanadi (partial update)
    private Boolean leavesEffectEnabled;
    private Boolean snowEffectEnabled;
}
