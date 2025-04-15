package com.yibei.supporttrack.entity.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RouterVo {

    private Integer id;

    /**
     * 路由名称
     */
    private String name;

    /**
     * 路由组件路径
     */
    private String component;

    /**
     * 路由地址
     */
    private String path;
    /**
     * 重定向地址
     */
    private String redirect;
    /**
     * 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
     */
    private Boolean alwaysShow;
    /**
     * 其他元素
     */
    private MetaVo meta;

    /**
     * 子路由
     */
    private List<RouterVo> children;

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

}
