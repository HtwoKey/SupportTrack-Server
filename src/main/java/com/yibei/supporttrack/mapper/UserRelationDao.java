package com.yibei.supporttrack.mapper;

import com.yibei.supporttrack.entity.po.Permission;

import java.util.List;

public interface UserRelationDao {

    List<Permission> getUserPermissionByUserId(Integer userId);
}
