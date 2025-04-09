package com.yibei.supporttrack.controller;

import com.yibei.supporttrack.entity.dto.UpdateUserParam;
import com.yibei.supporttrack.entity.dto.UserQueryParam;
import com.yibei.supporttrack.entity.po.User;
import com.yibei.supporttrack.entity.dto.AddUserParam;
import com.yibei.supporttrack.entity.vo.CommonPage;
import com.yibei.supporttrack.entity.vo.CommonResult;
import com.yibei.supporttrack.entity.vo.UserVo;
import com.yibei.supporttrack.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manage/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ResponseBody
    @PostMapping("/add")
    public CommonResult<?> addUser(@RequestBody AddUserParam userParam){

        User user = userService.addUser(userParam);
        if (user == null) {
            return CommonResult.failed("用户名已存在");
        }
        return CommonResult.success(user);
    }

    /**
     * 删除用户
     */
    @ResponseBody
    @DeleteMapping("/delete/{id}")
    public CommonResult<?> deleteUser(@PathVariable Integer id){
        int count = userService.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed("删除失败");
    }

    /**
     * 批量删除
     * @param ids 用户id
     * @return 删除结果
     */
    @ResponseBody
    @DeleteMapping("/batchDelete")
    public CommonResult<?> deleteUser(Integer[] ids){
        int count = userService.batchDelete(ids);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed("删除失败");
    }

    /**
     * 修改用户信息
     * @param user 修改信息
     * @return 修改结果
     */
    @ResponseBody
    @PutMapping("/update")
    public CommonResult<?> updateUser(@RequestBody UpdateUserParam user) {
        int count = userService.update(user);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed("更新失败");
    }

    /**
     * 用户列表
     * @param param 查询参数
     * @return 用户列表
     */
    @GetMapping("/list")
    @ResponseBody
    public CommonResult<CommonPage<UserVo>> list(UserQueryParam param) {
        List<UserVo> userList = userService.list(param);
        return CommonResult.success(CommonPage.restPage(userList));
    }

    /**
     * 根据用户id获取用户
     * @param id 用户id
     * @return 用户信息
     */
    @GetMapping("/{id}")
    @ResponseBody
    public CommonResult<UserVo> getItem(@PathVariable Integer id) {
        UserVo user = userService.getItem(id);
        return CommonResult.success(user);
    }

    /**
     * 修改用户状态
     * @param param 修改参数
     * @return 修改结果
     */
    @PutMapping("/changeStatus")
    @ResponseBody
    public CommonResult<?> updateStatus(@RequestBody UpdateUserParam param) {
        int count = userService.changeStatus(param);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 重置密码为123456
     * @param param 参数
     * @return 修改结果
     */
    @ResponseBody
    @PutMapping("/restPassword")
    public CommonResult<?> restPassword(@RequestBody UpdateUserParam param) {
        if (param.getUserId() == null){
            return CommonResult.failed("用户id不能为空");
        }
        int count = userService.restPassword(param.getUserId());
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();

    }
}
