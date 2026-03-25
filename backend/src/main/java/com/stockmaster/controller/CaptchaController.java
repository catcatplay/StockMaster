package com.stockmaster.controller;

import com.stockmaster.common.Result;
import com.stockmaster.config.CaptchaConfig;
import com.stockmaster.service.CaffeineCaptchaStore;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class CaptchaController {

    @Autowired
    private CaptchaConfig captchaConfig;

    @Autowired
    private CaffeineCaptchaStore captchaStore;

    @GetMapping("/captcha")
    public Result<Map<String, Object>> getCaptcha() {
        Map<String, Object> data = new HashMap<>();
        data.put("enabled", captchaConfig.isEnabled());

        if (!captchaConfig.isEnabled()) {
            data.put("captchaKey", null);
            data.put("captchaImage", null);
            return Result.success(data);
        }

        Captcha captcha;
        switch (captchaConfig.getType()) {
            case "gif":
                captcha = new GifCaptcha(captchaConfig.getWidth(), captchaConfig.getHeight(), captchaConfig.getLength());
                break;
            case "spec":
                captcha = new SpecCaptcha(captchaConfig.getWidth(), captchaConfig.getHeight(), captchaConfig.getLength());
                break;
            default:
                captcha = new ArithmeticCaptcha(captchaConfig.getWidth(), captchaConfig.getHeight());
        }

        String key = UUID.randomUUID().toString().replace("-", "");
        captchaStore.save(key, captcha.text());

        data.put("captchaKey", key);
        data.put("captchaImage", captcha.toBase64().replaceFirst("^data:image/[^;]+;base64,", ""));
        data.put("type", captchaConfig.getType());
        return Result.success(data);
    }
}
