package com.stockmaster.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stockmaster.common.Result;
import com.stockmaster.entity.Role;
import com.stockmaster.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,
                      @RequestParam(defaultValue = "10") Integer size,
                      @RequestParam(required = false) String roleName) {
        Page<Role> pageObj = new Page<>(page, size);
        QueryWrapper<Role> wrapper = new QueryWrapper<>();
        
        if (roleName != null && !roleName.isEmpty()) {
            wrapper.like("role_name", roleName);
        }
        
        Page<Role> result = roleService.page(pageObj, wrapper);
        return Result.success(result);
    }

    @GetMapping("/all")
    public Result getAllRoles() {
        List<Role> roles = roleService.list();
        return Result.success(roles);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Role role = roleService.getById(id);
        if (role != null) {
            return Result.success(role);
        }
        return Result.error("角色不存在");
    }

    @PostMapping("/add")
    public Result add(@RequestBody Role role) {
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        
        boolean success = roleService.save(role);
        if (success) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PutMapping("/update")
    public Result update(@RequestBody Role role) {
        role.setUpdateTime(LocalDateTime.now());
        
        boolean success = roleService.updateById(role);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        boolean success = roleService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
