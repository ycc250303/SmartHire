package com.SmartHire.common.utils;

import com.aliyun.oss.*;
import com.aliyun.oss.model.PutObjectRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliOssUtil {

  private String endpoint;
  private String accessKeyId;
  private String accessKeySecret;
  private String bucket;

  /** 可配置的目录前缀映射，如 avatar=avatars/, resume=resumes/ */
  private Map<String, String> directoryPrefixes = new HashMap<>();

  /**
   * 上传文件
   *
   * @param objectName 文件名
   * @param in 文件输入流
   * @return 文件访问url
   */
  public String uploadFile(String objectName, InputStream in) throws IOException {

    OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    try {
      PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, objectName, in);
      ossClient.putObject(putObjectRequest);
      return buildPublicUrl(objectName);
    } catch (OSSException oe) {
      log.error(
          "OSSException during upload. objectName={}, code={}, message={}, requestId={}",
          objectName,
          oe.getErrorCode(),
          oe.getErrorMessage(),
          oe.getRequestId(),
          oe);
      throw new RuntimeException("OSS上传失败，请检查凭证配置", oe);
    } catch (ClientException ce) {
      log.error(
          "ClientException during upload. objectName={}, message={}",
          objectName,
          ce.getMessage(),
          ce);
      throw new RuntimeException("OSS客户端异常，请检查网络或配置", ce);
    } finally {
      if (ossClient != null) {
        ossClient.shutdown();
      }
    }
  }

  /**
   * 根据目录键组装 objectName 并上传
   *
   * @param directoryKey 目录键（如 avatar、resume 等）
   * @param fileName 原始文件名（或生成的唯一名）
   * @param in 文件输入流
   */
  public String uploadFile(String directoryKey, String fileName, InputStream in)
      throws IOException {
    String objectName = buildObjectName(directoryKey, fileName);
    return uploadFile(objectName, in);
  }

  /**
   * 直接上传 multipart 文件并自动生成文件名
   *
   * @param directoryKey 目录键
   * @param file multipart 文件
   */
  public String uploadFile(String directoryKey, MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("上传文件不能为空");
    }
    String objectName = buildObjectName(directoryKey, generateFileUrl(file.getOriginalFilename()));
    return uploadFile(objectName, file.getInputStream());
  }

  /**
   * 查询文件
   *
   * @param objectName 文件名
   * @return 文件是否存在
   */
  public boolean findFile(String objectName) {
    // 创建OSSClient实例。
    // 当OSSClient实例不再使用时，调用shutdown方法以释放资源。
    OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    boolean found = false;

    try {
      // 判断文件是否存在。如果返回值为true，则文件存在，否则存储空间或者文件不存在。
      // 设置是否进行重定向或者镜像回源。默认值为true，表示忽略302重定向和镜像回源；如果设置isINoss为false，则进行302重定向或者镜像回源。
      // boolean isINoss = true;
      found = ossClient.doesObjectExist(bucket, objectName);
      // boolean found = ossClient.doesObjectExist(bucket, objectName, isINoss);
      System.out.println(found);
    } catch (OSSException oe) {
      System.out.println(
          "Caught an OSSException, which means your request made it to OSS, "
              + "but was rejected with an error response for some reason.");
      System.out.println("Error Message:" + oe.getErrorMessage());
      System.out.println("Error Code:" + oe.getErrorCode());
      System.out.println("Request ID:" + oe.getRequestId());
      System.out.println("Host ID:" + oe.getHostId());
    } catch (ClientException ce) {
      System.out.println(
          "Caught an ClientException, which means the client encountered "
              + "a serious internal problem while trying to communicate with OSS, "
              + "such as not being able to access the network.");
      System.out.println("Error Message:" + ce.getMessage());
    } finally {
      if (ossClient != null) {
        ossClient.shutdown();
      }
    }
    return found;
  }

  public boolean deleteFile(String objectName) {
    OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    boolean deleted = false;
    try {
      ossClient.deleteObject(bucket, objectName);
      deleted = true;
    } catch (OSSException oe) {
      log.warn(
          "删除OSS文件失败，objectName={}, code={}, message={}, requestId={}",
          objectName,
          oe.getErrorCode(),
          oe.getErrorMessage(),
          oe.getRequestId(),
          oe);
    } catch (ClientException ce) {
      log.warn("OSS客户端删除文件失败，objectName={}, message={}", objectName, ce.getMessage(), ce);
    } finally {
      if (ossClient != null) {
        ossClient.shutdown();
      }
    }
    return deleted;
  }

  /**
   * @param objectName
   * @return
   */
  public String generateFileUrl(String objectName) {
    // 生成文件名
    String fileExtension = "";

    // 安全地获取文件扩展名
    if (objectName != null && objectName.contains(".")) {
      fileExtension = objectName.substring(objectName.lastIndexOf("."));
    }

    return UUID.randomUUID().toString() + fileExtension;
  }

  /**
   * 构建带目录前缀的 objectName
   *
   * @param directoryKey 目录键
   * @param fileName 文件名
   */
  public String buildObjectName(String directoryKey, String fileName) {
    String prefix = resolveDirectoryPrefix(directoryKey);
    return prefix + Objects.requireNonNullElse(fileName, "");
  }

  public String extractObjectName(String fileUrl) {
    if (fileUrl == null || fileUrl.isBlank()) {
      return null;
    }
    String host = buildPublicHost();
    int hostIndex = fileUrl.indexOf(host);
    if (hostIndex < 0) {
      return null;
    }
    int pathIndex = fileUrl.indexOf("/", hostIndex + host.length());
    if (pathIndex < 0 || pathIndex + 1 >= fileUrl.length()) {
      return null;
    }
    String objectPath = fileUrl.substring(pathIndex + 1);
    int queryIndex = objectPath.indexOf("?");
    if (queryIndex >= 0) {
      objectPath = objectPath.substring(0, queryIndex);
    }
    return objectPath;
  }

  private String resolveDirectoryPrefix(String directoryKey) {
    if (directoryKey == null || directoryKey.isBlank()) {
      return "";
    }
    String configuredPrefix = directoryPrefixes.getOrDefault(directoryKey, directoryKey);
    return normalizeDirectory(configuredPrefix);
  }

  private String normalizeDirectory(String directory) {
    if (directory == null || directory.isBlank()) {
      return "";
    }
    String normalized = directory.replace("\\", "/");
    if (normalized.startsWith("/")) {
      normalized = normalized.substring(1);
    }
    if (!normalized.endsWith("/")) {
      normalized = normalized + "/";
    }
    return normalized;
  }

  private String buildPublicUrl(String objectName) {
    return "https://" + buildPublicHost() + "/" + objectName;
  }

  private String buildPublicHost() {
    return bucket + "." + extractEndpointHost(endpoint);
  }

  private String extractEndpointHost(String rawEndpoint) {
    if (rawEndpoint == null || rawEndpoint.isBlank()) {
      return "";
    }
    int idx = rawEndpoint.lastIndexOf("/");
    if (idx >= 0 && idx + 1 < rawEndpoint.length()) {
      return rawEndpoint.substring(idx + 1);
    }
    return rawEndpoint;
  }

  /** 为避免暴露内部可变 Map，返回一个副本 */
  public Map<String, String> getDirectoryPrefixes() {
    return new HashMap<>(directoryPrefixes);
  }

  /** 为避免持有外部可变 Map 的引用，仅拷贝内容 */
  public void setDirectoryPrefixes(Map<String, String> directoryPrefixes) {
    this.directoryPrefixes.clear();
    if (directoryPrefixes != null) {
      this.directoryPrefixes.putAll(directoryPrefixes);
    }
  }
}
