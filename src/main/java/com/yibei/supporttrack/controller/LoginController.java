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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
        try {
            String token = userService.login(userLoginParam.getUsername(), userLoginParam.getPassword());
            if (token == null) {
                return CommonResult.validateFailed("用户名或密码错误");
            }
            return buildTokenResponse(token);
        } catch (Exception e) {
            log.error("Error during login: {}", e.getMessage(), e);
            return CommonResult.failed("系统异常，请稍后再试");
        }
    }

    @GetMapping("/refreshToken")
    @ResponseBody
    public CommonResult<?> refreshToken(HttpServletRequest request) {
        try {
            String token = request.getHeader(header);
            if (token == null || token.isEmpty()) {
                return CommonResult.failed("无效的token");
            }
            String refreshToken = userService.refreshToken(token);
            if (refreshToken == null) {
                return CommonResult.failed("token已经过期！");
            }
            return buildTokenResponse(refreshToken);
        } catch (Exception e) {
            log.error("Error during token refresh: {}", e.getMessage(), e);
            return CommonResult.failed("系统异常，请稍后再试");
        }
    }
    @PostMapping("/logout")
    @ResponseBody
    public CommonResult<?> logout(HttpServletRequest request) {
        try {
            String token = request.getHeader(header);
            if (token == null || token.isEmpty()) {
                return CommonResult.failed("无效的token");
            }
            userService.logout(token);
            return CommonResult.success(null);
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            return CommonResult.failed("系统异常，请稍后再试");
        }
    }

    /**
     * 用户个人菜单接口
     * @param principal 用户
     * @return 菜单list
     */
    @GetMapping("/auth/menu")
    @ResponseBody
    public CommonResult<List<RouterVo>> getUserMenu(Principal principal) {
        if (principal == null) {
            return CommonResult.unauthorized(null);
        }
        try {
            String username = principal.getName();
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return CommonResult.unauthorized(null);
            }
            return CommonResult.success(menuService.buildMenus(menuService.getPermissionByUserId(user.getUserId())));
        } catch (Exception e) {
            log.error("Error fetching user menu: {}", e.getMessage(), e);
            return CommonResult.failed("系统异常，请稍后再试");
        }
    }



    /**
     * 获取个人信息
     * @param principal 用户标识
     * @return 用户信息
     */
    @GetMapping("/auth/info")
    @ResponseBody
    public CommonResult<?> getAdminInfo(Principal principal) {
        if (principal == null) {
            return CommonResult.unauthorized(null);
        }
        try {
            String username = principal.getName();
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return CommonResult.unauthorized(null);
            }
            UserVo userVO = new UserVo();
            BeanUtils.copyProperties(user, userVO);
            return CommonResult.success(userVO);
        } catch (Exception e) {
            log.error("Error fetching user info: {}", e.getMessage(), e);
            return CommonResult.failed("系统异常，请稍后再试");
        }
    }

    /**
     * 修改个人信息
     * @param userInfo 个人信息参数
     * @return 修改结果
     */
    @PutMapping("/auth/updateInfo")
    @ResponseBody
    public CommonResult<?> updateInfo(@Validated @RequestBody UpdateUserParam userInfo, Principal principal) {
        if (principal == null) {
            return CommonResult.unauthorized(null);
        }
        try {
            String username = principal.getName();
            if (!userInfo.getUsername().equals(username)) {
                return CommonResult.failed("提交参数不合法");
            }
            int status = userService.update(userInfo);
            if (status > 0) {
                return CommonResult.success("ok");
            }
            return CommonResult.failed("更新失败");
        } catch (Exception e) {
            log.error("Error updating user info: {}", e.getMessage(), e);
            return CommonResult.failed("系统异常，请稍后再试");
        }
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
        if (principal == null) {
            return CommonResult.unauthorized(null);
        }
        try {
            String username = principal.getName();
            if (!updatePasswordParam.getUsername().equals(username)) {
                return CommonResult.failed("提交参数不合法");
            }
            int i = userService.updatePassword(updatePasswordParam);
            if (i > 0) {
                return CommonResult.success("ok");
            }
            return CommonResult.failed("修改密码失败");
        } catch (Exception e) {
            log.error("Error updating password: {}", e.getMessage(), e);
            return CommonResult.failed("系统异常，请稍后再试");
        }
    }

    private CommonResult<Map<String, String>> buildTokenResponse(String token) {
        Map<String, String> tokenMap = new HashMap<>(2);
        tokenMap.put("token", token);
        tokenMap.put("tokenHead", tokenHead);
        return CommonResult.success(tokenMap);
    }
}
