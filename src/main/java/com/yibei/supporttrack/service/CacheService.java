package com.yibei.supporttrack.service;

import com.yibei.supporttrack.entity.po.Permission;
import com.yibei.supporttrack.entity.po.User;

import java.util.List;

public interface CacheService {
    /**
     * 删除后台用户缓存
     * @param username 用户名
     */
    void delUser(String username);

    /**
     * 删除后台用户权限资源列表缓存
     * @param UserId 用户ID
     */
    void delPermissionList(Integer UserId);

    /**
     * 当角色相关资源信息改变时删除相关后台用户缓存
     */
    void delPermissionListByRole(Integer roleId);

    /**
     * 当角色相关资源信息改变时删除相关后台用户缓存
     */
    void delPermissionListByRoleIds(List<Integer> roleIds);

    /**
     * 当资源信息改变时，删除资源项目后台用户缓存
     */
    void delPermissionListByPermission(Integer PermissionId);

    /**
     * 获取缓存后台用户信息
     */
    User getUser(String username);

    /**
     * 设置缓存后台用户信息
     */
    void setUser(User user);

    /**
     * 获取缓存后台用户资源列表
     */
    List<Permission> getPermissionList(Integer userId);

    /**
     * 设置缓存后台用户资源列表
     */
    void setPermissionList(Integer userId, List<Permission> PermissionList);
}
