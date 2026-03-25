package com.stockmaster.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CaffeineCaptchaStore {

    private final Cache<String, String> cache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    public void save(String key, String code) {
        cache.put(key, code.toLowerCase());
    }

    public boolean validateAndDelete(String key, String inputCode) {
        String stored = cache.getIfPresent(key);
        if (stored == null) return false;
        cache.invalidate(key);
        return stored.equals(inputCode.toLowerCase().trim());
    }
}
