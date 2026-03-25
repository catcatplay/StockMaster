package com.stockmaster.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "captcha")
public class CaptchaConfig {

    private boolean enabled = false;
    private String type = "arithmetic";
    private int width = 130;
    private int height = 48;
    private int length = 4;

    private static final List<String> VALID_TYPES = Arrays.asList("arithmetic", "gif", "chinese", "spec");

    @PostConstruct
    public void validate() {
        if (!VALID_TYPES.contains(type)) {
            throw new IllegalArgumentException("Invalid captcha type: " + type);
        }
    }

}
