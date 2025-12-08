package com.SmartHire;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** SmartHire 后端应用主启动类 统一管理所有模块的启动配置 */
@SpringBootApplication(scanBasePackages = "com.SmartHire")
@MapperScan({
  "com.SmartHire.userAuthService.mapper",
  "com.SmartHire.hrService.mapper",
  "com.SmartHire.seekerService.mapper",
  "com.SmartHire.messageService.mapper",
  "com.SmartHire.recruitmentService.mapper"
})
public class SmartHireBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(SmartHireBackendApplication.class, args);
  }
}
