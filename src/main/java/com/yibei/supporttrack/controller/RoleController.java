package com.yibei.supporttrack.controller;

import com.yibei.supporttrack.entity.po.Role;
import com.yibei.supporttrack.entity.vo.CommonPage;
import com.yibei.supporttrack.entity.vo.CommonResult;
import com.yibei.supporttrack.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        List<Integer> ids = List.of(id);
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
    public CommonResult<?> deleteBatch(@RequestParam("id") List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return CommonResult.failed("请选择需要删除的角色");
        }
        int count = roleService.delete(ids);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}
