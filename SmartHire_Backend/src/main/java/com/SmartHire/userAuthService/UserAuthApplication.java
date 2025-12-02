package com.SmartHire.userAuthService;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

/**
 * UserAuthService 启动类 使用 application-userAuth-service.yml 配置文件
 *
 * @author SmartHire Team
 */
@SpringBootApplication(scanBasePackages = "com.SmartHire")
@MapperScan("com.SmartHire.userAuthService.mapper")
public class UserAuthApplication {

  public static void main(String[] args) {
    // 创建 SpringApplication 实例
    SpringApplication app = new SpringApplication(UserAuthApplication.class);

    // 注意：profile 名称是文件名中 application- 和 .yml 之间的部分
    app.setAdditionalProfiles("userAuth-service");

    // 启动应用并获取环境信息
    var context = app.run(args);
    Environment env = context.getEnvironment();

    // 打印配置信息，用于调试
    System.out.println("==========================================");
    System.out.println("应用名称: " + env.getProperty("spring.application.name"));
    System.out.println("服务端口: " + env.getProperty("server.port"));
    System.out.println("上下文路径: " + env.getProperty("server.servlet.context-path"));
    System.out.println("激活的 Profile: " + String.join(", ", env.getActiveProfiles()));
    System.out.println("==========================================");
  }
}
