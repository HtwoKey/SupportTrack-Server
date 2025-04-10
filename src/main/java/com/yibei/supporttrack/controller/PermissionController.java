package com.yibei.supporttrack.controller;

import com.yibei.supporttrack.entity.dto.MenuQueryParam;
import com.yibei.supporttrack.entity.po.Permission;
import com.yibei.supporttrack.entity.vo.*;
import com.yibei.supporttrack.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manage/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 获取菜单列表
     */
    @GetMapping("/list")
    @ResponseBody
    public CommonResult<CommonPage<RouterVo>> list(MenuQueryParam menu) {
        List<Permission> permissions = permissionService.selectPermissionList(menu);
        return CommonResult.success(CommonPage.restPage(permissionService.buildMenus(permissions)));
    }

    /**
     * 新增菜单
     */
    @PostMapping("/add")
    @ResponseBody
    public CommonResult<?> create(@RequestBody Permission permission) {
        int count = permissionService.addPermission(permission);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 修改菜单
     */
    @PutMapping("/update")
    @ResponseBody
    public CommonResult<?> update(@RequestBody Permission permission) {
        int count = permissionService.updatePermission(permission);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public CommonResult<?> delete(@PathVariable Integer id) {
        int count = permissionService.deletePermissionById(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 获取菜单下拉树列表
     */
    @GetMapping("/tree")
    @ResponseBody
    public CommonResult<List<TreeSelect>> treeSelect() {
        List<Permission> menus = permissionService.selectAllPermissionList();
        List<TreeSelect> treeSelects = permissionService.buildMenuTreeSelect(menus);
        return CommonResult.success(treeSelects);
    }
}
