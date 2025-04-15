package com.yibei.supporttrack.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("customers")
public class Customer implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String customerName;

    private String contactPerson;

    private String phone;

    private String address;

    private String company;

    private Date createdAt;

    private Date updatedAt;


}
