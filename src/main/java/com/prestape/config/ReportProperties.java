package com.prestape.config;

import java.time.ZoneId;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.report")
public record ReportProperties(
        @DefaultValue("America/Lima") ZoneId timezone,
        @DefaultValue("10000") int maxRows
) {
}
