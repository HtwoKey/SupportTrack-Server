package com.yibei.supporttrack.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.yibei.supporttrack.entity.po.Role;
import com.yibei.supporttrack.mapper.RoleMapper;
import com.yibei.supporttrack.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
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
}
