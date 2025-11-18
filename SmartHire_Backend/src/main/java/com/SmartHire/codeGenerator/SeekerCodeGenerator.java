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

public class SeekerCodeGenerator {

        private static final Path BASE_RESOURCE_PATH = Paths.get("src", "main", "resources");
        private static final Path APPLICATION_YML = BASE_RESOURCE_PATH.resolve("application.yml");
        private static final Path SEEKER_SERVICE_YML = BASE_RESOURCE_PATH.resolve("application-seeker-service.yml");

        public static void main(String[] args) {
                DatabaseConfig dbConfig = loadDatabaseConfig();
                generateServiceProfile();

                String projectPath = System.getProperty("user.dir");

                FastAutoGenerator.create(dbConfig.url, dbConfig.username, dbConfig.password)
                                .globalConfig(builder -> builder
                                                .author("SmartHire Team")
                                                .outputDir(projectPath + "/src/main/java")
                                                .disableOpenDir()
                                                .dateType(DateType.ONLY_DATE))
                                .packageConfig(builder -> builder
                                                .parent("com.SmartHire.seekerService")
                                                .entity("model")
                                                .mapper("mapper")
                                                .service("service")
                                                .serviceImpl("service.impl")
                                                .controller("controller"))
                                .strategyConfig(builder -> {
                                        builder.addInclude("job_seeker", "job_seeker_expectation", "resume",
                                                        "education_experience",
                                                        "work_experience", "project_experience");
                                        builder.controllerBuilder()
                                                        .enableRestStyle();
                                        builder.entityBuilder()
                                                        .enableLombok()
                                                        .idType(IdType.AUTO);
                                        builder.mapperBuilder()
                                                        .enableBaseResultMap()
                                                        .enableBaseColumnList();
                                })
                                .execute();
        }

        @SuppressWarnings("unchecked")
        private static DatabaseConfig loadDatabaseConfig() {
                if (!Files.exists(APPLICATION_YML)) {
                        throw new IllegalStateException(
                                        "application.yml not found at " + APPLICATION_YML.toAbsolutePath());
                }
                try (InputStream inputStream = Files.newInputStream(APPLICATION_YML)) {
                        Yaml yaml = new Yaml();
                        Map<String, Object> root = yaml.load(inputStream);
                        Map<String, Object> spring = (Map<String, Object>) root.get("spring");
                        Map<String, Object> datasource = (Map<String, Object>) spring.get("datasource");
                        String url = String.valueOf(datasource.get("url"));
                        String username = String.valueOf(datasource.get("username"));
                        String password = String.valueOf(datasource.get("password"));
                        return new DatabaseConfig(url, username, password);
                } catch (IOException e) {
                        throw new IllegalStateException("Failed to read application.yml", e);
                } catch (NullPointerException e) {
                        throw new IllegalStateException("Datasource configuration is missing in application.yml", e);
                }
        }

        private static void generateServiceProfile() {
                try {
                        if (Files.notExists(SEEKER_SERVICE_YML)) {
                                Files.createDirectories(SEEKER_SERVICE_YML.getParent());
                        }
                        String content = """
                                        spring:
                                          application:
                                            name: SmartHire_SeekerService

                                        server:
                                          port: 8082
                                          servlet:
                                            context-path: /smarthire/api

                                        # inherits datasource/redis/mail from application.yml
                                        """;
                        Files.writeString(SEEKER_SERVICE_YML, content, StandardCharsets.UTF_8);
                } catch (IOException e) {
                        throw new IllegalStateException("Failed to create seeker service profile yml", e);
                }
        }

        private record DatabaseConfig(String url, String username, String password) {
        }
}