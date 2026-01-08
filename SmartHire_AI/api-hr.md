# SmartHire AI Service API 文档 - HR端

## 基础信息

- **Base URL**: `/smarthire/api` (运行在端口 7001)
- **认证方式**: JWT Bearer Token
- **Content-Type**: `application/json`
- **用户类型**: 仅限HR用户（`userType: 2`）

## 认证说明

所有需要认证的接口都需要在请求头中携带 JWT Token：

```
Authorization: Bearer <your_jwt_token>
```

Token 从 Java 后端获取，Python 后端负责验证。Token 必须包含 `type: "access"` 和 `claims.id`（用户ID），且 `claims.userType` 必须为 `2`（HR用户）。

---

## POST 候选人匹配分析

POST /smarthire/api/ai/hr/candidate/{job_seeker_id}/analysis

### 功能说明

快速分析某个候选人与指定岗位的匹配度，返回详细的匹配分析和差距分析。包括：
- 匹配度分析（基于向量相似度计算）
- 技能差距分析（必需技能vs候选人已有技能）
- 经验差距分析（工作年限vs职位要求）
- 学历匹配分析
- 教育背景匹配分析

### 请求参数

| 名称           | 位置  | 类型    | 必选 | 说明                                 |
| -------------- | ----- | ------- | ---- | ------------------------------------ |
| job_seeker_id  | path  | integer | 是   | 候选人ID（job_seeker表的id）         |
| job_id         | query | integer | 是   | 岗位ID                               |
| force_refresh  | query | boolean | 否   | 是否强制刷新（默认false，使用缓存） |
| userId         | -     | integer | -    | 从JWT Token中自动提取，无需前端传递 |

### 请求头

| 名称          | 类型   | 必选 | 说明           |
| ------------- | ------ | ---- | -------------- |
| Authorization | string | 是   | Bearer <token> |

> 返回示例

> 200 Response

```json
{
  "match_analysis": {
    "overall_score": 78,
    "skill_match": 0.75,
    "education_match": 1.0,
    "experience_qualified": true
  },
  "gap_analysis": {
    "skills": {
      "required_missing": ["Docker", "Kubernetes"],
      "optional_missing": ["RabbitMQ"],
      "matched": [
        {
          "name": "Java",
          "candidate_level": 4,
          "is_required": true
        },
        {
          "name": "Spring Boot",
          "candidate_level": 3,
          "is_required": true
        }
      ],
      "match_rate": 0.75
    },
    "experience": {
      "candidate_years": 4.5,
      "required_text": "3-5年",
      "is_qualified": true,
      "gap_years": 0.0
    },
    "education": {
      "candidate_text": "本科",
      "required_text": "本科",
      "is_qualified": true,
      "match_score": 1.0
    }
  },
  "candidate_info": {
    "job_seeker_id": 123,
    "real_name": "张三",
    "education": "本科",
    "work_experience_year": 4.5,
    "current_city": "北京"
  },
  "job_info": {
    "job_id": 456,
    "job_title": "资深后端工程师",
    "city": "北京",
    "education_required": "本科",
    "experience_required": "3-5年"
  }
}
```

> 401 Response

```json
{
  "detail": "Invalid token or not HR user"
}
```

> 404 Response

```json
{
  "detail": "Candidate or job not found"
}
```

> 500 Response

```json
{
  "detail": "Internal server error: ..."
}
```

### 返回结果

| 状态码 | 状态码含义           | 说明                   |
| ------ | -------------------- | ---------------------- |
| 200    | OK                   | 分析成功               |
| 401    | Unauthorized         | Token无效或非HR用户     |
| 404    | Not Found            | 候选人或岗位信息不存在 |
| 500    | Internal Server Error | 服务器内部错误         |

### 返回数据结构

#### match_analysis 结构

| 名称                 | 类型    | 说明                         |
| -------------------- | ------- | ---------------------------- |
| overall_score        | integer | 整体匹配分数，范围 0-100     |
| skill_match          | float   | 技能匹配度，范围 0.0-1.0     |
| education_match      | float   | 学历匹配度，范围 0.0-1.0     |
| experience_qualified | boolean | 经验是否符合要求             |

#### gap_analysis 结构

| 名称       | 类型   | 说明             |
| ---------- | ------ | ---------------- |
| skills     | object | 技能差距分析     |
| experience | object | 经验差距分析     |
| education  | object | 学历差距分析     |

#### gap_analysis.skills 结构

| 名称             | 类型  | 说明                                   |
| ---------------- | ----- | -------------------------------------- |
| required_missing | array | 候选人缺失的必需技能列表               |
| optional_missing | array | 候选人缺失的可选技能列表               |
| matched          | array | 已匹配的技能列表（含level和is_required） |
| match_rate       | float | 必需技能匹配率，范围 0.0-1.0           |

#### gap_analysis.skills.matched 数组元素结构

| 名称            | 类型    | 说明               |
| --------------- | ------- | ------------------ |
| name            | string  | 技能名称           |
| candidate_level | integer | 候选人的技能等级(0-4) |
| is_required     | boolean | 是否为职位必需技能 |

#### gap_analysis.experience 结构

| 名称            | 类型    | 说明                               |
| --------------- | ------- | ---------------------------------- |
| candidate_years | float   | 候选人的工作年限                   |
| required_text   | string  | 职位要求的经验描述                 |
| is_qualified    | boolean | 是否符合经验要求                   |
| gap_years       | float   | 差距年数（负数表示不足）           |

#### gap_analysis.education 结构

| 名称            | 类型    | 说明               |
| --------------- | ------- | ------------------ |
| candidate_text  | string  | 候选人的学历文本   |
| required_text   | string  | 职位要求的学历文本 |
| is_qualified    | boolean | 是否符合学历要求   |
| match_score     | float   | 学历匹配分数       |

### 数据来源说明

所有数据均为真实数据，无假数据：

| 数据项              | 来源                                                |
| ------------------- | --------------------------------------------------- |
| match_analysis      | 向量相似度计算的真实匹配分数                        |
| gap_analysis        | 基于数据库skill表和job_skill_requirement表计算      |
| candidate_info      | 数据库job_seeker表                                  |
| job_info            | 数据库job_info表                                    |

### 性能说明

- 响应时间：2-5秒（差距分析 + 向量相似度计算）
- 缓存：24小时有效
- 建议：前端展示骨架屏，数据返回后一次性渲染

---

## POST 候选人综合评估报告

POST /smarthire/api/ai/hr/candidate/{job_seeker_id}/evaluation

### 功能说明

生成候选人的综合评估报告，包括优势分析、劣势分析、推荐理由等。使用LLM基于候选人的真实简历数据和岗位要求生成。

### 请求参数

| 名称          | 位置  | 类型    | 必选 | 说明                                 |
| ------------- | ----- | ------- | ---- | ------------------------------------ |
| job_seeker_id | path  | integer | 是   | 候选人ID（job_seeker表的id）         |
| job_id        | query | integer | 是   | 岗位ID                               |
| force_refresh | query | boolean | 否   | 是否强制刷新（默认false，使用缓存） |
| userId        | -     | integer | -    | 从JWT Token中自动提取，无需前端传递 |

### 请求头

| 名称          | 类型   | 必选 | 说明           |
| ------------- | ------ | ---- | -------------- |
| Authorization | string | 是   | Bearer <token> |

> 返回示例

> 200 Response

```json
{
  "evaluation": {
    "overall_assessment": "该候选人具有扎实的Java后端开发经验，项目经验丰富，技术栈匹配度高。主要优势在于Spring Boot框架的深入理解和实际项目应用。不足之处是缺乏容器化技术经验。整体推荐度：高。",
    "strengths": [
      {
        "category": "技术能力",
        "description": "4年Java开发经验，熟练掌握Spring Boot框架，有完整的微服务项目经验",
        "evidence": "项目经验显示曾负责核心业务模块开发，使用Spring Cloud构建微服务架构"
      },
      {
        "category": "项目经验",
        "description": "有大型系统开发经验，具备系统设计和性能优化能力",
        "evidence": "XXX管理系统项目，负责后端架构设计，系统支撑日活10万+用户"
      },
      {
        "category": "学习能力",
        "description": "持续学习新技术，技术栈不断更新",
        "evidence": "从传统SSM框架快速转向Spring Boot，并在项目中成功应用"
      }
    ],
    "weaknesses": [
      {
        "category": "技能缺失",
        "description": "缺乏容器化技术经验（Docker、Kubernetes）",
        "impact": "中等影响，可通过入职后培训弥补"
      },
      {
        "category": "经验不足",
        "description": "大规模分布式系统经验相对有限",
        "impact": "低影响，有相关项目经验可快速上手"
      }
    ],
    "recommendation": {
      "level": "高",
      "reason": "候选人技术栈与岗位要求高度匹配，项目经验丰富，学习能力强。虽然缺少容器化技术，但基础扎实，可快速学习。建议进入面试环节。",
      "suggested_actions": [
        "重点考察微服务架构设计和性能优化经验",
        "询问对容器化技术的了解程度和学习计划",
        "评估团队协作和问题解决能力"
      ]
    },
    "interview_focus": [
      "Spring Boot自动配置原理和实际应用",
      "微服务架构设计思路和遇到的问题",
      "系统性能优化的具体实践",
      "对容器化技术的认知和学习计划"
    ]
  },
  "candidate_summary": {
    "job_seeker_id": 123,
    "real_name": "张三",
    "education": "本科",
    "work_experience_year": 4.5,
    "skill_count": 8,
    "project_count": 3,
    "work_experience_count": 2
  },
  "job_summary": {
    "job_id": 456,
    "job_title": "资深后端工程师",
    "required_skills_count": 6,
    "experience_required": "3-5年"
  }
}
```

### 返回数据结构

#### evaluation 结构

| 名称                | 类型   | 说明                           |
| ------------------- | ------ | ------------------------------ |
| overall_assessment  | string | 整体评估（LLM生成，100-200字） |
| strengths           | array  | 优势列表                       |
| weaknesses          | array  | 劣势列表                       |
| recommendation      | object | 推荐建议                       |
| interview_focus     | array  | 面试重点问题列表               |

#### strengths 数组元素结构

| 名称        | 类型   | 说明                           |
| ----------- | ------ | ------------------------------ |
| category    | string | 优势类别（如"技术能力"）       |
| description | string | 优势描述（LLM生成）            |
| evidence    | string | 证据支撑（来自简历数据）       |

#### weaknesses 数组元素结构

| 名称    | 类型   | 说明                           |
| ------- | ------ | ------------------------------ |
| category | string | 劣势类别（如"技能缺失"）       |
| description | string | 劣势描述（LLM生成）            |
| impact  | string | 影响程度（高/中/低）           |

#### recommendation 结构

| 名称            | 类型   | 说明                           |
| --------------- | ------ | ------------------------------ |
| level           | string | 推荐等级（高/中/低）           |
| reason          | string | 推荐理由（LLM生成，50-100字）  |
| suggested_actions | array | 建议行动（面试重点）           |

### 数据来源说明

| 数据项            | 来源                                    |
| ----------------- | --------------------------------------- |
| overall_assessment | LLM基于候选人简历和岗位要求生成         |
| strengths         | LLM分析候选人简历数据生成               |
| weaknesses        | LLM基于技能差距分析生成                 |
| recommendation    | LLM综合评估生成                         |
| candidate_summary | 数据库job_seeker、skill、project等表统计 |
| job_summary       | 数据库job_info表                        |

### 性能说明

- 响应时间：5-10秒（LLM生成时间）
- 缓存：24小时有效
- 建议：前端展示loading状态，数据返回后渲染

---

## GET 面试问题生成（SSE流式输出）

GET /smarthire/api/ai/hr/candidate/{job_seeker_id}/interview-questions

### 功能说明

基于岗位要求和候选人简历，通过SSE流式输出生成个性化的面试问题。包括技术问题、行为问题、项目相关问题等。

### 请求参数

| 名称          | 位置  | 类型    | 必选 | 说明                                 |
| ------------- | ----- | ------- | ---- | ------------------------------------ |
| job_seeker_id | path  | integer | 是   | 候选人ID（job_seeker表的id）         |
| job_id        | query | integer | 是   | 岗位ID                               |
| userId        | -     | integer | -    | 从JWT Token中自动提取，无需前端传递 |

### 请求头

| 名称          | 类型   | 必选 | 说明           |
| ------------- | ------ | ---- | -------------- |
| Authorization | string | 是   | Bearer <token> |
| Accept        | string | 否   | text/event-stream（推荐） |

### 响应格式

**Content-Type**: `text/event-stream`

**SSE事件格式**：

```
data: {"type":"start","message":"开始生成面试问题..."}

data: {"type":"chunk","content":"## 技术问题\n\n**1. 请解释Spring Boot的自动配置原理**\n\n*考察点：* 对Spring Boot核心机制的理解\n\n*参考回答要点：*"}

data: {"type":"chunk","content":"- @EnableAutoConfiguration注解的作用\n- 条件注解的使用\n- spring.factories文件的作用\n\n**2. 你在项目中如何实现缓存优化？**\n\n*考察点：* 实际项目经验，性能优化能力\n\n*参考回答要点：*"}

data: {"type":"chunk","content":"- Redis缓存策略\n- 缓存穿透、击穿、雪崩的解决方案\n- 结合候选人项目经验提问\n\n## 行为问题\n\n**3. 描述一次你解决复杂技术问题的经历**\n\n*考察点：* 问题解决能力，STAR法则应用\n\n*参考回答要点：*"}

data: {"type":"done"}
```

### 事件类型说明

| 类型    | 说明                     |
| ------- | ------------------------ |
| start   | 开始生成，包含提示消息   |
| chunk   | 内容块，包含部分生成文本 |
| done    | 生成完成                 |
| error  | 生成错误，包含错误消息   |

### 前端使用示例

```javascript
const eventSource = new EventSource(
  `/api/ai/hr/candidate/${jobSeekerId}/interview-questions?job_id=${jobId}&Authorization=Bearer ${token}`
);

let fullContent = '';

eventSource.onmessage = (event) => {
  const data = JSON.parse(event.data);
  
  if (data.type === 'start') {
    console.log('开始生成:', data.message);
    // 显示loading状态
  } else if (data.type === 'chunk') {
    fullContent += data.content;
    // 实时更新UI，显示生成的内容
    updateUI(fullContent);
  } else if (data.type === 'done') {
    eventSource.close();
    // 生成完成，可以解析markdown并展示
    parseAndDisplay(fullContent);
  } else if (data.type === 'error') {
    eventSource.close();
    // 显示错误信息
    showError(data.message);
  }
};

eventSource.onerror = (error) => {
  console.error('SSE连接错误:', error);
  eventSource.close();
};
```

### 性能说明

- 首次Token时间：< 1秒
- 流式输出时间：10-20秒（取决于内容长度）
- 前端可实时显示，无需等待完整响应

---

## POST 候选人推荐理由

POST /smarthire/api/ai/hr/candidate/{job_seeker_id}/recommendation

### 功能说明

生成推荐某个候选人的理由，适用于HR向团队或上级推荐候选人时使用。使用LLM基于候选人的真实数据和岗位要求生成。

### 请求参数

| 名称          | 位置  | 类型    | 必选 | 说明                                 |
| ------------- | ----- | ------- | ---- | ------------------------------------ |
| job_seeker_id | path  | integer | 是   | 候选人ID（job_seeker表的id）         |
| job_id        | query | integer | 是   | 岗位ID                               |
| force_refresh | query | boolean | 否   | 是否强制刷新（默认false，使用缓存） |
| userId        | -     | integer | -    | 从JWT Token中自动提取，无需前端传递 |

### 请求头

| 名称          | 类型   | 必选 | 说明           |
| ------------- | ------ | ---- | -------------- |
| Authorization | string | 是   | Bearer <token> |

> 返回示例

> 200 Response

```json
{
  "recommendation": {
    "summary": "推荐该候选人担任资深后端工程师职位。候选人具有4.5年Java开发经验，技术栈与岗位要求高度匹配，项目经验丰富，具备系统设计和性能优化能力。",
    "key_points": [
      {
        "point": "技术能力匹配",
        "description": "熟练掌握Spring Boot、MySQL、Redis等核心技术栈，与岗位要求完全匹配",
        "evidence": "候选人简历显示有4年Spring Boot项目经验，掌握微服务架构"
      },
      {
        "point": "项目经验丰富",
        "description": "有大型系统开发经验，具备完整的项目生命周期管理能力",
        "evidence": "XXX管理系统项目，负责后端架构设计，系统支撑10万+日活用户"
      },
      {
        "point": "学习能力强",
        "description": "能够快速学习新技术，技术栈持续更新",
        "evidence": "从传统框架快速转向Spring Boot，并在项目中成功应用"
      }
    ],
    "concerns": [
      {
        "concern": "容器化技术经验不足",
        "mitigation": "候选人基础扎实，可快速学习。建议入职后提供Docker/K8s培训"
      }
    ],
    "recommendation_text": "基于以上分析，我强烈推荐该候选人进入面试环节。候选人技术能力与岗位要求高度匹配，项目经验丰富，具备独立解决问题的能力。虽然缺少容器化技术经验，但基础扎实，学习能力强，可通过入职后培训快速弥补。建议安排技术面试，重点考察微服务架构设计和性能优化经验。",
    "suggested_next_steps": [
      "安排技术面试，重点考察Spring Boot和微服务架构经验",
      "询问对容器化技术的了解程度和学习计划",
      "评估团队协作和问题解决能力",
      "如通过技术面试，可安排HR面试了解职业规划"
    ]
  },
  "match_score": 78,
  "candidate_highlights": {
    "work_experience": "4.5年Java开发经验",
    "key_skills": ["Java", "Spring Boot", "MySQL", "Redis"],
    "project_count": 3,
    "education": "本科"
  }
}
```

### 返回数据结构

#### recommendation 结构

| 名称                | 类型   | 说明                           |
| ------------------- | ------ | ------------------------------ |
| summary             | string | 推荐摘要（LLM生成，50-100字）  |
| key_points          | array  | 推荐要点列表                   |
| concerns            | array  | 关注点及应对措施               |
| recommendation_text | string | 完整推荐理由（LLM生成，200-300字） |
| suggested_next_steps | array | 建议下一步行动                 |

#### key_points 数组元素结构

| 名称        | 类型   | 说明                           |
| ----------- | ------ | ------------------------------ |
| point       | string | 要点标题                       |
| description | string | 要点描述（LLM生成）            |
| evidence    | string | 证据支撑（来自简历数据）        |

#### concerns 数组元素结构

| 名称      | 类型   | 说明                           |
| --------- | ------ | ------------------------------ |
| concern   | string | 关注点描述                     |
| mitigation | string | 应对措施（LLM生成）            |

### 数据来源说明

| 数据项              | 来源                                    |
| ------------------- | --------------------------------------- |
| summary             | LLM基于候选人数据和岗位要求生成         |
| key_points          | LLM分析候选人优势生成                   |
| concerns            | LLM基于技能差距分析生成                 |
| recommendation_text | LLM综合评估生成                         |
| match_score         | 向量相似度计算的真实匹配分数            |
| candidate_highlights | 数据库job_seeker、skill等表统计         |

### 性能说明

- 响应时间：5-10秒（LLM生成时间）
- 缓存：24小时有效
- 建议：前端展示loading状态，数据返回后渲染

---

## 数据模型

### 候选人匹配分析响应（完整示例）

```json
{
  "match_analysis": {
    "overall_score": 78,
    "skill_match": 0.75,
    "education_match": 1.0,
    "experience_qualified": true
  },
  "gap_analysis": {
    "skills": {
      "required_missing": ["Docker", "Kubernetes"],
      "optional_missing": ["RabbitMQ"],
      "matched": [
        {
          "name": "Java",
          "candidate_level": 4,
          "is_required": true
        },
        {
          "name": "Spring Boot",
          "candidate_level": 3,
          "is_required": true
        }
      ],
      "match_rate": 0.75
    },
    "experience": {
      "candidate_years": 4.5,
      "required_text": "3-5年",
      "is_qualified": true,
      "gap_years": 0.0
    },
    "education": {
      "candidate_text": "本科",
      "required_text": "本科",
      "is_qualified": true,
      "match_score": 1.0
    }
  },
  "candidate_info": {
    "job_seeker_id": 123,
    "real_name": "张三",
    "education": "本科",
    "work_experience_year": 4.5,
    "current_city": "北京"
  },
  "job_info": {
    "job_id": 456,
    "job_title": "资深后端工程师",
    "city": "北京",
    "education_required": "本科",
    "experience_required": "3-5年"
  }
}
```

---

## 匹配分数计算说明

### 候选人匹配分数计算

基于向量相似度计算：
1. 从数据库获取岗位信息和候选人信息
2. 构建候选人文本和岗位文本
3. 使用 `text2vec-base-chinese` 模型转换为向量
4. 计算余弦相似度并转换为 0-100 分数

### 技能匹配度计算

1. **必需技能匹配率**：候选人已掌握的必需技能数 / 岗位要求的必需技能总数
2. **可选技能匹配率**：候选人已掌握的可选技能数 / 岗位要求的可选技能总数
3. **整体技能匹配度**：加权平均（必需技能权重0.7，可选技能权重0.3）

---

## 权限说明

所有HR端AI服务接口都需要：
1. 有效的JWT Token
2. Token中的 `userType` 必须为 `2`（HR用户）
3. HR必须拥有查看该岗位的权限（岗位的 `hr_id` 必须匹配）

如果权限验证失败，将返回 `401 Unauthorized` 错误。

---

## 日志说明

服务运行时会输出以下日志：

- 启动时：MySQL 和 Milvus Lite 连接状态
- 请求时：FastAPI 自动记录 HTTP 请求
- 错误时：认证失败、数据库查询错误、LLM调用错误等详细信息

日志格式：`%(asctime)s - %(name)s - %(levelname)s - %(message)s`

日志级别：INFO

