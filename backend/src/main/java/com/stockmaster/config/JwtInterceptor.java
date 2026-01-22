package com.stockmaster.config;

import com.stockmaster.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 放行登录和注册接口
        String uri = request.getRequestURI();
        if (uri.contains("/api/auth/")) {
            return true;
        }

        // 获取token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            ObjectMapper mapper = new ObjectMapper();
            response.getWriter().write(mapper.writeValueAsString(Result.error("未登录或token已过期")));
            return false;
        }

        String token = authHeader.substring(7);
        
        // 验证token
        if (!jwtUtils.validateToken(token)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            ObjectMapper mapper = new ObjectMapper();
            response.getWriter().write(mapper.writeValueAsString(Result.error("token无效或已过期")));
            return false;
        }

        // 将用户信息设置到request中
        Long userId = jwtUtils.getUserIdFromToken(token);
        Long roleId = jwtUtils.getRoleIdFromToken(token);
        request.setAttribute("userId", userId);
        request.setAttribute("roleId", roleId);

        return true;
    }
}
