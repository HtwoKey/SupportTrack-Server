package com.yibei.supporttrack.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.yibei.supporttrack.entity.bo.SystemUserDetails;
import com.yibei.supporttrack.entity.dto.UpdateUserParam;
import com.yibei.supporttrack.entity.dto.UpdateUserPasswordParam;
import com.yibei.supporttrack.entity.dto.UserQueryParam;
import com.yibei.supporttrack.entity.po.Permission;
import com.yibei.supporttrack.entity.po.User;
import com.yibei.supporttrack.entity.po.UserRoleRelation;
import com.yibei.supporttrack.entity.dto.AddUserParam;
import com.yibei.supporttrack.entity.vo.UserVo;
import com.yibei.supporttrack.exception.ApiException;
import com.yibei.supporttrack.exception.Asserts;
import com.yibei.supporttrack.mapper.UserMapper;
import com.yibei.supporttrack.mapper.UserRoleRelationMapper;
import com.yibei.supporttrack.service.PermissionService;
import com.yibei.supporttrack.service.UserService;
import com.yibei.supporttrack.utils.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_PASSWORD = "123456";

    @Autowired
    private PermissionService permissionService;
    @Autowired
    private CacheServiceImpl cacheService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserRoleRelationMapper userRoleRelationMapper;


    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = getUserByUsername(username);
        if (user != null) {
            List<Permission> resourceList = permissionService.getPermissionList(user.getUserId());
            try {
                user.setLastLogin(new Date());
                userMapper.updateById(user);
            } catch (Exception e) {
                log.error("更新最后登录时间失败: {}", e.getMessage());
            }
            return new SystemUserDetails(user, resourceList);
        }
        throw new UsernameNotFoundException("用户不存在");
    }

    @Override
    public User getUserByUsername(String username) {
        // 输入参数校验
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        // 尝试从缓存中获取用户
        User user = cacheService.getUser(username);
        if (user != null) {
            // 如果用户存在但已被禁用，清理缓存并抛出自定义异常
            if (!Boolean.TRUE.equals(user.getIsActive())) {
                cacheService.delUser(username);
                throw new ApiException("帐号已被禁用");
            }
            return user; // 缓存中的用户状态有效，直接返回
        }

        // 如果缓存中不存在用户，则从数据库查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        user = userMapper.selectOne(queryWrapper);

        if (user != null) {
            // 如果用户存在但已被禁用，抛出自定义异常
            if (!Boolean.TRUE.equals(user.getIsActive())) {
                throw new ApiException("帐号已被禁用");
            }
            // 更新缓存
            cacheService.setUser(user);
        }

        return user; // 返回查询结果，可能为 null
    }

    @Override
    public String login(String username, String password) {
        UserDetails userDetails = loadUserByUsername(username);
        if (passwordEncoder.matches(password, userDetails.getPassword())) {
            String token = jwtTokenUtil.generateToken(userDetails);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            return token;
        } else {
            Asserts.fail("密码不正确");
        }
        return "";
    }

    @Override
    public String refreshToken(String token) {
        return jwtTokenUtil.refreshHeadToken(token);
    }

    @Override
    public void logout(String token) {
        String username = jwtTokenUtil.getUserNameFromToken(token);
        cacheService.delUser(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User addUser(AddUserParam userParam) {
        if (userMapper.selectCount(new QueryWrapper<User>().eq("username", userParam.getUsername())) > 0) {
            return null;
        }
        User user = new User();
        user.setUsername(userParam.getUsername());
        user.setEmail(userParam.getEmail());
        user.setFullName(userParam.getFullName());
        user.setPhone(userParam.getPhone());
        user.setAvatar(userParam.getAvatar());
        user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        if (userMapper.insert(user) > 0) {
            insertUserRoleRelations(user.getUserId(), userParam.getRoles());
        }
        return user;
    }
    private void insertUserRoleRelations(Integer userId, Integer[] roles) {
        if (roles != null && roles.length > 0) {
            List<UserRoleRelation> relations = new ArrayList<>();
            for (Integer roleId : roles) {
                UserRoleRelation relation = new UserRoleRelation();
                relation.setUserId(userId);
                relation.setRoleId(roleId);
                relations.add(relation);
            }
            userRoleRelationMapper.insert(relations); // 批量插入
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Integer id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getIsAdmin()) {
            return 0;
        }
        try {
            userMapper.deleteById(id);
            userRoleRelationMapper.delete(new QueryWrapper<UserRoleRelation>().eq("user_id", id));
            cacheService.delUser(user.getUsername());
            cacheService.delPermissionList(id);
        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage());
            throw e; // 确保事务回滚
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(Integer[] ids) {
        for (Integer id : ids) {
            delete(id);
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(UpdateUserParam param) {
        // 参数校验
        if (param == null || param.getUserId() == null) {
            log.error("参数非法, param:{}", param);
            throw new IllegalArgumentException("参数非法");
        }

        // 查询用户并校验是否存在
        User user = userMapper.selectById(param.getUserId());
        if (user == null) {
            log.warn("用户不存在, userId:{}", param.getUserId());
            Asserts.fail("用户不存在");
        }

        // 显式设置允许更新的字段
        if (param.getUsername() != null) user.setUsername(param.getUsername());
        if (param.getFullName() != null) user.setFullName(param.getFullName());
        if (param.getEmail() != null) user.setEmail(param.getEmail());
        if (param.getPhone() != null) user.setPhone(param.getPhone());
        if (param.getAvatar() != null) user.setAvatar(param.getAvatar());
        if (param.getIsActive() != null) user.setIsActive(param.getIsActive());

        log.debug("更新用户信息: {}", user);

        // 更新用户信息
        int updateResult = userMapper.updateById(user);
        if (updateResult <= 0) {
            log.error("用户更新失败, userId:{}", param.getUserId());
            throw new RuntimeException("用户更新失败");
        }

        // 更新角色关系
        updateRoles(param.getUserId(), param.getRoles());

        // 更新缓存
        try {
            cacheService.delUser(user.getUsername());
            cacheService.setUser(user);
        } catch (Exception e) {
            log.error("缓存更新失败, userId:{}, error:{}", param.getUserId(), e.getMessage(), e);
            // 缓存更新失败不影响主流程，继续执行
        }

        return updateResult;
    }

    /**
     * 更新用户角色关系
     */
    private void updateRoles(Integer userId, Integer[] roles) {
        if (roles == null || roles.length == 0) {
            return; // 如果没有角色，直接返回
        }

        // 删除旧的角色关系
        QueryWrapper<UserRoleRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        userRoleRelationMapper.delete(queryWrapper);

        // 批量插入新的角色关系
        List<UserRoleRelation> relations = Arrays.stream(roles)
                .map(roleId -> {
                    UserRoleRelation relation = new UserRoleRelation();
                    relation.setUserId(userId);
                    relation.setRoleId(roleId);
                    return relation;
                })
                .collect(Collectors.toList());

        userRoleRelationMapper.insert(relations);
    }

    @Override
    public List<UserVo> list(UserQueryParam param) {
        if (param.getPageNum() == null || param.getPageSize() == null) {
            Asserts.fail("分页参数不能为空");
        }
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("user_id", "username", "full_name", "email","phone","avatar", "created_at", "is_active", "last_login");
        if (StrUtil.isNotEmpty(param.getKeyword())) {
            queryWrapper.like("username", param.getKeyword()).or().like("full_name", param.getKeyword());
        }
        if (param.getStatus() != null ) {
            if (param.getStatus().equals("true")) {
                queryWrapper.eq("is_active", 1);
            } else if (param.getStatus().equals("false")) {
                queryWrapper.eq("is_active", 0);
            }
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        if (CollUtil.isNotEmpty(userList)) {
            return userList.stream().map(this::convertToUserVo).toList();
        }
        return List.of();
    }

    private UserVo convertToUserVo(User user) {
        UserVo userVo = new UserVo();
        userVo.setUserId(user.getUserId());
        userVo.setUsername(user.getUsername());
        userVo.setEmail(user.getEmail());
        userVo.setFullName(user.getFullName());
        userVo.setPhone(user.getPhone());
        userVo.setAvatar(user.getAvatar());
        userVo.setIsActive(user.getIsActive());
        userVo.setCreatedAt(user.getCreatedAt());
        userVo.setLastLogin(user.getLastLogin());
        // 设置角色信息
        List<Integer> roles = userRoleRelationMapper.selectList(new QueryWrapper<UserRoleRelation>().eq("user_id", user.getUserId())).stream().map(UserRoleRelation::getRoleId).toList();
        userVo.setRoles(roles.toArray(new Integer[0]));
        return userVo;
    }

    @Override
    public UserVo getItem(Integer id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            return convertToUserVo(user);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int restPassword(Integer id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            if (userMapper.updateById(user) > 0) {
                cacheService.delUser(user.getUsername());
                cacheService.setUser(user);
                return 1;
            }
        }
        return 0;
    }

    @Override
    public int updatePassword(UpdateUserPasswordParam updatePasswordParam) {
        User user = userMapper.selectById(updatePasswordParam.getId());
        if (user != null) {
            user.setPasswordHash(passwordEncoder.encode(updatePasswordParam.getNewPassword()));
            if (userMapper.updateById(user) > 0) {
                cacheService.delUser(user.getUsername());
                cacheService.setUser(user);
                return 1;
            }
        }
        return 0;
    }

    @Override
    public int changeStatus(UpdateUserParam param) {
        User user = userMapper.selectById(param.getUserId());
        if (user != null) {
            user.setIsActive(param.getIsActive());
            if (userMapper.updateById(user) > 0) {
                cacheService.delUser(user.getUsername());
                cacheService.setUser(user);
                return 1;
            }
        }
        return 0;
    }
}
