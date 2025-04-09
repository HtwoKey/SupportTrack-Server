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
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

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
        //获取用户信息
        User user = getUserByUsername(username);
        if (user != null) {
            List<Permission> resourceList = permissionService.getPermissionList(user.getUserId());
            return new SystemUserDetails(user,resourceList);
        }
        throw new UsernameNotFoundException("用户不存在");
    }

    @Override
    public User getUserByUsername(String username) {
        // 从缓存获取用户
        User user = cacheService.getUser(username);
        if(user!=null) {
            return  user;
        }
        // 如果没有才从数据库获取
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);
        user = userMapper.selectOne(queryWrapper);
        if(user!=null){
            cacheService.setUser(user);
            return user;
        }
        return null;
    }

    @Override
    public String login(String username, String password) {
        String token;
        // 获取用户信息
        UserDetails userDetails = loadUserByUsername(username);
        if (userDetails.isEnabled()) {
            Asserts.fail("帐号已被禁用");
            return "";
        }
        // 验证密码
        boolean isValid = passwordEncoder.matches(password, userDetails.getPassword());
        if (isValid) {
            // 生成token
            token = jwtTokenUtil.generateToken(userDetails);
            // 更新用户信息
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            return token;
        }else{
            Asserts.fail("密码不正确");
            return "";
        }
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
        //查询是否有相同用户名的用户
        List<User> userList = userMapper.selectList(new QueryWrapper<User>().eq("username", userParam.getUsername()));
        if (CollUtil.isNotEmpty(userList)) {
            return null;
        }
        // 创建用户
        User user = new User();
        user.setUsername(userParam.getUsername());
        user.setEmail(userParam.getEmail());
        user.setFullName(userParam.getFullName());
        user.setPhone(userParam.getPhone());
        user.setAvatar(userParam.getAvatar());
        //将密码进行加密操作
        String encodePassword = passwordEncoder.encode(userParam.getPassword());
        user.setPasswordHash(encodePassword);
        // 插入用户信息
        int i = userMapper.insert(user);
        // 插入用户角色
        if (i > 0) {
            UserRoleRelation userRoleRelation = new UserRoleRelation();
            for(Integer roleId : userParam.getRoles()){
                userRoleRelation.setRoleId(roleId);
                userRoleRelation.setUserId(user.getUserId());
                userRoleRelationMapper.insert(userRoleRelation);
            }
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Integer id) {
        //不能删除超级管理员
        User user = userMapper.selectById(id);
        if (user.getIsAdmin()){
            return 0;
        }
        int i = userMapper.deleteById(id);
        // 删除角色关系
        QueryWrapper<UserRoleRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", id);
        userRoleRelationMapper.delete(queryWrapper);
        // 删除缓存信息
        cacheService.delUser(user.getUsername());
        cacheService.delPermissionList(id);
        return i;
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
        // 查询用户时增加空校验
        User user = userMapper.selectById(param.getUserId());
        if (user == null) {
            log.warn("用户不存在, userId:{}", param.getUserId());
            Asserts.fail("用户不存在");
        }
        // 显式设置允许更新的字段
        if (param.getFullName() != null)
            user.setFullName(param.getFullName());
        if (param.getPhone() != null)
            user.setPhone(param.getPhone());
        if (param.getAvatar() != null)
            user.setAvatar(param.getAvatar());
        if (param.getEmail() != null)
            user.setEmail(param.getEmail());
        if (param.getIsActive() != null)
            user.setIsActive(param.getIsActive());

        log.debug("更新用户信息: {}", user);
        userMapper.updateById(user);
        if (param.getRoles() != null) {
            // 删除角色关系
            QueryWrapper<UserRoleRelation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", param.getUserId());
            userRoleRelationMapper.delete(queryWrapper);
            // 插入角色关系
            for (Integer roleId : param.getRoles()) {
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setRoleId(roleId);
            }
        }
        int i = userMapper.updateById(user);
        // 更新缓存
        if (i > 0){
            cacheService.delUser(user.getUsername());
            cacheService.setUser(user);
        }
        return i;
    }

    @Override
    public List<UserVo> list(UserQueryParam param) {
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("user_id","username","full_name","email","created_at","is_active","phone","avatar");
        if(StrUtil.isNotEmpty(param.getKeyword())){
            queryWrapper.like("username",param.getKeyword());
            queryWrapper.or();
            queryWrapper.like("full_name",param.getKeyword());
        }
        if (param.getStatus() != null) {
            queryWrapper.eq("is_active", param.getStatus());
        }
        if (param.getStartTime() != null && param.getEndTime() != null){
            queryWrapper.between("created_at", param.getStartTime(), param.getEndTime());
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        if (CollUtil.isNotEmpty(userList)){
            List<UserVo> userVoList = new ArrayList<>();
            UserVo userVo = new UserVo();
            for (User user : userList) {
                userVo.setUserId(user.getUserId());
                userVo.setUsername(user.getUsername());
                userVo.setEmail(user.getEmail());
                userVo.setFullName(user.getFullName());
                userVo.setPhone(user.getPhone());
                userVo.setAvatar(user.getAvatar());
                userVo.setDepartmentId(user.getDepartmentId());
                userVo.setIsActive(user.getIsActive());
                userVo.setCreatedAt(user.getCreatedAt());
                userVo.setLastLogin(user.getLastLogin());
                userVoList.add(userVo);
            }
            return userVoList;
        }

        return List.of();
    }

    @Override
    public UserVo getItem(Integer id) {
        User user = userMapper.selectById(id);
        if (user != null){
            UserVo userVo = new UserVo();
            userVo.setUserId(user.getUserId());
            userVo.setUsername(user.getUsername());
            userVo.setEmail(user.getEmail());
            userVo.setFullName(user.getFullName());
            userVo.setDepartmentId(user.getDepartmentId());
            userVo.setIsActive(user.getIsActive());
            userVo.setCreatedAt(user.getCreatedAt());
            userVo.setLastLogin(user.getLastLogin());
            return userVo;
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int restPassword(Integer id) {
        User user = userMapper.selectById(id);
        if (user != null){
            user.setPasswordHash(passwordEncoder.encode("123456"));
            int count = userMapper.updateById(user);
            if (count > 0) {
                // 先删除在更新
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
        if (user != null){
            user.setPasswordHash(passwordEncoder.encode(updatePasswordParam.getNewPassword()));
            int count = userMapper.updateById(user);
            if (count > 0) {
                // 先删除在更新
                cacheService.delUser(user.getUsername());
                cacheService.setUser(user);
                return 1;
            }
        }
        return 0;
    }


}
