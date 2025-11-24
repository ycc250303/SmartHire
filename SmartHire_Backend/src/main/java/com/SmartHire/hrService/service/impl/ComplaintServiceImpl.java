package com.SmartHire.hrService.service.impl;

import com.SmartHire.hrService.model.Complaint;
import com.SmartHire.hrService.mapper.ComplaintMapper;
import com.SmartHire.hrService.service.ComplaintService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 投诉服务实现类
 */
@Service
public class ComplaintServiceImpl extends ServiceImpl<ComplaintMapper, Complaint> implements ComplaintService {

}

