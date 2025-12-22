package com.SmartHire.recruitmentService.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class InternJobRecommendationsDTO implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  private List<InternJobItemDTO> jobs;
}


