package com.org.library.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    /**
     * Human-readable application name.
     * Injected from: app.name
     */
    private String name;

    /**
     * Current application version string.
     * Injected from: app.version
     */
    private String version;
}
