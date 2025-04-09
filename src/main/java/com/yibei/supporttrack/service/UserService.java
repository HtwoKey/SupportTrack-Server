package com.yibei.supporttrack.service;

import com.yibei.supporttrack.entity.dto.UpdateUserParam;
import com.yibei.supporttrack.entity.dto.UpdateUserPasswordParam;
import com.yibei.supporttrack.entity.dto.UserQueryParam;
import com.yibei.supporttrack.entity.po.User;
import com.yibei.supporttrack.entity.dto.AddUserParam;
import com.yibei.supporttrack.entity.vo.UserVo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService {

    UserDetails loadUserByUsername(String username);

    User getUserByUsername(String username);

    String login(String username,String password);

    String refreshToken(String token);

    void logout(String token);

    User addUser(AddUserParam userParam);

    int delete(Integer id);

    int batchDelete(Integer[] ids);

    int update(UpdateUserParam user);

    List<UserVo> list(UserQueryParam param);

    UserVo getItem(Integer id);

    int restPassword(Integer id);

    int updatePassword(UpdateUserPasswordParam updatePasswordParam);
}
