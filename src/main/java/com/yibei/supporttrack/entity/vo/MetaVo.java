package com.yibei.supporttrack.entity.vo;

import lombok.Data;


@Data
public class MetaVo {

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 菜单标题
     */
    private String title;

    /**
     * 普通页面所属菜单
     */
    private String activeMenu;

    /**
     * 是否外链
     */
    private Integer isLink;

    /**
     * 是否隐藏
     */
    private Integer isHide;

    /**
     * 是否全屏(示例：数据大屏页面)
     */
    private Integer isFull;

    /**
     * 是否固定在 tabs nav
     */
    private Integer isAffix;

    /**
     * 是否缓存
     */
    private Integer isKeepAlive;

}
