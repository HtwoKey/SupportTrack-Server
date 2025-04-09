package com.yibei.supporttrack.service;

import com.yibei.supporttrack.entity.po.Role;

import java.util.List;

public interface RoleService {
    /**
     * 角色列表
     * @param keyword 查询参数
     * @param pageSize 分页数
     * @param pageNum 页码
     * @return 角色列表
     */
    List<Role> list(String keyword, Integer pageSize, Integer pageNum);

    /**
     * 更新角色
     * @param role 角色信息
     * @return 更新结果
     */
    int update(Role role);

    /**
     * 添加角色
     * @param role 角色信息
     * @return 添加结果
     */
    int create(Role role);

    /**
     * 删除角色
     * @param ids 角色id
     * @return 删除结果
     */
    int delete(List<Integer> ids);

    /**
     * 获取所有角色
     * @return 角色列表
     */
    List<Role> allList();
}
