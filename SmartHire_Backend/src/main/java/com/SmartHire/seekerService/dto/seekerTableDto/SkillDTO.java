package com.SmartHire.seekerService.dto.seekerTableDto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Data;

@Data
public class SkillDTO {
    public interface Create extends Default {
    }

    public interface Update extends Default {
    }

    private Long id;

    @NotBlank(message = "技能名称不能为空", groups = Create.class)
    @Size(max = 50, message = "技能名称不能超过10个字符")
    private String skillName;

    @NotNull(message = "熟练度不能为空", groups = Create.class)
    @Min(value = 0, message = "熟练度只能为0-2之间的整数")
    @Max(value = 2, message = "熟练度只能为0-2之间的整数")
    private Integer level;
}
