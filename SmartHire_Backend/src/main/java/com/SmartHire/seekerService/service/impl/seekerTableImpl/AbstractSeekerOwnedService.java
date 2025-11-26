package com.SmartHire.seekerService.service.impl.seekerTableImpl;

import com.SmartHire.seekerService.service.JobSeekerService;
import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 提供求职者维度公共能力的基础服务类。
 *
 * @param <M> Mapper 类型
 * @param <T> 实体类型
 */
public abstract class AbstractSeekerOwnedService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    @Autowired
    protected JobSeekerService jobSeekerService;

    /**
     * 获取当前登录求职者 ID。
     */
    protected Long currentSeekerId() {
        return jobSeekerService.getJobSeekerId();
    }

    /**
     * 校验记录归属并返回实体。
     *
     * @param id            实体 ID
     * @param fetcher       根据 ID 查询实体的函数
     * @param ownerGetter   获取实体 ownerId 的函数
     * @param notExistCode  不存在时的错误码
     * @param notBelongCode 归属异常时的错误码
     */
    protected T requireOwnedEntity(Long id,
            Function<Long, T> fetcher,
            Function<T, Long> ownerGetter,
            ErrorCode notExistCode,
            ErrorCode notBelongCode) {
        T entity = fetcher.apply(id);
        if (entity == null) {
            throw new BusinessException(notExistCode);
        }
        if (!Objects.equals(ownerGetter.apply(entity), currentSeekerId())) {
            throw new BusinessException(notBelongCode);
        }
        return entity;
    }
}
