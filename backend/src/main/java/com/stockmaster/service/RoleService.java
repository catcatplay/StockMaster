package com.stockmaster.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stockmaster.entity.Role;

public interface RoleService extends IService<Role> {
    /**
     * 根据角色代码查询角色
     */
    Role getRoleByCode(String roleCode);
}
