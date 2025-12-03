package com.SmartHire.adminService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 证书表（在线简历信息）
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
@Getter
@Setter
public class Certificate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 证书ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 求职者ID
     */
    private Long jobSeekerId;

    /**
     * 证书名称
     */
    private String certificateName;

    /**
     * 证书图片URL
     */
    private String certificateUrl;

    /**
     * 创建时间
     */
    private Date createdAt;
}
