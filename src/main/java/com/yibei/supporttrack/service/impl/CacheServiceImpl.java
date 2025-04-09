package com.yibei.supporttrack.service.impl;

import com.yibei.supporttrack.entity.po.Permission;
import com.yibei.supporttrack.entity.po.User;
import com.yibei.supporttrack.service.CacheService;
import com.yibei.supporttrack.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CacheServiceImpl implements CacheService {

    @Autowired
    private RedisService redisService;
    @Value("${redis.database}")
    private String REDIS_DATABASE;
    @Value("${redis.expire.common}")
    private Long REDIS_EXPIRE;
    @Value("${redis.key.user}")
    private String REDIS_KEY_ADMIN;
    @Value("${redis.key.permissionList}")
    private String REDIS_KEY_RESOURCE_LIST;




    @Override
    public void delPermissionList(Integer UserId) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":" + UserId;
        redisService.del(key);
    }

    @Override
    public void delPermissionListByRole(Integer roleId) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":" + roleId;
        redisService.del(key);
    }

    @Override
    public void delPermissionListByRoleIds(List<Integer> roleIds) {
        for (Integer roleId : roleIds) {
            String key = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":" + roleId;
            redisService.del(key);
        }
    }

    @Override
    public void delPermissionListByPermission(Integer PermissionId) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":" + PermissionId;
        redisService.del(key);
    }

    @Override
    public User getUser(String username) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + username;
        return redisService.get(key, User.class);
    }

    @Override
    public void setUser(User admin) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + admin.getUsername();
        redisService.set(key, admin, REDIS_EXPIRE);
    }

    @Override
    public void delUser(String username) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + username;
        redisService.del(key);
    }

    @Override
    public List<Permission> getPermissionList(Integer userId) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":" + userId;
        return redisService.get(key, ArrayList.class);
    }

    @Override
    public void setPermissionList(Integer userId, List<Permission> PermissionList) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":" + userId;
        redisService.set(key, PermissionList, REDIS_EXPIRE);
    }
}
