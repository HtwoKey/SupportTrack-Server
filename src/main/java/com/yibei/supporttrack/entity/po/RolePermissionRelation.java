package com.yibei.supporttrack.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@TableName("role_permission_relations")

public class RolePermissionRelation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer roleId;
    private Integer permissionId;
    private Date grantedAt;

    public RolePermissionRelation(Integer roleId, Integer permissionId, Date grantedAt) {
        this.roleId = roleId;
        this.permissionId = permissionId;
        this.grantedAt = grantedAt;
    }

}
