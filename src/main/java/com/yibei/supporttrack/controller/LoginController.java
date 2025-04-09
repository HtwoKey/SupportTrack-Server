package com.yibei.supporttrack.controller;

import com.yibei.supporttrack.entity.dto.UpdateUserParam;
import com.yibei.supporttrack.entity.dto.UpdateUserPasswordParam;
import com.yibei.supporttrack.entity.dto.UserLoginParam;
import com.yibei.supporttrack.entity.po.User;
import com.yibei.supporttrack.entity.vo.CommonResult;
import com.yibei.supporttrack.entity.vo.RouterVo;
import com.yibei.supporttrack.entity.vo.UserVo;
import com.yibei.supporttrack.service.PermissionService;
import com.yibei.supporttrack.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("")
public class LoginController {

    @Value("${jwt.tokenHead}")
    private String tokenHead;
    @Value("${jwt.header}")
    private String header;
    private final UserService userService;
    private final PermissionService menuService;

    public LoginController(UserService userService, PermissionService menuService) {
        this.userService = userService;
        this.menuService = menuService;
    }

    @RequestMapping("/login")
    @ResponseBody
    public CommonResult<?> login(@Validated @RequestBody UserLoginParam userLoginParam) {
        String token = userService.login(userLoginParam.getUsername(), userLoginParam.getPassword());
        if (token == null) {
            return CommonResult.validateFailed("用户名或密码错误");
        }
        Map<String, String> tokenMap = new HashMap<>(2);
        tokenMap.put("token", token);
        tokenMap.put("tokenHead", tokenHead);
        return CommonResult.success(tokenMap);
    }

    @GetMapping("/refreshToken")
    @ResponseBody
    public CommonResult<?> refreshToken(HttpServletRequest request) {
        String token = request.getHeader(header);
        String refreshToken = userService.refreshToken(token);
        if (refreshToken == null) {
            return CommonResult.failed("token已经过期！");
        }
        Map<String, String> tokenMap = new HashMap<>(2);
        tokenMap.put("token", refreshToken);
        tokenMap.put("tokenHead", tokenHead);
        return CommonResult.success(tokenMap);
    }
    @PostMapping("/logout")
    @ResponseBody
    public CommonResult<?> logout(HttpServletRequest request) {
        String token = request.getHeader(header);
        userService.logout(token);
        return CommonResult.success(null);
    }

    /**
     * 用户个人菜单接口
     * @param principal 用户
     * @return 菜单list
     */
    @GetMapping("/auth/menu")
    @ResponseBody
    public CommonResult<List<RouterVo>> getAdminUserMenu(Principal principal) {
        if(principal==null){
            return CommonResult.unauthorized(null);
        }
        String username = principal.getName();
        User user = userService.getUserByUsername(username);
        return CommonResult.success(menuService.buildMenus(menuService.getPermissionByUserId(user.getUserId())));
    }



    /**
     * 获取个人信息
     * @param principal 用户标识
     * @return 用户信息
     */
    @GetMapping("/auth/info")
    @ResponseBody
    public CommonResult<?> getAdminInfo(Principal principal) {
        if(principal==null){
            return CommonResult.unauthorized(null);
        }
        String username = principal.getName();
        User user = userService.getUserByUsername(username);
        UserVo userVO = new UserVo();
        BeanUtils.copyProperties(user, userVO);
        return CommonResult.success(userVO);
    }

    /**
     * 修改个人信息
     * @param userInfo 个人信息参数
     * @return 修改结果
     */
    @ResponseBody
    @PutMapping("/auth/updateInfo")
    public CommonResult<?> UpdateInfo(@Validated @RequestBody UpdateUserParam userInfo, Principal principal) {

        String username = principal.getName();
        if (!userInfo.getUsername().equals(username)){
            return CommonResult.failed("提交参数不合法");
        }
        int status = userService.update(userInfo);
        if (status > 0){
            return CommonResult.success("ok");
        }
        return CommonResult.unauthorized(null);
    }

    /**
     * 修改密码
     * @param updatePasswordParam 密码参数
     * @param principal 用户标识
     * @return 修改结果
     */
    @PostMapping("/auth/updatePassword")
    @ResponseBody
    public CommonResult<?> updatePassword(@Validated @RequestBody UpdateUserPasswordParam updatePasswordParam, Principal principal) {

        if (principal != null){
            String username = principal.getName();
            // 判断是否是同一用户
            if (!updatePasswordParam.getUsername().equals(username)){
                return CommonResult.failed("提交参数不合法");
            }
            int i = userService.updatePassword(updatePasswordParam);
            if (i > 0){
                return CommonResult.success("ok");
            }
        }
        return CommonResult.unauthorized(null);
    }
}
