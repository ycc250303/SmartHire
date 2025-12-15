package com.SmartHire.codeGenerator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/**
 * AdminService 代码生成器
 *
 * @author SmartHire Team 重要提醒：生成的代码会覆盖已存在的文件，请谨慎操作！
 */
public class AdminCodeGenerator {

  private static final Path BASE_RESOURCE_PATH = Paths.get("SmartHire_Backend/src/main/resources");
  private static final Path APPLICATION_YML = BASE_RESOURCE_PATH.resolve("application.yml");
  private static final Path APPLICATION_LOCAL_YML =
      BASE_RESOURCE_PATH.resolve("application-local.yml");
  // TODO：需要修改yml文件名
  private static final Path ADMIN_SERVICE_YML =
      BASE_RESOURCE_PATH.resolve("application-admin-service.yml");

  public static void main(String[] args) {
    DatabaseConfig dbConfig = loadDatabaseConfig();
    generateServiceProfile();

    String projectPath = System.getProperty("user.dir");

    FastAutoGenerator.create(dbConfig.url, dbConfig.username, dbConfig.password)
        .globalConfig(
            builder ->
                builder
                    .author("SmartHire Team")
                    .outputDir(projectPath + "/SmartHire_Backend/src/main/java")
                    .disableOpenDir()
                    .dateType(DateType.ONLY_DATE))
        .packageConfig(
            builder ->
                builder
                    // TODO：需要修改服务文件夹名称
                    .parent("com.SmartHire.adminService")
                    .entity("model")
                    .mapper("mapper")
                    .service("service")
                    .serviceImpl("service.impl"))
        .strategyConfig(
            builder -> {
              // TODO：需要修改服务相关的数据库表
              builder.addInclude(
                  "user",
                  "company",
                  "hr_info",
                  "job_position",
                  "application",
                  "resume",
                  "conversation",
                  "chat_message",
                  "interview",
                  "job_seeker",
                  "education_experience",
                  "work_experience",
                  "project_experience",
                  "skill",
                  "certificate");
              builder.entityBuilder().enableLombok().idType(IdType.AUTO);
              builder.mapperBuilder().enableBaseResultMap().enableBaseColumnList();
              builder
                  .serviceBuilder()
                  .formatServiceFileName("%sService")
                  .formatServiceImplFileName("%sServiceImpl");
              // Controller 手动生成，不自动生成
              builder.controllerBuilder().disable();
            })
        .execute();
  }

  @SuppressWarnings("unchecked")
  private static DatabaseConfig loadDatabaseConfig() {
    // 优先读取 application-local.yml，如果不存在则读取 application.yml
    Path configFile = Files.exists(APPLICATION_LOCAL_YML) ? APPLICATION_LOCAL_YML : APPLICATION_YML;

    if (!Files.exists(configFile)) {
      throw new IllegalStateException(
          "Configuration file not found at " + configFile.toAbsolutePath());
    }

    try (InputStream inputStream = Files.newInputStream(configFile)) {
      Yaml yaml = new Yaml();
      Map<String, Object> root = yaml.load(inputStream);
      Map<String, Object> spring = (Map<String, Object>) root.get("spring");
      if (spring == null) {
        throw new IllegalStateException(
            "Spring configuration is missing in " + configFile.getFileName());
      }
      Map<String, Object> datasource = (Map<String, Object>) spring.get("datasource");
      if (datasource == null) {
        throw new IllegalStateException(
            "Datasource configuration is missing in " + configFile.getFileName());
      }
      String url = String.valueOf(datasource.get("url"));
      String username = String.valueOf(datasource.get("username"));
      String password = String.valueOf(datasource.get("password"));

      // 验证配置是否有效
      if ("null".equals(url) || url.isEmpty()) {
        throw new IllegalStateException(
            "Database URL is missing or invalid in " + configFile.getFileName());
      }
      if ("null".equals(username) || username.isEmpty()) {
        throw new IllegalStateException(
            "Database username is missing or invalid in " + configFile.getFileName());
      }
      if ("null".equals(password)) {
        throw new IllegalStateException(
            "Database password is missing or invalid in " + configFile.getFileName());
      }

      return new DatabaseConfig(url, username, password);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read " + configFile.getFileName(), e);
    }
  }

  private static void generateServiceProfile() {
    try {
      if (ADMIN_SERVICE_YML == null) {
        throw new IllegalStateException("ADMIN_SERVICE_YML path is null");
      }
      if (Files.notExists(ADMIN_SERVICE_YML)) {
        Path parentDir = ADMIN_SERVICE_YML.getParent();
        if (parentDir != null) {
          Files.createDirectories(parentDir);
        }
      }
      String content =
          """
                    spring:
                      application:
                        name: SmartHire_AdminService

                    server:
                      port: 8085
                      servlet:
                        context-path: /smarthire/api/admin

                    # inherits datasource/redis/mail from application.yml
                    """;
      Files.writeString(ADMIN_SERVICE_YML, content, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create admin service profile yml", e);
    }
  }

  // @SuppressWarnings("unused")
  //    private static void generateAdminController(String projectPath) {
  //        // TODO:修改controller文件的路径
  //        if (projectPath == null) {
  //            throw new IllegalArgumentException("Project path cannot be null");
  //        }
  //        Path controllerPath = Paths.get(projectPath +
  // "\\src\\main\\java\\com\\SmartHire\\adminService\\controller", "AdminController.java");
  //
  //        try {
  //            if (controllerPath == null) {
  //                throw new IllegalStateException("Failed to create controller path");
  //            }
  //            Path parentDir = controllerPath.getParent();
  //            if (parentDir != null) {
  //                Files.createDirectories(parentDir);
  //            }
  //
  //            // TODO：需要修改controller文件内容
  //            String content = """
  //                    package com.SmartHire.adminService.controller;
  //
  //                    import org.springframework.web.bind.annotation.RequestMapping;
  //                    import org.springframework.web.bind.annotation.RestController;
  //
  //                    /**
  //                     * <p>
  //                     * 管理员服务统一控制器
  //                     * </p>
  //                     *
  //                     * @author SmartHire Team
  //                     * @since 2025-12-02
  //                     */
  //                    @RestController
  //                    @RequestMapping("/admin")
  //                    public class AdminController {
  //
  //                    }
  //                    """;
  //
  //            Files.writeString(controllerPath, content, StandardCharsets.UTF_8);
  //            Path absolutePath = controllerPath.toAbsolutePath();
  //            if (absolutePath != null) {
  //                System.out.println("SeekerController generated at: " + absolutePath);
  //            }
  //        } catch (IOException e) {
  //            throw new IllegalStateException("Failed to create SeekerController", e);
  //        }
  //    }

  private record DatabaseConfig(String url, String username, String password) {}
}
