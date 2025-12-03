package com.SmartHire.adminService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * HR信息表
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
@Getter
@Setter
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
