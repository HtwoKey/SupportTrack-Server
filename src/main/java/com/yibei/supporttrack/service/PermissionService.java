package com.yibei.supporttrack.service;

import com.yibei.supporttrack.entity.dto.MenuQueryParam;
import com.yibei.supporttrack.entity.po.Permission;
import com.yibei.supporttrack.entity.vo.RouterVo;
import com.yibei.supporttrack.entity.vo.TreeSelect;

import java.util.List;

public interface PermissionService {

    List<Permission> getPermissionList(Integer userId);

    List<Permission> selectPermissionList(MenuQueryParam menu);

    List<RouterVo> buildMenus(List<Permission> permissions);

    int addPermission(Permission permission);

    int updatePermission(Permission permission);

    int deletePermissionById(Integer id);

    List<Permission> selectAllPermissionList();

    List<TreeSelect> buildMenuTreeSelect(List<Permission> menus);

    List<Permission> getPermissionByUserId(Integer userId);
}
