package com.SmartHire.seekerService.utils;

import com.SmartHire.seekerService.dto.seekerTableDto.EducationExperienceDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EducationYearsValidator
    implements ConstraintValidator<ValidEducationYears, EducationExperienceDTO> {

  @Override
  public boolean isValid(EducationExperienceDTO dto, ConstraintValidatorContext context) {

    String start = dto.getStartYear();
    String end = dto.getEndYear();

    if (start == null || end == null) {
      return true;
    }

    int startYear = Integer.parseInt(start);
    int endYear = Integer.parseInt(end);

    // 使用全局 CURRENT_YEAR
    int current = EducationExperienceDTO.YearConstants.CURRENT_YEAR;

    // 开始年份不能超过当前年份
    if (startYear > current) {
      addViolation(context, "startYear", "开始年份不能超过当前年份");
      return false;
    }

    // 结束年份 <= 开始年份 + 6
    if (endYear > startYear + 6) {
      addViolation(context, "endYear", "结束年份不能超过开始年份 + 6");
      return false;
    }

    return true;
  }

  private void addViolation(ConstraintValidatorContext context, String field, String message) {
    context.disableDefaultConstraintViolation();
    context
        .buildConstraintViolationWithTemplate(message)
        .addPropertyNode(field)
        .addConstraintViolation();
  }
}
