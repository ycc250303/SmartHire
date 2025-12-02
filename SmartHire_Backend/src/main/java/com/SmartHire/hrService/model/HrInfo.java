package com.SmartHire.hrService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * HR信息实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_info")
public class HrInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * HR ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 职位
     */
    private String position;

    /**
     * 工作电话
     */
    private String workPhone;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

