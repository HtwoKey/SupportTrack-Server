package com.yibei.supporttrack.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@TableName("permission")
public class Permission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 父级id
     */
    private Integer parentId;

    /**
     * 菜单标题
     */
    private String title;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 路由名称
     */
    private String name;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件地址
     */
    private String component;

    /**
     * 重定向地址
     */
    private String redirect;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 普通页面归属菜单
     */
    private String activeMenu;

    /**
     * 菜单类型：1.主菜单，2，二级菜单，3.附属页面
     */
    private String type;

    /**
     * 外链地址
     */
    private String isLink;

    /**
     * 是否隐藏
     */
    private Integer isHide;

    /**
     * 是否全屏
     */
    private Integer isFull;

    /**
     * 是否固定再tabs nav
     */
    private Integer isAffix;

    /**
     * 是否缓存
     */
    private Integer isKeepAlive;


    /** 子菜单 */
    @TableField(exist = false)
    private List<Permission> children = new ArrayList<>();

    /**
     * 名称
     */
    private String permissionName;

    /**
     * 资源路径
     */
    private String uri;

    /**
     * 请求方式
     */
    private String method;

    /**
     * 说明
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;
}
