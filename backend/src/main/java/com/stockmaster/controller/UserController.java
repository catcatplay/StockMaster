package com.stockmaster.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stockmaster.common.Result;
import com.stockmaster.entity.User;
import com.stockmaster.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,
                      @RequestParam(defaultValue = "10") Integer size,
                      @RequestParam(required = false) String username,
                      @RequestParam(required = false) Long roleId) {
        Page<User> pageObj = new Page<>(page, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        
        if (username != null && !username.isEmpty()) {
            wrapper.like("username", username);
        }
        
        if (roleId != null) {
            wrapper.eq("role_id", roleId);
        }
        
        Page<User> result = userService.page(pageObj, wrapper);
        
        // 不返回密码
        result.getRecords().forEach(user -> user.setPassword(null));
        
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }

    @PostMapping("/add")
    public Result add(@RequestBody User user) {
        boolean success = userService.register(user);
        if (success) {
            return Result.success("添加成功");
        }
        return Result.error("用户名已存在");
    }

    @PutMapping("/update")
    public Result update(@RequestBody User user) {
        User existUser = userService.getById(user.getId());
        if (existUser == null) {
            return Result.error("用户不存在");
        }
        
        // 如果密码为空或未改变，则不更新密码
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(existUser.getPassword());
        } else {
            // 如果密码改变了，重新加密
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        user.setUpdateTime(LocalDateTime.now());
        
        boolean success = userService.updateById(user);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = userService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    @PutMapping("/status/{id}")
    public Result updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        
        boolean success = userService.updateById(user);
        if (success) {
            return Result.success("状态更新成功");
        }
        return Result.error("状态更新失败");
    }
}
