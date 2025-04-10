package com.yibei.supporttrack.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.yibei.supporttrack.entity.dto.MenuQueryParam;
import com.yibei.supporttrack.entity.po.Permission;
import com.yibei.supporttrack.entity.po.RolePermissionRelation;
import com.yibei.supporttrack.entity.po.User;
import com.yibei.supporttrack.entity.vo.MetaVo;
import com.yibei.supporttrack.entity.vo.RouterVo;
import com.yibei.supporttrack.entity.vo.TreeSelect;
import com.yibei.supporttrack.mapper.PermissionMapper;
import com.yibei.supporttrack.mapper.RolePermissionRelationMapper;
import com.yibei.supporttrack.mapper.UserMapper;
import com.yibei.supporttrack.mapper.UserRelationDao;
import com.yibei.supporttrack.service.CacheService;
import com.yibei.supporttrack.service.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;
    private final CacheService cacheService;
    private final UserRelationDao userRelationDao;
    private final UserMapper userMapper;
    private final RolePermissionRelationMapper rolePermissionRelationMapper;

    public PermissionServiceImpl(PermissionMapper permissionMapper, CacheService cacheService, UserRelationDao userRelationDao, UserMapper userMapper, RolePermissionRelationMapper rolePermissionRelationMapper) {
        this.permissionMapper = permissionMapper;
        this.cacheService = cacheService;
        this.userRelationDao = userRelationDao;
        this.userMapper = userMapper;
        this.rolePermissionRelationMapper = rolePermissionRelationMapper;
    }

    @Override
    public List<Permission> getPermissionList(Integer userId) {
        // 从缓存获取权限列表
        List<Permission> permissionList = cacheService.getPermissionList(userId);
        if(CollUtil.isNotEmpty(permissionList)){
            return  permissionList;
        }
        // 判断是否是管理员
        User user = userMapper.selectById(userId);
        if(user.getIsAdmin()){
            // 获取uri不为空的权限
            permissionList = permissionMapper.selectList(new QueryWrapper<Permission>().lambda().isNotNull(Permission::getUri));
            if(CollUtil.isNotEmpty(permissionList)){
                cacheService.setPermissionList(userId,permissionList);
            }
            return permissionList;
        }

        permissionList = userRelationDao.getUserPermissionByUserId(userId);
        if(CollUtil.isNotEmpty(permissionList)){
            cacheService.setPermissionList(userId,permissionList);
        }
        return permissionList;
    }

    @Override
    public List<Permission> selectPermissionList(MenuQueryParam menu) {
        PageHelper.startPage(menu.getPageNum(), menu.getPageSize());
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", 0);
        if (menu.getTitle() != null) {
            queryWrapper.like("title", menu.getTitle());
        }
        if (menu.getName() != null) {
            queryWrapper.like("name", menu.getName());
        }
        if (menu.getIsHide() != null) {
            queryWrapper.eq("is_hide", menu.getIsHide());
        }
        if (menu.getPermissionName() != null) {
            queryWrapper.like("permission_name", menu.getPermissionName());
        }
        queryWrapper.orderByAsc("sort");
        List<Permission> Permissions = permissionMapper.selectList(queryWrapper);
        return getChildPerms(Permissions, 0);
    }

    @Override
    public List<RouterVo> buildMenus(List<Permission> permissions) {
        // 将菜单列表转换为路由列表
        List<RouterVo> routers = new ArrayList<>();
        permissions.forEach(menu -> {
            RouterVo router = new RouterVo();
            router.setId(menu.getId());
            router.setName(menu.getName());
            router.setPath(menu.getPath());
            router.setComponent(menu.getComponent());
            if (menu.getRedirect() != null && !menu.getRedirect().isEmpty()) {
                router.setRedirect(menu.getRedirect());
            }
            MetaVo meta = BeanUtil.copyProperties(menu, MetaVo.class);
            router.setMeta(meta);
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                router.setAlwaysShow(true);
                router.setChildren(buildMenus(menu.getChildren()));
            }
            routers.add(router);
        });
        return routers;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addPermission(Permission permission) {
        return permissionMapper.insert(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePermission(Permission permission) {
        return permissionMapper.updateById(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePermissionById(Integer id) {
        // 查询子菜单
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Permission::getParentId , id);
        List<Permission> menus = permissionMapper.selectList(queryWrapper);
        // 判断是否有子菜单
        if (!menus.isEmpty()) {
            for (Permission menu : menus) {
                deleteMenuAll(menu.getId());
            }
        }else{
            deleteMenuAll(id);
        }
        return 1;
    }

    private void deleteMenuAll(Integer permissionId) {
        // 角色权限关联表
        rolePermissionRelationMapper.delete(new QueryWrapper<RolePermissionRelation>().eq("permission_id", permissionId));
        // 删除权限
        permissionMapper.deleteById(permissionId);
    }


    @Override
    public List<Permission> selectAllPermissionList() {
        return permissionMapper.selectList(null);
    }

    @Override
    public List<TreeSelect> buildMenuTreeSelect(List<Permission> menus) {
        if (CollUtil.isNotEmpty(menus)) {
            List<Permission> menuTrees = buildMenuTree(menus);
            return menuTrees.stream().map(TreeSelect::new).collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public List<Permission> getMenuByUserId(Integer userId) {
        List<Permission> menuList;
        User user = userMapper.selectById(userId);
        // 判断是否是超级管理员
        if (user.getIsAdmin()) {
            QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByAsc("sort");
            menuList = permissionMapper.selectList(queryWrapper);
        }else {
            menuList = userRelationDao.getUserMenuByUserId(userId);
        }
        return getChildPerms(menuList, 0);
    }

    private List<Permission> buildMenuTree(List<Permission> menus) {
        List<Permission> returnList = new ArrayList<>();
        for (Permission t : menus) {
            // 根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId() == 0) {
                recursionFn(menus, t);
                returnList.add(t);
            }
        }
        if (returnList.isEmpty()) {
            returnList = menus;
        }
        return returnList;
    }

    /**
     * 根据父节点的ID获取所有子节点
     * @param list 分类表
     * @param parentId 传入的父节点ID
     * @return String
     */
    public List<Permission> getChildPerms(List<Permission> list, int parentId) {
        List<Permission> allPermissionList = selectAllPermissionList();
        List<Permission> returnList = new ArrayList<>();
        for (Permission t : list) {
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId() == parentId) {
                recursionFn(allPermissionList, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    /**
     * 递归列表
     *
     * @param list 分类表
     * @param t 传入的父节点
     */
    private void recursionFn(List<Permission> list, Permission t){
        // 得到子节点列表
        List<Permission> childList = getChildList(list, t);
        t.setChildren(childList);
        for (Permission tChild : childList) {
            if (hasChild(list, tChild)) {
                // 判断是否有子节点
                for (Permission n : childList) {
                    recursionFn(list, n);
                }
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<Permission> getChildList(List<Permission> list, Permission t) {
        List<Permission> tList = new ArrayList<>();
        for (Permission menu : list) {
            if (menu.getParentId().equals(t.getId())) {
                tList.add(menu);
            }
        }
        return tList;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<Permission> list, Permission t) {
        return !getChildList(list, t).isEmpty();
    }
}
