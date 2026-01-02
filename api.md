# SmartHire AI Service API 文档

## 基础信息

- **Base URL**: `/smarthire/api` (运行在端口 7001)
- **认证方式**: JWT Bearer Token
- **Content-Type**: `application/json`

## 认证说明

所有需要认证的接口都需要在请求头中携带 JWT Token：

```
Authorization: Bearer <your_jwt_token>
```

Token 从 Java 后端获取，Python 后端负责验证。Token 必须包含 `type: "access"` 和 `claims.id`（用户ID）。

---

## POST 计算岗位匹配分数

POST /smarthire/api/recruitment/seeker/calculate-match-score

### 请求参数

| 名称   | 位置 | 类型    | 必选 | 说明                                 |
| ------ | ---- | ------- | ---- | ------------------------------------ |
| jobId  | body | integer | 是   | 岗位ID                               |
| userId | -    | integer | -    | 从JWT Token中自动提取，无需前端传递 |

### 请求头

| 名称          | 类型   | 必选 | 说明           |
| ------------- | ------ | ---- | -------------- |
| Authorization | string | 是   | Bearer <token> |

> 返回示例

> 200 Response

```json
{
  "matchScore": 85
}
```

> 401 Response

```json
{
  "detail": "Invalid token"
}
```

> 404 Response

```json
{
  "detail": "Job 123 not found"
}
```

> 500 Response

```json
{
  "detail": "Internal server error: ..."
}
```

### 返回结果

| 状态码 | 状态码含义           | 说明                     |
| ------ | -------------------- | ------------------------ |
| 200    | OK                   | 计算成功                 |
| 401    | Unauthorized         | Token无效或过期          |
| 404    | Not Found            | 岗位或求职者信息不存在   |
| 500    | Internal Server Error | 服务器内部错误           |

### 返回数据结构

| 名称       | 类型    | 说明                |
| ---------- | ------- | ------------------- |
| matchScore | integer | 匹配分数，范围 0-100 |

---

## POST 批量计算岗位匹配分数

POST /smarthire/api/recruitment/seeker/batch-calculate-match-scores

### 请求参数

| 名称          | 位置 | 类型   | 必选 | 说明                                 |
| ------------- | ---- | ------ | ---- | ------------------------------------ |
| seekerProfile | body | object | 是   | 求职者信息                           |
| jobs          | body | array  | 是   | 岗位列表                             |
| userId        | -    | integer | -    | 从JWT Token中自动提取，无需前端传递 |

### 请求头

| 名称          | 类型   | 必选 | 说明           |
| ------------- | ------ | ---- | -------------- |
| Authorization | string | 是   | Bearer <token> |

### seekerProfile 结构

| 名称             | 类型   | 必选 | 说明           |
| ---------------- | ------ | ---- | -------------- |
| jobSeekerId      | integer | 否   | 求职者ID       |
| major            | string | 否   | 专业           |
| university       | string | 否   | 大学           |
| highestEducation | string | 否   | 最高学历       |
| currentCity      | string | 否   | 当前城市       |
| skills           | array  | 否   | 技能列表       |
| workExperiences  | array  | 否   | 工作经历列表   |
| projectExperiences | array  | 否   | 项目经历列表   |

### skills 结构

| 名称  | 类型    | 必选 | 说明     |
| ----- | ------- | ---- | -------- |
| name  | string  | 是   | 技能名称 |
| level | integer | 否   | 技能等级 |

### workExperiences 结构

| 名称        | 类型   | 必选 | 说明     |
| ----------- | ------ | ---- | -------- |
| companyName | string | 否   | 公司名称 |
| position    | string | 否   | 职位     |
| description | string | 否   | 描述     |

### projectExperiences 结构

| 名称          | 类型   | 必选 | 说明     |
| ------------- | ------ | ---- | -------- |
| projectName   | string | 否   | 项目名称 |
| description   | string | 否   | 描述     |
| responsibilities | string | 否   | 职责     |

### jobs 结构

| 名称             | 类型    | 必选 | 说明         |
| ---------------- | ------- | ---- | ------------ |
| jobId            | integer | 是   | 岗位ID       |
| jobTitle         | string  | 否   | 岗位标题     |
| description      | string  | 否   | 岗位描述     |
| responsibilities | string  | 否   | 岗位职责     |
| requirements     | string  | 否   | 任职要求     |
| skills           | array   | 否   | 技能要求列表 |
| educationRequired | any     | 否   | 学历要求     |

> 返回示例

> 200 Response

```json
{
  "scores": [
    {
      "jobId": 1,
      "matchScore": 85,
      "similarity": 0.8500,
      "details": {
        "skillMatch": 0.9,
        "descriptionMatch": 0.82,
        "educationMatch": 1.0
      }
    },
    {
      "jobId": 2,
      "matchScore": 45,
      "similarity": 0.4500,
      "details": {
        "skillMatch": 0.3,
        "descriptionMatch": 0.5,
        "educationMatch": 1.0
      }
    }
  ]
}
```

> 401 Response

```json
{
  "detail": "Invalid token"
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
| 200    | OK                   | 计算成功               |
| 401    | Unauthorized         | Token无效或过期        |
| 500    | Internal Server Error | 服务器内部错误         |

### 返回数据结构

| 名称      | 类型   | 说明                           |
| --------- | ------ | ------------------------------ |
| scores    | array  | 匹配分数列表                   |

### scores 数组元素结构

| 名称       | 类型    | 说明                           |
| ---------- | ------- | ------------------------------ |
| jobId      | integer | 岗位ID                          |
| matchScore | integer | 匹配分数，范围 0-100            |
| similarity | float   | 余弦相似度，范围 0.0-1.0        |
| details    | object  | 详细匹配信息                   |

### details 结构

| 名称            | 类型  | 说明                           |
| --------------- | ----- | ------------------------------ |
| skillMatch      | float | 技能匹配度，范围 0.0-1.0        |
| descriptionMatch | float | 描述匹配度，范围 0.0-1.0        |
| educationMatch  | float | 教育背景匹配度，范围 0.0-1.0    |

---

## POST 职业路径分析（快速返回，增强版）

POST /smarthire/api/ai/career-planning/{jobId}/analysis

### 功能说明

快速返回匹配度分析、差距分析、职业发展路线和结构化学习计划（8-12秒返回）。包括：
- 匹配度分析（基于向量相似度计算）
- 技能差距分析（必需技能vs已有技能）
- 经验差距分析（工作年限vs职位要求）
- 学历匹配分析
- **职业发展路线**：技术栈分类、阶段路线、即时建议
- **结构化学习计划（新增）**：每个缺失技能的详细学习卡片

### 请求参数

| 名称          | 位置  | 类型    | 必选 | 说明                                 |
| ------------- | ----- | ------- | ---- | ------------------------------------ |
| jobId         | path  | integer | 是   | 职位ID                               |
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
  "match_analysis": {
    "overall_score": 75,
    "skill_match": 0.6,
    "education_match": 1.0,
    "experience_qualified": true
  },
  "gap_analysis": {
    "skills": {
      "required_missing": ["Docker", "Kubernetes", "Redis"],
      "optional_missing": ["RabbitMQ"],
      "matched": [
        {
          "name": "Java",
          "your_level": 3,
          "is_required": true
        },
        {
          "name": "Spring Boot",
          "your_level": 3,
          "is_required": true
        }
      ],
      "match_rate": 0.6
    },
    "experience": {
      "your_years": 3.5,
      "required_text": "3-5年",
      "is_qualified": true,
      "gap_years": 0.0
    },
    "education": {
      "your_text": "本科",
      "required_text": "本科",
      "is_qualified": true
    }
  },
  "career_roadmap": {
    "overview": {
      "target_position": "资深后端工程师",
      "current_level": "本科·3.5年经验",
      "skill_gaps_count": 3,
      "plan_duration_days": 90,
      "milestones_count": 3
    },
    "technology_stacks": [
      {
        "category": "容器化技术",
        "skills": ["Docker", "Kubernetes"]
      },
      {
        "category": "缓存与消息队列",
        "skills": ["Redis", "RabbitMQ"]
      }
    ],
    "phase_roadmap": [
      {
        "phase": 1,
        "title": "容器化基础",
        "description": "掌握 Docker 容器化部署，完成现有项目容器化改造。",
        "duration_days": 30,
        "skills": ["Docker", "容器编排"],
        "based_on_gap": "Docker"
      },
      {
        "phase": 2,
        "title": "K8s 实践",
        "description": "学习 Kubernetes 集群管理，部署微服务应用。",
        "duration_days": 30,
        "skills": ["Kubernetes", "微服务"],
        "based_on_gap": "Kubernetes"
      },
      {
        "phase": 3,
        "title": "缓存架构",
        "description": "深入 Redis 缓存方案，实现高并发架构优化。",
        "duration_days": 30,
        "skills": ["Redis", "高并发"],
        "based_on_gap": "Redis"
      }
    ],
    "immediate_suggestions": [
      {
        "title": "启动 Docker 学习",
        "description": "本周完成 Docker 基础课程，容器化一个 Spring Boot 项目。"
      },
      {
        "title": "搭建实验环境",
        "description": "使用云服务器搭建 K8s 测试环境。"
      },
      {
        "title": "补充项目文档",
        "description": "完善现有项目的技术文档，准备面试讲解。"
      }
    ]
  },
  "learning_plan_structured": {
    "skills": [
      {
        "skill_name": "Docker",
        "priority": "高",
        "reason": "容器化部署是后端必备",
        "learning_steps": [
          "第1周：学习Docker基础命令和概念",
          "第2-3周：实践项目容器化部署",
          "第4周：学习Docker Compose编排"
        ],
        "resources": [
          {
            "name": "Docker官方文档",
            "type": "文档",
            "url": "https://docs.docker.com/"
          },
          {
            "name": "Docker从入门到实践",
            "type": "书籍",
            "url": ""
          }
        ],
        "estimated_weeks": 4,
        "difficulty": "中等"
      },
      {
        "skill_name": "Kubernetes",
        "priority": "高",
        "reason": "容器编排的行业标准",
        "learning_steps": [
          "第1-2周：理解K8s核心概念",
          "第3-4周：搭建本地环境实践",
          "第5-6周：部署微服务应用"
        ],
        "resources": [
          {
            "name": "Kubernetes官方文档",
            "type": "文档",
            "url": "https://kubernetes.io/docs/"
          },
          {
            "name": "CNCF免费课程",
            "type": "视频",
            "url": ""
          }
        ],
        "estimated_weeks": 6,
        "difficulty": "困难"
      },
      {
        "skill_name": "Redis",
        "priority": "中",
        "reason": "高并发场景缓存必备",
        "learning_steps": [
          "第1周：掌握Redis基础数据结构",
          "第2周：学习缓存策略和持久化",
          "第3周：实践高并发缓存方案"
        ],
        "resources": [
          {
            "name": "Redis设计与实现",
            "type": "书籍",
            "url": ""
          },
          {
            "name": "Redis官方文档",
            "type": "文档",
            "url": "https://redis.io/docs/"
          }
        ],
        "estimated_weeks": 3,
        "difficulty": "中等"
      }
    ]
  }
}
```

### 返回数据结构

#### match_analysis 结构

| 名称                 | 类型    | 说明                                     |
| -------------------- | ------- | ---------------------------------------- |
| overall_score        | integer | 整体匹配分数，范围 0-100                 |
| skill_match          | float   | 技能匹配度，范围 0.0-1.0                 |
| education_match      | float   | 学历匹配度，范围 0.0-1.0                 |
| experience_qualified | boolean | 经验是否符合要求                         |

#### gap_analysis 结构

| 名称       | 类型   | 说明             |
| ---------- | ------ | ---------------- |
| skills     | object | 技能差距分析     |
| experience | object | 经验差距分析     |
| education  | object | 学历差距分析     |

#### gap_analysis.skills 结构

| 名称             | 类型  | 说明                                   |
| ---------------- | ----- | -------------------------------------- |
| required_missing | array | 缺失的必需技能列表                     |
| optional_missing | array | 缺失的可选技能列表                     |
| matched          | array | 已匹配的技能列表（含level和is_required） |
| match_rate       | float | 必需技能匹配率，范围 0.0-1.0           |

#### gap_analysis.experience 结构

| 名称          | 类型    | 说明                               |
| ------------- | ------- | ---------------------------------- |
| your_years    | float   | 你的工作年限                       |
| required_text | string  | 职位要求的经验描述                 |
| is_qualified  | boolean | 是否符合经验要求                   |
| gap_years     | float   | 差距年数（负数表示不足）           |

#### gap_analysis.education 结构

| 名称          | 类型    | 说明               |
| ------------- | ------- | ------------------ |
| your_text     | string  | 你的学历文本       |
| required_text | string  | 职位要求的学历文本 |
| is_qualified  | boolean | 是否符合学历要求   |

#### career_roadmap 结构（新增）

| 名称                  | 类型   | 说明                           |
| --------------------- | ------ | ------------------------------ |
| overview              | object | 概览信息                       |
| technology_stacks     | array  | 技术栈分类列表                 |
| phase_roadmap         | array  | 分阶段学习路线                 |
| immediate_suggestions | array  | 即时建议列表                   |

#### career_roadmap.overview 结构

| 名称               | 类型    | 说明                                       |
| ------------------ | ------- | ------------------------------------------ |
| target_position    | string  | 目标职位名称（来自job_info.job_title）     |
| current_level      | string  | 当前水平（如"本科·3.5年经验"）             |
| skill_gaps_count   | integer | 技能差距数量                               |
| plan_duration_days | integer | 计划总时长（天），计算规则：gaps_count * 30 |
| milestones_count   | integer | 里程碑数量，等于skill_gaps_count           |

#### career_roadmap.technology_stacks 数组元素

| 名称     | 类型   | 说明                                    |
| -------- | ------ | --------------------------------------- |
| category | string | 技术栈分类名称（LLM生成，如"容器化技术"） |
| skills   | array  | 该技术栈下的技能列表                    |

#### career_roadmap.phase_roadmap 数组元素

| 名称          | 类型    | 说明                                       |
| ------------- | ------- | ------------------------------------------ |
| phase         | integer | 阶段编号（1、2、3...）                     |
| title         | string  | 阶段标题（LLM生成，如"容器化基础"）        |
| description   | string  | 阶段描述（LLM生成，30-50字）               |
| duration_days | integer | 阶段时长（天），通常为30                   |
| skills        | array   | 该阶段涉及的技能标签                       |
| based_on_gap  | string  | 对应的缺失技能（来自required_missing）     |

#### career_roadmap.immediate_suggestions 数组元素

| 名称        | 类型   | 说明                               |
| ----------- | ------ | ---------------------------------- |
| title       | string | 建议标题（LLM生成）                |
| description | string | 建议描述（LLM生成，20-40字）       |

#### learning_plan_structured 结构（新增）

| 名称   | 类型  | 说明                     |
| ------ | ----- | ------------------------ |
| skills | array | 结构化技能学习计划列表   |

#### learning_plan_structured.skills 数组元素

| 名称            | 类型   | 说明                                          |
| --------------- | ------ | --------------------------------------------- |
| skill_name      | string | 技能名称                                      |
| priority        | string | 优先级："高"/"中"/"低"                        |
| reason          | string | 学习理由（一句话，20字以内）                  |
| learning_steps  | array  | 学习步骤（按周分解）                          |
| resources       | array  | 推荐学习资源列表                              |
| estimated_weeks | integer | 预计学习周数                                  |
| difficulty      | string | 难度："简单"/"中等"/"困难"                    |

#### learning_plan_structured.resources 数组元素

| 名称 | 类型   | 说明                                   |
| ---- | ------ | -------------------------------------- |
| name | string | 资源名称                               |
| type | string | 资源类型："文档"/"视频"/"书籍"        |
| url  | string | 资源链接（可能为空字符串）             |

### 数据来源说明

所有数据均为真实数据，无假数据：

| 数据项                    | 来源                                                |
| ------------------------- | --------------------------------------------------- |
| match_analysis            | 向量相似度计算的真实匹配分数                        |
| gap_analysis              | 基于数据库skill表和job_skill_requirement表计算      |
| target_position           | 数据库job_info.job_title                            |
| current_level             | 数据库job_seeker.education + work_experience_year   |
| skill_gaps_count          | gap_analysis.skills.required_missing数组长度        |
| technology_stacks         | LLM对职位要求技能进行语义分类                       |
| phase_roadmap             | LLM基于真实缺失技能生成的学习路线                   |
| immediate_suggestions     | LLM基于用户真实背景和差距生成的建议                 |
| learning_plan_structured  | LLM基于缺失技能生成的结构化学习计划（最多5个技能）  |

### 性能说明

- 响应时间：8-12秒（差距分析3秒 + roadmap生成3-5秒 + 学习计划3-5秒）
- 缓存：24小时有效
- 建议：前端展示骨架屏，数据返回后一次性渲染

### 前端展示建议

**⚠️ 重要：判断是否有缺失技能的正确方式**

前端必须使用 `gap_analysis.skills.required_missing` 来判断是否有缺失技能，而不是判断 `learning_plan_structured` 是否存在！

**错误做法**：
```javascript
// ❌ 错误
if (!data.learning_plan_structured) {
  显示("恭喜！你已掌握所有必备技能");  // 可能是LLM失败而不是没有缺失
}
```

**正确做法**：
```javascript
// ✅ 正确
const hasMissingSkills = data.gap_analysis.skills.required_missing.length > 0;

if (!hasMissingSkills) {
  显示("🎉 恭喜！你已掌握所有必备技能");
} else {
  // 有缺失技能，尝试显示学习计划
  if (data.learning_plan_structured?.skills?.length > 0) {
    显示学习计划卡片();
  } else {
    // LLM生成失败，显示降级内容
    显示缺失技能列表(data.gap_analysis.skills.required_missing);
  }
}
```

#### 1. 顶部概览
使用 `overview` 数据展示目标、当前状态、计划周期

#### 2. 匹配度仪表盘
使用 `match_analysis` 绘制3个环形进度图（overall_score、skill_match、education_match）

#### 3. 技术栈标签云
使用 `technology_stacks` 展示分类技能标签

#### 4. 技能差距列表
- 红色标签：`gap_analysis.skills.required_missing`
- 绿色标签+进度条：`gap_analysis.skills.matched`（根据your_level显示等级）

#### 5. 阶段路线时间轴
使用 `phase_roadmap` 绘制垂直时间线

#### 6. 即时建议卡片
使用 `immediate_suggestions` 展示可操作建议列表

#### 7. 结构化学习计划（新增，重点）
使用 `learning_plan_structured.skills` 展示学习卡片：

**卡片布局**（每个技能一个卡片）：
- 顶部：技能名 + 优先级徽章（红色=高，橙色=中，蓝色=低）+ 预计周数
- 中间：学习理由（一句话）
- 展开区域：
  - 学习步骤（时间轴样式，按周展示）
  - 推荐资源（图标+名称，可点击链接）
- 底部：难度标签 + 行动按钮

**优先级颜色**：
- "高"：红色边框/徽章（#FF3B30）
- "中"：橙色边框/徽章（#FF9500）
- "低"：蓝色边框/徽章（#007AFF）

**卡片交互**：
- 默认收起学习步骤和资源
- 点击卡片展开详情
- 资源如果有url，显示为可点击链接
- 底部"开始学习"按钮

**列表排序**：
按priority排序：高优先级在前

**空状态处理（重要）**：

前端应根据 `gap_analysis.skills.required_missing` 判断是否有缺失技能：

```javascript
if (data.gap_analysis.skills.required_missing.length === 0) {
  // 真的没有缺失技能
  显示("🎉 恭喜！你已掌握所有必备技能");
} else {
  // 有缺失技能
  if (data.learning_plan_structured && data.learning_plan_structured.skills.length > 0) {
    // 显示学习计划卡片
  } else {
    // LLM生成失败或未返回，显示降级UI
    显示("正在生成学习计划，请稍后刷新" 或 直接显示required_missing列表);
  }
}
```

**注意**：不要单独判断 `learning_plan_structured` 是否存在，因为：
- LLM生成失败时该字段不存在，但仍有缺失技能
- 应该以 `gap_analysis.skills.required_missing` 为准

---

## POST 职业路径分析（旧版，仅返回差距分析）

此部分已被上面的增强版替代，此处保留旧接口文档供参考。

---

## GET 学习计划（SSE流式输出）

GET /smarthire/api/ai/career-planning/{jobId}/learning-plan

### 功能说明

通过SSE（Server-Sent Events）流式输出LLM生成的学习计划，前端可实时显示生成内容。

### 请求参数

| 名称   | 位置 | 类型    | 必选 | 说明                                 |
| ------ | ---- | ------- | ---- | ------------------------------------ |
| jobId  | path | integer | 是   | 职位ID                               |
| userId | -    | integer | -    | 从JWT Token中自动提取，无需前端传递 |

### 请求头

| 名称          | 类型   | 必选 | 说明           |
| ------------- | ------ | ---- | -------------- |
| Authorization | string | 是   | Bearer <token> |
| Accept        | string | 否   | text/event-stream（推荐） |

### 响应格式

**Content-Type**: `text/event-stream`

**SSE事件格式**：

```
data: {"type":"start","message":"开始生成学习计划..."}

data: {"type":"chunk","content":"### Docker\n- 学习理由：..."}

data: {"type":"chunk","content":"容器化部署是必备技能\n- 学习路径：..."}

data: {"type":"done"}
```

### 事件类型说明

| 类型    | 说明                     |
| ------- | ------------------------ |
| start   | 开始生成，包含提示消息   |
| chunk   | 内容块，包含部分生成文本 |
| done    | 生成完成                 |
| error   | 生成错误，包含错误消息   |

### 前端使用示例

```javascript
const eventSource = new EventSource(
  `/api/ai/career-planning/${jobId}/learning-plan?Authorization=Bearer ${token}`
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

## GET 面试准备（SSE流式输出）

GET /smarthire/api/ai/career-planning/{jobId}/interview-prep

### 功能说明

通过SSE（Server-Sent Events）流式输出LLM生成的面试准备建议，前端可实时显示生成内容。

### 请求参数

| 名称   | 位置 | 类型    | 必选 | 说明                                 |
| ------ | ---- | ------- | ---- | ------------------------------------ |
| jobId  | path | integer | 是   | 职位ID                               |
| userId | -    | integer | -    | 从JWT Token中自动提取，无需前端传递 |

### 请求头

| 名称          | 类型   | 必选 | 说明           |
| ------------- | ------ | ---- | -------------- |
| Authorization | string | 是   | Bearer <token> |
| Accept        | string | 否   | text/event-stream（推荐） |

### 响应格式

**Content-Type**: `text/event-stream`

**SSE事件格式**：与学习计划相同

### 前端使用示例

```javascript
const eventSource = new EventSource(
  `/api/ai/career-planning/${jobId}/interview-prep?Authorization=Bearer ${token}`
);

// 使用方式与学习计划相同
```

### 性能说明

- 首次Token时间：< 1秒
- 流式输出时间：10-20秒（取决于内容长度）
- 前端可实时显示，无需等待完整响应

---

## POST 职业路径规划（完整版，兼容旧接口）

POST /smarthire/api/ai/career-planning/{jobId}

### 功能说明

**注意：此接口已废弃，建议使用拆分后的三个接口**

为求职者提供基于真实数据的职业路径规划和面试准备建议。包括：
- 匹配度分析（基于向量相似度计算）
- 技能差距分析（必需技能vs已有技能）
- 经验差距分析（工作年限vs职位要求）
- 学历匹配分析
- LLM生成的学习计划
- LLM生成的面试准备建议

**问题**：此接口需要等待所有LLM调用完成（约40秒），用户体验差。

### 请求参数

| 名称          | 位置  | 类型    | 必选 | 说明                                 |
| ------------- | ----- | ------- | ---- | ------------------------------------ |
| jobId         | path  | integer | 是   | 职位ID                               |
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
  "match_analysis": {
    "overall_score": 75,
    "skill_match": 0.6,
    "education_match": 1.0,
    "experience_qualified": true
  },
  "gap_analysis": {
    "skills": {
      "required_missing": ["Docker", "Kubernetes"],
      "optional_missing": ["Redis", "RabbitMQ"],
      "matched": [
        {
          "name": "Java",
          "your_level": 3,
          "is_required": true
        },
        {
          "name": "Spring Boot",
          "your_level": 3,
          "is_required": true
        }
      ],
      "match_rate": 0.6
    },
    "experience": {
      "your_years": 3.5,
      "required_text": "3-5年",
      "is_qualified": true,
      "gap_years": 0.0
    },
    "education": {
      "your_text": "本科",
      "required_text": "本科",
      "is_qualified": true
    }
  },
  "learning_plan": {
    "skills": [
      {
        "skill_name": "Docker",
        "reason": "职位核心要求，容器化部署是必备技能",
        "learning_path": [
          "学习Docker基础概念和命令",
          "实践容器化部署项目",
          "学习Docker Compose和多容器应用"
        ],
        "resources": [
          "Docker官方文档",
          "《Docker实战》书籍",
          "在线课程：Docker从入门到实践"
        ],
        "estimated_weeks": 4
      },
      {
        "skill_name": "Kubernetes",
        "reason": "大规模容器编排的行业标准",
        "learning_path": [
          "掌握K8s核心概念",
          "搭建本地K8s环境",
          "部署微服务应用到K8s"
        ],
        "resources": [
          "Kubernetes官方文档",
          "CNCF在线课程",
          "实验室环境练习"
        ],
        "estimated_weeks": 6
      }
    ],
    "raw_text": "# LLM完整输出文本\n\n（作为备用，前端可直接展示）"
  },
  "interview_prep": {
    "project_tips": "## 项目讲解要点\n\n### 项目1: XXX管理系统\n\n**STAR法则框架：**\n- 情境(Situation): 公司需要开发...\n- 任务(Task): 我负责后端架构设计...\n- 行动(Action): 采用Spring Boot框架，实现了...\n- 结果(Result): 系统上线后支撑了...\n\n**强调要点：**\n1. 技术选型的考量\n2. 性能优化的成果\n3. 团队协作经验",
    "possible_questions": "## 可能的面试问题\n\n1. **技术问题：请解释Spring Boot的自动配置原理**\n   - 回答思路：从@EnableAutoConfiguration注解入手...\n\n2. **技术问题：如何设计一个高并发系统？**\n   - 回答思路：分层考虑（负载均衡、缓存、数据库优化）...\n\n3. **行为问题：描述一次你解决复杂问题的经历**\n   - 回答思路：使用STAR法则，选择项目中的实际案例...",
    "weakness_strategy": "## 弱项应对策略\n\n针对缺失技能（Docker、K8s）：\n- 坦诚态度：\"我目前对容器化技术了解有限\"\n- 积极姿态：\"但我已经开始自学Docker，并且...\"\n- 学习能力：\"在之前的工作中，我快速掌握了...新技术\"\n- 转化优势：\"我的Java基础扎实，可以快速学习相关技术\"",
    "strength_emphasis": "## 优势展示建议\n\n**你的核心优势：**\n1. 扎实的Java和Spring Boot经验（3年+）\n2. 完整的项目经验（XXX管理系统）\n3. 良好的问题解决能力\n\n**如何展示：**\n- 自我介绍时重点提及项目经验和技术栈\n- 用具体数据支撑成果（性能提升X%，处理X请求/秒）\n- 展示持续学习的态度（正在学习容器化技术）",
    "raw_text": "# LLM完整输出文本\n\n（作为备用，前端可直接展示）"
  }
}
```

> 401 Response

```json
{
  "detail": "Invalid token"
}
```

> 404 Response

```json
{
  "detail": "Job 123 not found or Seeker information not found"
}
```

> 500 Response

```json
{
  "detail": "Internal server error: ..."
}
```

> 503 Response (LLM服务不可用时的降级响应)

```json
{
  "match_analysis": {
    "overall_score": 75,
    "skill_match": 0.6,
    "education_match": 1.0,
    "experience_qualified": true
  },
  "gap_analysis": {
    "skills": {
      "required_missing": ["Docker", "Kubernetes"],
      "optional_missing": ["Redis"],
      "matched": [
        {"name": "Java", "your_level": 3, "is_required": true}
      ],
      "match_rate": 0.6
    },
    "experience": {
      "your_years": 3.5,
      "required_text": "3-5年",
      "is_qualified": true,
      "gap_years": 0.0
    },
    "education": {
      "your_text": "本科",
      "required_text": "本科",
      "is_qualified": true
    }
  },
  "learning_plan": null,
  "interview_prep": null,
  "error": "LLM service temporarily unavailable"
}
```

### 返回结果

| 状态码 | 状态码含义                 | 说明                                    |
| ------ | -------------------------- | --------------------------------------- |
| 200    | OK                         | 计算成功                                |
| 401    | Unauthorized               | Token无效或过期                         |
| 404    | Not Found                  | 职位或求职者信息不存在                  |
| 500    | Internal Server Error      | 服务器内部错误                          |
| 503    | Service Unavailable        | LLM服务暂时不可用（返回部分数据）       |

### 返回数据结构

#### match_analysis 结构

| 名称                 | 类型    | 说明                                     |
| -------------------- | ------- | ---------------------------------------- |
| overall_score        | integer | 整体匹配分数，范围 0-100                 |
| skill_match          | float   | 技能匹配度，范围 0.0-1.0                 |
| education_match      | float   | 学历匹配度，范围 0.0-1.0                 |
| experience_qualified | boolean | 经验是否符合要求                         |

#### gap_analysis 结构

| 名称       | 类型   | 说明             |
| ---------- | ------ | ---------------- |
| skills     | object | 技能差距分析     |
| experience | object | 经验差距分析     |
| education  | object | 学历差距分析     |

#### gap_analysis.skills 结构

| 名称             | 类型  | 说明                                   |
| ---------------- | ----- | -------------------------------------- |
| required_missing | array | 缺失的必需技能列表                     |
| optional_missing | array | 缺失的可选技能列表                     |
| matched          | array | 已匹配的技能列表（含level和is_required） |
| match_rate       | float | 必需技能匹配率，范围 0.0-1.0           |

#### gap_analysis.skills.matched 数组元素结构

| 名称        | 类型    | 说明               |
| ----------- | ------- | ------------------ |
| name        | string  | 技能名称           |
| your_level  | integer | 你的技能等级(0-4)  |
| is_required | boolean | 是否为职位必需技能 |

#### gap_analysis.experience 结构

| 名称          | 类型    | 说明                               |
| ------------- | ------- | ---------------------------------- |
| your_years    | float   | 你的工作年限                       |
| required_text | string  | 职位要求的经验描述                 |
| is_qualified  | boolean | 是否符合经验要求                   |
| gap_years     | float   | 差距年数（负数表示不足）           |

#### gap_analysis.education 结构

| 名称          | 类型    | 说明               |
| ------------- | ------- | ------------------ |
| your_text     | string  | 你的学历文本       |
| required_text | string  | 职位要求的学历文本 |
| is_qualified  | boolean | 是否符合学历要求   |

#### learning_plan 结构

| 名称     | 类型   | 说明                                 |
| -------- | ------ | ------------------------------------ |
| skills   | array  | 技能学习计划列表                     |
| raw_text | string | LLM原始输出文本（作为备用展示方案） |

#### learning_plan.skills 数组元素结构

| 名称            | 类型   | 说明                       |
| --------------- | ------ | -------------------------- |
| skill_name      | string | 技能名称                   |
| reason          | string | 为什么需要学习这个技能     |
| learning_path   | array  | 学习路径（步骤列表）       |
| resources       | array  | 推荐的学习资源列表         |
| estimated_weeks | integer | 预计学习周数               |

#### interview_prep 结构

| 名称              | 类型   | 说明                                 |
| ----------------- | ------ | ------------------------------------ |
| project_tips      | string | 项目讲解要点（markdown格式）         |
| possible_questions | string | 可能的面试问题（markdown格式）       |
| weakness_strategy | string | 弱项应对策略（markdown格式）         |
| strength_emphasis | string | 优势展示建议（markdown格式）         |
| raw_text          | string | LLM原始输出文本（作为备用展示方案） |

### 缓存说明

- 相同userId和jobId的请求在24小时内会返回缓存结果
- 如需强制刷新，请添加查询参数 `?force_refresh=true`
- 缓存基于内存实现，服务重启后失效

### 性能说明

- 首次请求（无缓存）：10-25秒（包含LLM流式调用时间，后端内部使用流式输出）
- 缓存命中：< 1秒
- 降级模式（LLM不可用）：2-5秒（仅返回差距分析）

**注意**：此接口需要等待所有LLM调用完成，响应时间较长。**强烈建议使用拆分后的三个接口**。

### 使用建议

**推荐使用拆分接口**：
1. 先调用 `/analysis` 接口，立即展示分析结果（2-5秒）
2. 并行调用 `/learning-plan` 和 `/interview-prep` 接口，通过SSE实时显示生成内容
3. 用户体验更好，无需等待40秒

**如果必须使用此接口**：
1. 前端展示策略：
   - 优先展示match_analysis和gap_analysis（数据可靠，快速返回）
   - 异步加载learning_plan和interview_prep（可能较慢，显示loading状态）
   - 如果LLM返回null，隐藏相关模块

2. 错误处理：
   - 503错误时，仍然展示差距分析部分
   - 提示用户"AI建议暂时不可用，请稍后重试"

3. 数据可视化：
   - overall_score: 圆形仪表盘
   - skill_match, education_match: 雷达图
   - required_missing: 红色标签列表
   - matched: 绿色标签列表（附level进度条）

---

## 数据模型

### 计算匹配分数请求

```json
{
  "jobId": 123
}
```

### 计算匹配分数响应

```json
{
  "matchScore": 85
}
```

### 批量计算匹配分数请求

```json
{
  "seekerProfile": {
    "jobSeekerId": 123,
    "major": "计算机科学与技术",
    "university": "清华大学",
    "highestEducation": "本科",
    "currentCity": "北京",
    "skills": [
      {"name": "Java", "level": 2},
      {"name": "Spring Boot", "level": 2},
      {"name": "MySQL", "level": 1}
    ],
    "workExperiences": [
      {
        "companyName": "XX公司",
        "position": "后端开发",
        "description": "负责系统开发..."
      }
    ],
    "projectExperiences": [
      {
        "projectName": "XX系统",
        "description": "项目描述...",
        "responsibilities": "负责模块开发..."
      }
    ]
  },
  "jobs": [
    {
      "jobId": 1,
      "jobTitle": "Java后端开发实习生",
      "description": "负责后端系统开发...",
      "responsibilities": "1. 参与系统设计...",
      "requirements": "1. 本科及以上学历...",
      "skills": ["Java", "Spring Boot", "MySQL", "Redis"]
    },
    {
      "jobId": 2,
      "jobTitle": "前端开发实习生",
      "description": "负责前端页面开发...",
      "responsibilities": "1. 开发用户界面...",
      "requirements": "1. 熟悉Vue.js...",
      "skills": ["Vue.js", "JavaScript", "HTML", "CSS"]
    }
  ]
}
```

### 批量计算匹配分数响应

```json
{
  "scores": [
    {
      "jobId": 1,
      "matchScore": 85,
      "similarity": 0.8500,
      "details": {
        "skillMatch": 0.9,
        "descriptionMatch": 0.82,
        "educationMatch": 1.0
      }
    },
    {
      "jobId": 2,
      "matchScore": 45,
      "similarity": 0.4500,
      "details": {
        "skillMatch": 0.3,
        "descriptionMatch": 0.5,
        "educationMatch": 1.0
      }
    }
  ]
}
```

### 职业路径规划响应（完整示例）

```json
{
  "match_analysis": {
    "overall_score": 75,
    "skill_match": 0.6,
    "education_match": 1.0,
    "experience_qualified": true
  },
  "gap_analysis": {
    "skills": {
      "required_missing": ["Docker", "Kubernetes"],
      "optional_missing": ["Redis", "RabbitMQ"],
      "matched": [
        {
          "name": "Java",
          "your_level": 3,
          "is_required": true
        },
        {
          "name": "Spring Boot",
          "your_level": 3,
          "is_required": true
        },
        {
          "name": "MySQL",
          "your_level": 2,
          "is_required": true
        }
      ],
      "match_rate": 0.6
    },
    "experience": {
      "your_years": 3.5,
      "required_text": "3-5年",
      "is_qualified": true,
      "gap_years": 0.0
    },
    "education": {
      "your_text": "本科",
      "required_text": "本科",
      "is_qualified": true
    }
  },
  "learning_plan": {
    "skills": [
      {
        "skill_name": "Docker",
        "reason": "职位核心要求，容器化部署是后端开发必备技能",
        "learning_path": [
          "学习Docker基础概念：镜像、容器、仓库",
          "掌握Docker常用命令和Dockerfile编写",
          "实践容器化部署至少2个Spring Boot项目",
          "学习Docker Compose编排多容器应用"
        ],
        "resources": [
          "Docker官方文档中文版",
          "《Docker实战》书籍",
          "B站视频：Docker从入门到实践",
          "实验项目：将现有项目容器化"
        ],
        "estimated_weeks": 4
      },
      {
        "skill_name": "Kubernetes",
        "reason": "大规模容器编排的行业标准，高级后端工程师必备",
        "learning_path": [
          "理解K8s核心概念：Pod、Service、Deployment",
          "使用Minikube搭建本地K8s环境",
          "部署Spring Boot微服务到K8s集群",
          "学习K8s配置管理和监控"
        ],
        "resources": [
          "Kubernetes官方文档",
          "CNCF免费在线课程",
          "《Kubernetes权威指南》",
          "实验室环境：阿里云/腾讯云K8s服务"
        ],
        "estimated_weeks": 6
      }
    ],
    "raw_text": "完整的LLM输出markdown文本..."
  },
  "interview_prep": {
    "project_tips": "## 项目讲解要点\n\n### 项目：XXX管理系统\n\n**STAR法则框架：**\n\n**情境(Situation):** 公司需要开发一个面向XX行业的管理系统，支持XX功能，用户规模预计XX。\n\n**任务(Task):** 我作为后端负责人，负责：\n- 后端架构设计\n- 核心业务模块开发\n- 数据库设计和优化\n- 团队技术指导\n\n**行动(Action):**\n1. 技术选型：采用Spring Boot + MySQL + Redis架构\n2. 设计RESTful API规范，实现XX个核心接口\n3. 使用MyBatis-Plus提升开发效率\n4. 实现Redis缓存，优化热点数据访问\n5. SQL优化：建立合理索引，查询性能提升50%\n\n**结果(Result):**\n- 系统按时上线，运行稳定\n- 支撑日活XX用户，QPS达到XX\n- 接口平均响应时间<200ms\n- 获得团队和领导认可\n\n**强调要点：**\n- 技术选型的理由和权衡\n- 具体的性能优化措施和成果\n- 团队协作和问题解决能力",
    "possible_questions": "## 可能的面试问题\n\n### 技术问题\n\n**1. 请解释Spring Boot的自动配置原理**\n\n*回答思路：*\n- 从@EnableAutoConfiguration注解入手\n- 解释条件注解（@ConditionalOnClass等）\n- 说明spring.factories文件的作用\n- 举例：数据源自动配置的过程\n\n**2. 如何设计一个高并发系统？**\n\n*回答思路：*\n- 负载均衡：Nginx/F5分发流量\n- 缓存：Redis缓存热点数据\n- 数据库：读写分离、分库分表\n- 异步处理：消息队列削峰填谷\n- 限流降级：Sentinel/Hystrix保护系统\n- 结合项目经验说明具体实践\n\n**3. MySQL索引优化有哪些实践？**\n\n*回答思路：*\n- 索引类型选择（B+树、Hash）\n- 联合索引的最左前缀原则\n- 避免索引失效（函数、隐式转换）\n- Explain分析执行计划\n- 结合项目中的实际优化案例\n\n### 行为问题\n\n**4. 描述一次你解决复杂技术问题的经历**\n\n*回答思路（STAR法则）：*\n- S: 项目中遇到XX性能瓶颈\n- T: 需要将响应时间从Xs降低到Xms\n- A: 分析定位、优化方案、实施步骤\n- R: 最终效果和收获\n\n**5. 如何在团队中推动技术改进？**\n\n*回答思路：*\n- 发现问题、提出方案\n- 技术分享、说服团队\n- 渐进式推进、总结复盘\n- 结合实际案例",
    "weakness_strategy": "## 弱项应对策略\n\n### 针对缺失技能（Docker、Kubernetes）\n\n**坦诚且积极的回应策略：**\n\n*面试官可能的问题：*\n\"你对Docker/K8s有了解吗？\"\n\n*推荐回答：*\n\"我坦诚地说，目前我对容器化技术的实践经验还不够丰富。但是：\n\n1. **已有认知**：我了解Docker的基本原理，知道它解决了环境一致性和快速部署的问题。\n\n2. **正在学习**：我已经开始系统学习Docker，在本地环境搭建了测试环境，并尝试将之前的项目进行容器化改造。\n\n3. **学习能力**：在之前的工作中，我快速掌握了Spring Cloud微服务技术栈，从零到独立开发只用了2个月。我相信容器化技术也能快速上手。\n\n4. **相关经验**：虽然没有生产环境的容器化经验，但我在项目部署、环境配置方面有丰富经验，理解DevOps的理念。\n\n5. **学习计划**：如果有机会加入团队，我会在入职前集中学习Docker和K8s，并在工作中快速实践提升。\"\n\n**关键要点：**\n- ✅ 诚实承认不足\n- ✅ 展示学习态度和计划\n- ✅ 强调学习能力和相关经验\n- ✅ 表达加入团队后快速成长的决心\n- ❌ 不要夸大或编造经验\n- ❌ 不要说\"这个很简单，我能快速学会\"（显得轻视）",
    "strength_emphasis": "## 优势展示建议\n\n### 你的核心优势\n\n1. **扎实的Java和Spring Boot经验（3.5年）**\n   - 熟悉Spring生态体系\n   - 有完整项目从0到1的经验\n   - 代码规范和工程化实践经验\n\n2. **完整的后端项目经验**\n   - XXX管理系统：负责后端架构和核心模块\n   - 具备系统设计和性能优化能力\n   - 有团队协作和技术指导经验\n\n3. **良好的问题解决能力**\n   - 能够独立分析和解决技术问题\n   - 有SQL优化、性能调优的实战经验\n   - 快速学习新技术的能力\n\n### 如何展示优势\n\n**1. 自我介绍时（60秒版本）**\n\n\"您好，我是XX，毕业于XX大学计算机专业，有3.5年Java后端开发经验。\n\n我最近的项目是XXX管理系统，作为后端负责人，我主导了整个后端架构设计，采用Spring Boot + MySQL + Redis技术栈，实现了XX个核心业务模块。系统上线后支撑了XX用户规模，接口响应时间控制在200ms以内。\n\n在项目中，我通过SQL优化和Redis缓存，将系统性能提升了50%，这让我对系统性能优化有了深入理解。\n\n我的技术栈主要是Java、Spring Boot、MySQL、Redis，同时正在学习Docker和K8s等容器化技术。我相信扎实的基础加上持续学习，能够快速适应新的技术环境。\n\n贵公司的这个岗位很吸引我，我很期待能有机会加入团队，贡献自己的经验并持续成长。\"\n\n**2. 项目讲解时**\n- 用数据说话：性能提升XX%，支撑XX用户，QPS达到XX\n- 突出难点：遇到的技术挑战和解决方案\n- 展示思考：技术选型的考量，架构设计的权衡\n\n**3. 回答问题时**\n- 结合实际项目经验举例\n- 展示深度：不仅知道\"怎么做\"，更理解\"为什么\"\n- 保持谦虚：承认不足，展示学习态度\n\n**4. 反问环节**\n- 问技术相关：\"团队目前的技术栈和架构是怎样的？\"\n- 问成长相关：\"新人入职后，团队会有哪些培养计划？\"\n- 问业务相关：\"这个职位最主要的技术挑战是什么？\"\n\n这些问题既显示你的专业性，又表达了你对岗位的重视和学习意愿。",
    "raw_text": "完整的LLM输出markdown文本..."
  }
}
```

---

## 匹配分数计算说明

### 单个匹配分数计算

基于向量相似度计算：
1. 从数据库获取岗位信息和求职者信息
2. 构建求职者文本和岗位文本
3. 使用 `text2vec-base-chinese` 模型转换为向量
4. 计算余弦相似度并转换为 0-100 分数

### 批量匹配分数计算

返回详细的相似度信息：
1. **整体相似度**：基于完整文本的向量相似度
2. **技能匹配度**：求职者技能与岗位技能要求的匹配比例
3. **描述匹配度**：求职者描述与岗位描述的向量相似度
4. **教育背景匹配度**：学历要求的匹配程度（1.0表示完全匹配，0.0表示不匹配）

---

## 日志说明

服务运行时会输出以下日志：

- 启动时：MySQL 和 Milvus Lite 连接状态
- 请求时：FastAPI 自动记录 HTTP 请求
- 错误时：认证失败、数据库查询错误等详细信息

日志格式：`%(asctime)s - %(name)s - %(levelname)s - %(message)s`

日志级别：INFO
