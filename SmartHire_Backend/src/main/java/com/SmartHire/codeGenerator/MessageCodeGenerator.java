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
 * SeekerService 代码生成器
 *
 * @author SmartHire Team 重要提醒：生成的代码会覆盖已存在的文件，请谨慎操作！
 */
public class MessageCodeGenerator {

  private static final Path BASE_RESOURCE_PATH = Paths.get("src", "main", "resources");
  private static final Path APPLICATION_YML = BASE_RESOURCE_PATH.resolve("application.yml");
  private static final Path APPLICATION_LOCAL_YML =
      BASE_RESOURCE_PATH.resolve("application-local.yml");
  // TODO：需要修改yml文件名
  private static final Path SEEKER_SERVICE_YML =
      BASE_RESOURCE_PATH.resolve("application-message-service.yml");

  public static void main(String[] args) {
    DatabaseConfig dbConfig = loadDatabaseConfig();
    generateServiceProfile();

    String projectPath = System.getProperty("user.dir");

    FastAutoGenerator.create(dbConfig.url, dbConfig.username, dbConfig.password)
        .globalConfig(
            builder ->
                builder
                    .author("SmartHire Team")
                    .outputDir(projectPath + "/src/main/java")
                    .disableOpenDir()
                    .dateType(DateType.ONLY_DATE))
        .packageConfig(
            builder ->
                builder
                    // TODO：需要修改服务文件夹名称
                    .parent("com.SmartHire.messageService")
                    .entity("model")
                    .mapper("mapper")
                    .service("service")
                    .serviceImpl("service.impl"))
        .strategyConfig(
            builder -> {
              // TODO：需要修改服务相关的数据库表
              builder.addInclude("chat_message", "conversation");
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

    generateRecruitmentController(projectPath);
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
      Path parent = SEEKER_SERVICE_YML.getParent();
      if (parent != null && Files.notExists(parent)) {
        Files.createDirectories(parent);
      }
      // TODO：需要修改yml文件内容，需要修改port
      String content =
          """
          spring:
            application:
              name: SmartHire_MessageService

          server:
            port: 8085
            servlet:
              context-path: /smarthire/api

          # inherits datasource/redis/mail from application.yml
          """;
      Files.writeString(SEEKER_SERVICE_YML, content, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create seeker service profile yml", e);
    }
  }

  private static void generateRecruitmentController(String projectPath) {
    // TODO:修改controller文件的路径
    Path controllerPath =
        Paths.get(
            projectPath,
            "src",
            "main",
            "java",
            "com",
            "SmartHire",
            "messageService",
            "controller",
            "MessageController.java");

    try {
      Path parent = controllerPath.getParent();
      if (parent != null && Files.notExists(parent)) {
        Files.createDirectories(parent);
      }

      // TODO：需要修改controller文件内容
      String content =
          """
          package com.SmartHire.messageService.controller;

          import org.springframework.web.bind.annotation.RequestMapping;
          import org.springframework.web.bind.annotation.RestController;

          /**
           * <p>
           * 沟通消息服务统一控制器
           * </p>
           *
           * @author SmartHire Team
           * @since 2025-11-30
           */
          @RestController
          @RequestMapping("/message")
          public class MessageController {

          }
          """;

      Files.writeString(controllerPath, content, StandardCharsets.UTF_8);
      System.out.println("SeekerController generated at: " + controllerPath.toAbsolutePath());
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create SeekerController", e);
    }
  }

  private record DatabaseConfig(String url, String username, String password) {}
}
