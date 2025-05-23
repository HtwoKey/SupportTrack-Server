package com.yibei.supporttrack.controller;

import com.yibei.supporttrack.entity.po.Role;
import com.yibei.supporttrack.entity.po.RolePermissionRelation;
import com.yibei.supporttrack.entity.vo.CommonPage;
import com.yibei.supporttrack.entity.vo.CommonResult;
import com.yibei.supporttrack.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/manage/role")
public class
RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/list")
    @ResponseBody
    public CommonResult<CommonPage<?>> list(@RequestParam("pageNum") Integer pageNum,
                                            @RequestParam("pageSize") Integer pageSize,
                                            @RequestParam(value = "keyword", required = false) String keyword) {
        List<Role> list = roleService.list(keyword, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(list));
    }

    @GetMapping("/list/all")
    @ResponseBody
    public CommonResult<?> allList() {
        List<Role> list = roleService.allList();
        return CommonResult.success(list);
    }


    @PostMapping("/add")
    @ResponseBody
    public CommonResult<?> create(@RequestBody Role role) {
        int count = roleService.create(role);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 修改角色
     */
    @PutMapping("/update")
    @ResponseBody
    public CommonResult<?> update(@RequestBody Role role) {
        int count = roleService.update(role);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public CommonResult<?> delete(@PathVariable Integer id) {
        if (id == null) {
            return CommonResult.failed("请选择需要删除的角色");
        }
        List<Integer> ids = new java.util.ArrayList<>();
        ids.add(id);
        int count = roleService.delete(ids);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 批量删除角色
     */
    @DeleteMapping("/delete/batch")
    @ResponseBody
    public CommonResult<?> deleteBatch(@RequestParam("ids") List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return CommonResult.failed("请选择需要删除的角色");
        }
        int count = roleService.delete(ids);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 获取角色菜单
     */
    @GetMapping("/menu/{roleId}")
    @ResponseBody
    public CommonResult<?> getMenuList(@PathVariable Integer roleId) {
        List<RolePermissionRelation> menuList = roleService.getRolePermissionRelationList(roleId);
        List<Integer> idList = new ArrayList<>();
        for ( RolePermissionRelation menu: menuList){
            int id  = menu.getId();
            idList.add(id);
        }
        return CommonResult.success(idList);
    }

    /**
     * 设置角色菜单
     */
    @PostMapping("/menu/set")
    @ResponseBody
    public CommonResult<?> allocMenu(@RequestParam("roleId") Integer roleId,
                                     @RequestParam("menuIds") List<Integer> menuIds) {
        if (roleId == null || menuIds == null) {
            return CommonResult.failed("请选择需要分配的菜单");
        }
        int count = roleService.setRolePermissionRelation(roleId, menuIds);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}
