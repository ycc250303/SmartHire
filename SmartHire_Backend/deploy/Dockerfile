# 运行时镜像：JRE 21
FROM eclipse-temurin:21-jre

WORKDIR /app

# 把打好的 Spring Boot 可执行 jar 拷进容器
# 注意：这里的 jar 名要和你实际打出来的一致
COPY target/SmartHire_Backend-0.0.1-SNAPSHOT.jar app.jar

# 可选：通过环境变量传 JVM 参数
ENV JAVA_OPTS=""

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]