package com.stockmaster.controller;

import com.stockmaster.common.Result;
import com.stockmaster.config.CaptchaConfig;
import com.stockmaster.config.JwtUtils;
import com.stockmaster.dto.LoginRequest;
import com.stockmaster.dto.LoginResponse;
import com.stockmaster.entity.Role;
import com.stockmaster.entity.User;
import com.stockmaster.service.CaffeineCaptchaStore;
import com.stockmaster.service.RoleService;
import com.stockmaster.service.UserService;
import com.stockmaster.util.PermissionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CaptchaConfig captchaConfig;

    @Autowired
    private CaffeineCaptchaStore captchaStore;

    @PostMapping("/login")
    public Result login(@RequestBody LoginRequest request) {
        if (captchaConfig.isEnabled()) {
            if (!StringUtils.hasText(request.getCaptchaKey()) || !StringUtils.hasText(request.getCaptchaCode())) {
                return Result.error("验证码不能为空");
            }
            if (!captchaStore.validateAndDelete(request.getCaptchaKey(), request.getCaptchaCode())) {
                return Result.error("验证码错误或已过期");
            }
        }
        User user = userService.login(request.getUsername(), request.getPassword());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        List<String> permissions = new ArrayList<>();
        if (user.getRoleId() != null) {
            Role role = roleService.getById(user.getRoleId());
            if (role != null && role.getPermissions() != null) {
                permissions = PermissionUtils.parsePermissions(role.getPermissions());
            }
        }

        String token = jwtUtils.generateToken(user.getId(), user.getRealName(),
                user.getRoleId(), user.getRoleName());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getRealName());
        response.setRealName(user.getRealName());
        response.setRoleId(user.getRoleId());
        response.setRoleName(user.getRoleName());
        response.setPermissions(permissions);
        return Result.success(response);
    }

    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        boolean success = userService.register(user);
        if (success) {
            return Result.success("注册成功");
        }
        return Result.error("用户名已存在");
    }

    @GetMapping("/info")
    public Result getUserInfo(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("未登录");
        }

        String username = jwtUtils.getUsernameFromAuthorizationHeader(authHeader);
        if (username == null) {
            return Result.error("token无效");
        }

        User user = userService.getUserByUsername(username);
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }
}
