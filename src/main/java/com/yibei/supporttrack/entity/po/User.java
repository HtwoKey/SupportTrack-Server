package com.yibei.supporttrack.entity.po;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("users")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码哈希值
     */
    private String passwordHash;

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
     * 头像URL
     */
    private String avatar;

    /**
     * 部门ID
     */
    private Integer departmentId;

    /**
     * 账号状态
     */
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 最后登录时间
     */
    private Date lastLogin;
    /**
     * 是否是管理员
     */
    @Builder.Default
    private Boolean isAdmin = false;
}
