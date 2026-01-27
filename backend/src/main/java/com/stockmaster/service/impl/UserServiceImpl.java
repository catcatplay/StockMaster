package com.stockmaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stockmaster.entity.Role;
import com.stockmaster.entity.User;
import com.stockmaster.mapper.RoleMapper;
import com.stockmaster.mapper.UserMapper;
import com.stockmaster.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User login(String username, String password) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        wrapper.eq("status", 1); // 只查询启用状态的用户
        
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            return null;
        }
        System.out.println(passwordEncoder.encode("admin123"));
        // 验证密码
        if (passwordEncoder.matches(password, user.getPassword())) {
            // 查询角色信息
            if (user.getRoleId() != null) {
                Role role = roleMapper.selectById(user.getRoleId());
                if (role != null) {
                    user.setRoleName(role.getRoleName());
                }
            }
            return user;
        }
        
        return null;
    }

    @Override
    public boolean register(User user) {
        // 检查用户名是否已存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            return false;
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1); // 默认启用
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        // 设置角色名称
        if (user.getRoleId() != null) {
            Role role = roleMapper.selectById(user.getRoleId());
            if (role != null) {
                user.setRoleName(role.getRoleName());
            }
        }
        
        return userMapper.insert(user) > 0;
    }

    @Override
    public User getUserByUsername(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        return userMapper.selectOne(wrapper);
    }
}
