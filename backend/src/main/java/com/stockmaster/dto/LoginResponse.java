package com.stockmaster.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private Long roleId;
    private String roleName;
    private List<String> permissions; // 权限列表
}
