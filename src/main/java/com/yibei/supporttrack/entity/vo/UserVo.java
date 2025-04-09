package com.yibei.supporttrack.entity.vo;

import com.yibei.supporttrack.entity.po.UserRoleRelation;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class UserVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer userId;
    /**
     * 用户名
     */
    private String username;

    /**
     * 电子邮件地址
     */
    private String email;

    /**
     * 完整姓名
     */
    private String fullName;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 部门ID
     */
    private Integer departmentId;

    /**
     * 账号状态
     */
    private Boolean isActive;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 最后登录时间
     */
    private Date lastLogin;

    /**
     * 角色关系
     */
    private List<UserRoleRelation> userRoleRelations;
}
