package com.portfolio.silver_lady_s.dto.sitesettings;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SiteSettingsUpdateRequest {
    @NotNull
    private Boolean leavesEffectEnabled;
}
