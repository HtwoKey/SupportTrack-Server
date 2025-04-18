package com.yibei.supporttrack.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.yibei.supporttrack.entity.po.Role;
import com.yibei.supporttrack.entity.po.RolePermissionRelation;
import com.yibei.supporttrack.mapper.RoleMapper;
import com.yibei.supporttrack.mapper.RolePermissionRelationMapper;
import com.yibei.supporttrack.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionRelationMapper rolePermissionRelationMapper;

    public RoleServiceImpl(RoleMapper roleMapper, RolePermissionRelationMapper rolePermissionRelationMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionRelationMapper = rolePermissionRelationMapper;
    }


    @Override
    public List<Role> list(String keyword, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        if (keyword != null) {
            return roleMapper.selectList(new QueryWrapper<Role>().like("role_name", keyword));
        }
        return roleMapper.selectList(null);
    }

    @Override
    public int update(Role role) {
        return roleMapper.updateById(role);
    }

    @Override
    public int delete(List<Integer> ids) {
        if (ids.size() == 1) {
            deleteRelation(ids.getFirst());
            return roleMapper.deleteById(ids.getFirst());
        }
        for (Integer id : ids) {
            deleteRelation(id);
        }
        return roleMapper.deleteByIds(ids);
    }

    @Override
    public List<Role> allList() {
        return roleMapper.selectList(null);
    }

    @Override
    public int create(Role role) {
        return roleMapper.insert(role);
    }

    @Override
    public List<RolePermissionRelation> getRolePermissionRelationList(Integer roleId) {
        if (roleId != null) {
            return rolePermissionRelationMapper.selectList(new QueryWrapper<RolePermissionRelation>().eq("role_id", roleId));
        }
        return List.of();
    }

    /**
     * 删除角色和菜单关系
     * @param roleId 角色ID
     */

    public void deleteRelation(Integer roleId) {
        rolePermissionRelationMapper.delete(new QueryWrapper<RolePermissionRelation>().eq("role_id", roleId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setRolePermissionRelation(Integer roleId, List<Integer> menuIds) {
        // 防御性空值处理
        if (menuIds == null) return 0;

        // 删除原有关系
        deleteRelation(roleId);

        if (!menuIds.isEmpty()) {
            // 使用流式处理优化对象构造
            List<RolePermissionRelation> relations = menuIds.stream()
                    .map(menuId -> new RolePermissionRelation(
                            roleId,
                            menuId,
                            new Date()))
                    .collect(Collectors.toList());

            // 批量插入
            rolePermissionRelationMapper.insert(relations);
            return relations.size(); // 返回实际插入数量更合理
        }
        return 0;
    }
}
