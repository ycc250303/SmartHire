# SmartHire JWT 认证方案技术文档

参考链接

[jwt基础概念讲解](https://javaguide.cn/system-design/security/jwt-intro.html)

[jwt身份认证优缺点分析](https://javaguide.cn/system-design/security/advantages-and-disadvantages-of-jwt.html)

## 一、都需要看的部分

### 1. 用户登录流程

#### 1.1 登录接口

**接口地址：** `POST /smarthire/api/user-auth/login`

**请求体：**
```json
{
  "username": "SmartHire001",
  "password": "your_password"
}
```

**响应示例：**
```json
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 604800
  }
}
```

#### 1.2 登录流程说明

登录逻辑位于 `UserAuthServiceImpl.login()` 方法，流程如下：

1. 验证用户名和密码
2. 更新用户最后登录时间
3. 构建 JWT Claims（包含用户ID、用户名、用户类型）
4. 生成 Access Token 和 Refresh Token
5. 将 Refresh Token 缓存到 Redis（用于单点登录控制）
6. 返回 Token 信息

---

### 2. Access Token 和 Refresh Token

#### 2.1 Token 配置

配置文件：`application-local.yml`

```yaml
jwt:
  secret-key: SmartHire  # JWT 签名密钥
  refresh-token-valid-time: 604800000  # Refresh Token 有效期：7天（毫秒）
  access-token-valid-time: 604800000  # Access Token 有效期：7天（毫秒）
  refresh-token-renew-threshold: 600000  # Refresh Token 续期阈值：10分钟（毫秒）
```

#### 2.2 Access Token

- **作用**：用于访问所有需要认证的 API 接口，携带在请求头 `Authorization` 中
- **注意事项**：
  - 必须使用 Access Token 访问受保护接口（使用 Refresh Token 会返回错误）
  - Token 过期后需要使用 Refresh Token 刷新
  - 登出或刷新时，旧的 Access Token 会被加入黑名单

#### 2.3 Refresh Token

- **作用**：用于刷新 Access Token，有效期较长
- **注意事项**：
  - 不能用于访问受保护接口，只能用于调用 `/user-auth/refresh-token` 接口
  - 支持单点登录：Redis 存储最新 Refresh Token（Key：`token:refresh:single:{userId}`），新登录会覆盖旧的
  - 自动续期：剩余有效期小于阈值（10分钟）时自动生成新的 Refresh Token

---

### 3. Token 黑名单机制

黑名单用于在以下场景使 Token 失效：
1. **用户登出**：Access Token 和 Refresh Token 都加入黑名单
2. **Token 刷新**：旧的 Access Token 加入黑名单
3. **Refresh Token 续期**：旧的 Refresh Token 加入黑名单

**存储方式**：Redis，Key 格式：
- Access Token 黑名单：`token:blacklist:access:{token}`
- Refresh Token 黑名单：`token:blacklist:refresh:{token}`

**检查时机**：`JwtAuthenticationFilter` 在每次请求时检查 Access Token 是否在黑名单中

---

### 4. Apifox 调试说明

1. 调用登录接口获取 token
2. 在对应文件夹处，点击"前置操作"，添加自定义脚本：
   ```
   pm.request.addHeader("Authorization:your-access-token");
   ```
3. 相当于给该文件夹下的所有接口都添加了 Authorization 请求头

---

## 二、前端需要看的部分

### 1. Token 的存储和管理

**推荐方案**：使用 `localStorage` 或 `sessionStorage` 存储 Token

**注意事项**：
- 不要将 Token 存储在 `cookie` 中（避免 CSRF 攻击）
- 如果使用 `localStorage`，注意 XSS 攻击防护
- 建议在 Token 过期前 5 分钟自动刷新

---

### 2. 定期刷新 Token

**刷新时机**：
1. 定时刷新：在 Token 过期前 5 分钟自动刷新
2. 请求前检查：每次发起请求前检查 Token 是否即将过期
3. 错误时刷新：当收到 1012 错误时，尝试刷新 Token 后重试请求

**刷新接口**：`POST /smarthire/api/user-auth/refresh-token`

**请求体：**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**响应示例：**
```json
{
  "code": 0,
  "message": "刷新成功",
  "data": {
    "accessToken": "新的 accessToken",
    "refreshToken": "新的或原有的 refreshToken",
    "expiresIn": 604800
  }
}
```

**注意事项**：
- 刷新成功后需要更新本地存储的 Token
- 如果刷新失败，需要清除本地 Token 并跳转到登录页

---

### 3. 请求拦截器配置

- **请求拦截器**：自动添加 Token 到请求头 `Authorization` 中
- **响应拦截器**：处理 Token 过期情况
  - 收到 1012 错误时，尝试使用 Refresh Token 刷新 Access Token
  - 刷新成功后，使用新的 Access Token 重试原请求
  - 刷新失败后，清除本地 Token 并跳转到登录页

---

### 4. 错误处理

| 错误码 | 错误信息                   | 处理方式                         |
| ------ | -------------------------- | -------------------------------- |
| 1011   | JWT Token 为空             | 跳转到登录页                     |
| 1012   | JWT Token 无效             | 尝试刷新 Token，失败则跳转登录页 |
| 1017   | Refresh Token 已过期       | 跳转到登录页                     |
| 1018   | JWT Token 为 refresh token | 使用 Access Token 重新请求       |

---

## 三、后端需要看的部分

### 1. 获取当前登录用户信息（推荐方式）

#### 1.1 使用 UserContext 工具类

**文件位置**：`com.SmartHire.common.auth.UserContext`

**推荐方式**：在 Service 层使用 `UserContext` 获取当前登录用户信息，无需手动解析 JWT。

**常用方法：**
```java
@Autowired
private UserContext userContext;

// 获取用户ID（最常用）
Long userId = userContext.getCurrentUserId();

// 获取用户名
String username = userContext.getCurrentUsername();

// 获取用户类型（1-求职者，2-HR，3-管理员）
Integer userType = userContext.getCurrentUserType();

// 获取完整用户信息
UserInfo userInfo = userContext.getCurrentUser();

// 检查用户类型（便捷方法）
boolean isSeeker = userContext.isSeeker();
boolean isHr = userContext.isHr();
boolean isAdmin = userContext.isAdmin();
```

#### 1.2 实际使用示例

```java
@Service
public class JobSeekerServiceImpl implements JobSeekerService {
    @Autowired
    private UserContext userContext;
    
    @Override
    public JobSeeker getSeekerInfo() {
        Long userId = userContext.getCurrentUserId();
        // ... 业务逻辑
    }
}
```

---

### 2. JWT 工具类使用

#### 2.1 JwtUtil 主要方法

**文件位置**：`com.SmartHire.common.utils.JwtUtil`

**生成 Token：**
```java
@Autowired
private JwtUtil jwtUtil;

// 生成 Access Token
Map<String, Object> claims = Map.of(
    "id", user.getId(),
    "username", user.getUsername(),
    "userType", user.getUserType()
);
String accessToken = jwtUtil.generateAccessToken(claims);

// 生成 Refresh Token
String refreshToken = jwtUtil.generateRefreshToken(claims);
```

**验证和解析 Token：**
```java
// 验证 Token（返回 DecodedJWT）
DecodedJWT decoded = jwtUtil.verifyToken(token);

// 解析 Token（直接返回 Claims）
Map<String, Object> claims = jwtUtil.parseToken(token);

// 判断 Token 类型
boolean isAccess = jwtUtil.isAccessToken(decoded);
boolean isRefresh = jwtUtil.isRefreshToken(decoded);

// 获取剩余有效时间（秒）
long expiresIn = jwtUtil.getExpiresInSeconds(decoded);
```

---

### 3. 认证过滤器实现

#### 3.1 JwtAuthenticationFilter 工作流程

**文件位置**：`com.SmartHire.common.filters.JwtAuthenticationFilter`

**工作流程**（基于 Spring Security）：
1. 检查是否为公开路径，是则放行
2. 使用 `JwtTokenExtractor` 从请求头 `Authorization` 提取 Token
3. 检查 Token 是否在黑名单中（Redis）
4. 使用 `JwtUtil.verifyToken()` 验证 Token 合法性
5. 确保是 Access Token（不是 Refresh Token）
6. 提取 Claims 并设置到 `SecurityContextHolder`
7. 后续 Controller/Service 可通过 `UserContext` 从 `SecurityContextHolder` 读取用户信息

**公开路径**（无需 Token 认证）：
- `/user-auth/login`
- `/user-auth/register`
- `/user-auth/send-verification-code`
- `/user-auth/verify-code`
- `/user-auth/refresh-token`

---

### 4. Token 提取工具

#### 4.1 JwtTokenExtractor

**文件位置**：`com.SmartHire.common.auth.JwtTokenExtractor`

**作用**：统一从 HTTP 请求中提取 JWT Token

**使用场景**：在需要直接从请求中获取 Token 时使用（如登出、刷新 Token 等场景）

```java
@Autowired
private JwtTokenExtractor tokenExtractor;

// 从当前请求提取 Token（不存在时抛出异常）
String token = tokenExtractor.extractToken();

// 从当前请求提取 Token（不存在时返回 null）
String token = tokenExtractor.extractTokenNullable();

// 从指定请求提取 Token
String token = tokenExtractor.extractToken(request);
```

---

## 总结

### 关键要点

1. **Token 类型区分**：Access Token 用于访问接口，Refresh Token 仅用于刷新
2. **黑名单机制**：登出、刷新、续期时都会将旧 Token 加入黑名单
3. **单点登录**：通过 Redis 存储最新 Refresh Token 实现单点登录控制
4. **自动续期**：Refresh Token 剩余时间少于阈值时自动续期
5. **统一获取用户信息**：后端统一使用 `UserContext` 获取当前登录用户信息

### 配置建议

**生产环境配置建议：**
```yaml
jwt:
  secret-key: # 使用复杂的随机字符串
  refresh-token-valid-time: 604800000  # 7天
  access-token-valid-time: 3600000     # 1小时（建议更短）
  refresh-token-renew-threshold: 600000  # 10分钟
```

### 相关文件

- **用户上下文工具**：`com.SmartHire.common.auth.UserContext`（**推荐使用**）
- **Token 提取工具**：`com.SmartHire.common.auth.JwtTokenExtractor`
- **JWT 工具类**：`com.SmartHire.common.utils.JwtUtil`
- **认证过滤器**：`com.SmartHire.common.filters.JwtAuthenticationFilter`
- **用户认证服务**：`com.SmartHire.userAuthService.service.impl.UserAuthServiceImpl`
- **配置文件**：`application-local.yml`

### 前端是否有改变

**前端无需改变**。本次重构仅优化了后端代码结构，API 接口、Token 格式、请求响应格式均保持不变，前端代码无需修改。

### Spring Security 集成说明

**已集成 Spring Security**。认证流程如下：

1. `JwtAuthenticationFilter` 继承 `OncePerRequestFilter`，在 Spring Security 过滤器链中执行
2. 验证 Token 后，将用户信息封装为 `UsernamePasswordAuthenticationToken` 存入 `SecurityContextHolder`
3. `UserContext` 从 `SecurityContextHolder` 读取用户信息
4. Spring Security 配置位于 `SecurityConfig.java`，配置了过滤器链和异常处理

**注意**：本方案使用 Spring Security 的上下文机制，但**不使用** Spring Security 的认证提供者（如 UserDetailsService），而是通过自定义的 JWT 过滤器完成认证。
