# 职业路径规划功能设计文档

## 1. 功能概述

为求职者提供基于真实数据的职业发展路径规划和面试准备建议。所有数值指标均从数据库计算得出，LLM仅用于生成文本建议和学习资源推荐。

## 2. 系统架构

```
前端请求 → FastAPI路由 → 数据获取层 → 差距分析层 → LLM服务层 → 返回结构化数据
                                  ↓
                            MySQL数据库
```

## 3. 数据流设计

### 3.1 输入数据

- **请求参数**：
  - `jobId`: 职位ID（从路径参数获取）
  - `userId`: 用户ID（从JWT Token自动提取）

### 3.2 数据获取

#### 现有能力（已实现）：
- `get_job_info(db, job_id)` - 职位基本信息
- `get_job_skills(db, job_id)` - 职位技能列表
- `get_seeker_info(db, user_id)` - 求职者基本信息
- `get_seeker_education(db, job_seeker_id)` - 教育背景
- `get_seeker_skills(db, job_seeker_id)` - 求职者技能
- `get_seeker_work_experience(db, job_seeker_id)` - 工作经历
- `get_seeker_project_experience(db, job_seeker_id)` - 项目经历
- `calculate_single_match_score(db, user_id, job_id)` - 整体匹配分数
- `calculate_skill_match(seeker_skills, job_skills)` - 技能匹配度
- `calculate_education_match(seeker_edu, job_edu)` - 学历匹配度

#### 需要扩展的功能：

##### 3.2.1 扩展 `get_job_info`
**位置**：`app/services/db_service.py`

**原因**：当前只返回部分字段，需要增加：
- `experience_required` (TINYINT) - 经验要求
- `salary_min` (DECIMAL) - 最低薪资
- `salary_max` (DECIMAL) - 最高薪资

**实现方式**：
```python
# 在返回的dict中增加字段
return {
    "id": job.id,
    "job_title": job.job_title,
    "description": job.description or "",
    "responsibilities": job.responsibilities or "",
    "requirements": job.requirements or "",
    "education_required": job.education_required,
    "experience_required": job.experience_required,  # 新增
    "salary_min": float(job.salary_min) if job.salary_min else None,  # 新增
    "salary_max": float(job.salary_max) if job.salary_max else None,  # 新增
    "city": job.city,
}
```

##### 3.2.2 新增 `get_job_skills_detailed`
**位置**：`app/services/db_service.py`

**目的**：获取技能及其是否必需的信息

**实现方式**：
```python
def get_job_skills_detailed(db: Session, job_id: int) -> List[Dict[str, Any]]:
    """
    获取职位技能详细信息（包含是否必需）
    返回格式：[{"name": "Java", "is_required": True}, ...]
    """
    skills = (
        db.query(JobSkillRequirement.skill_name, JobSkillRequirement.is_required)
        .filter(JobSkillRequirement.job_id == job_id)
        .order_by(desc(JobSkillRequirement.is_required), JobSkillRequirement.id)
        .all()
    )
    return [
        {"name": skill[0], "is_required": bool(skill[1])}
        for skill in skills if skill[0]
    ]
```

##### 3.2.3 新增 `calculate_work_years`
**位置**：`app/services/db_service.py`

**目的**：计算求职者总工作年限

**实现方式**：
```python
from datetime import datetime
from dateutil.relativedelta import relativedelta

def calculate_work_years(work_experiences: List[Dict[str, Any]]) -> float:
    """
    计算总工作年限（年）
    输入：work_experience表的查询结果
    输出：工作年数（保留1位小数）
    """
    total_months = 0
    for exp in work_experiences:
        start_date = exp.get("start_date")
        end_date = exp.get("end_date") or datetime.now()
        
        if start_date:
            # 计算月数
            delta = relativedelta(end_date, start_date)
            months = delta.years * 12 + delta.months
            total_months += months
    
    return round(total_months / 12, 1)
```

**注意**：需要在`requirements.txt`中添加`python-dateutil`依赖。

### 3.3 差距分析层

创建新模块：`app/services/gap_analysis_service.py`

#### 3.3.1 技能差距分析

```python
def analyze_skill_gap(
    seeker_skills: List[Dict[str, Any]],
    job_skills: List[Dict[str, Any]]
) -> Dict[str, Any]:
    """
    分析技能差距
    
    输入：
    - seeker_skills: [{"name": "Java", "level": 3}, ...]
    - job_skills: [{"name": "Java", "is_required": True}, ...]
    
    输出：
    {
        "required_missing": ["Docker", "K8s"],  # 缺失的必需技能
        "optional_missing": ["Redis"],          # 缺失的可选技能
        "matched": [                            # 已匹配的技能
            {"name": "Java", "your_level": 3, "is_required": True}
        ],
        "match_rate": 0.75                      # 必需技能匹配率
    }
    """
```

**实现逻辑**：
1. 提取求职者技能名称集合（小写）
2. 遍历职位技能要求：
   - 如果is_required=True且不在求职者技能中 → required_missing
   - 如果is_required=False且不在求职者技能中 → optional_missing
   - 如果在求职者技能中 → matched（附上level）
3. 计算match_rate = 已匹配必需技能数 / 总必需技能数

#### 3.3.2 经验差距分析

```python
def analyze_experience_gap(
    work_experiences: List[Dict[str, Any]],
    experience_required: int
) -> Dict[str, Any]:
    """
    分析工作经验差距
    
    输入：
    - work_experiences: 工作经历列表（含start_date, end_date）
    - experience_required: 0=不限, 1=1-3年, 2=3-5年, 3=5-10年, 4=10年以上
    
    输出：
    {
        "your_years": 3.5,                    # 实际工作年限
        "required_level": 1,                  # 要求等级
        "required_text": "1-3年",             # 要求文本
        "is_qualified": True,                 # 是否符合
        "gap_years": 0.0                      # 差距年数（负数表示不足）
    }
    """
```

**实现逻辑**：
1. 调用`calculate_work_years`计算总年限
2. 根据experience_required映射到年限范围：
   - 0: 不限制（返回is_qualified=True）
   - 1: 1-3年（下限1年）
   - 2: 3-5年（下限3年）
   - 3: 5-10年（下限5年）
   - 4: 10年以上（下限10年）
3. 判断是否符合：your_years >= 下限
4. 计算gap_years = 下限 - your_years（如果为负则表示不足）

#### 3.3.3 教育差距分析

```python
def analyze_education_gap(
    seeker_education: int,
    job_education_required: int
) -> Dict[str, Any]:
    """
    分析学历差距（复用现有的calculate_education_match逻辑）
    
    输入：
    - seeker_education: 0-4（高中/专科/本科/硕士/博士）
    - job_education_required: 0-4
    
    输出：
    {
        "your_level": 2,
        "your_text": "本科",
        "required_level": 2,
        "required_text": "本科",
        "is_qualified": True,
        "match_score": 1.0
    }
    """
```

**实现逻辑**：
- 使用映射表：`{0: "高中及以下", 1: "专科", 2: "本科", 3: "硕士", 4: "博士"}`
- 调用现有的`calculate_education_match`获取匹配度
- 判断is_qualified = (your_level >= required_level)

### 3.4 LLM服务层

#### 3.4.1 实现LLM API调用

**位置**：`app/services/llm_service.py`

**需要实现的方法**：`_call_api`

**技术方案**：
- 使用`requests`库发送HTTP POST请求到通义千问API
- 使用OpenAI compatible格式
- 不强制要求JSON格式输出（因为通义千问的JSON模式支持可能不稳定）
- 采用markdown格式输出，后续用正则解析

**实现方式**：
```python
import requests
import json

def _call_api(self, request_data: Dict[str, Any]) -> Dict[str, Any]:
    """
    调用通义千问API
    """
    headers = {
        "Authorization": f"Bearer {self.api_key}",
        "Content-Type": "application/json"
    }
    
    response = requests.post(
        f"{self.base_url}/chat/completions",
        headers=headers,
        json=request_data,
        timeout=30
    )
    
    if response.status_code != 200:
        raise Exception(f"LLM API error: {response.status_code}, {response.text}")
    
    return response.json()
```

#### 3.4.2 设计LLM输入Prompt

**Prompt 1: 学习计划生成**

```
你是一名职业规划顾问。基于以下求职者与目标职位的差距分析，为求职者制定技能提升计划。

【缺失的必需技能】
{required_missing_skills}

【缺失的可选技能】
{optional_missing_skills}

【已具备的技能】
{matched_skills}

【职位描述】
{job_description}

【职位要求】
{job_requirements}

请为每个缺失的必需技能生成学习建议，格式如下（每个技能一段）：

### 技能名称
- 学习理由：为什么需要学习这个技能
- 学习路径：1) 步骤1 2) 步骤2 3) 步骤3
- 推荐资源：官方文档、书籍、在线课程等
- 预计周数：需要多少周掌握

请确保建议实用、具体、可操作。
```

**Prompt 2: 面试准备建议**

```
你是一名面试官和职业顾问。基于求职者的背景和目标职位，提供面试准备建议。

【目标职位】
{job_title}

【职位要求】
{job_requirements}

【求职者项目经历】
{project_experiences}

【求职者工作经历】
{work_experiences}

【求职者缺失技能】
{missing_skills}

请提供以下内容：

## 1. 项目讲解要点
针对求职者的每个项目，用STAR法则（情境-任务-行动-结果）设计讲解框架。

## 2. 可能的面试问题
列出3-5个针对该职位的技术问题，并提供回答思路。

## 3. 弱项应对策略
针对缺失的技能，如何在面试中诚实且积极地回应。

## 4. 优势展示建议
如何强调求职者已具备的技能和项目经验。

请确保建议具体、实用、符合该职位特点。
```

#### 3.4.3 LLM响应解析

**策略**：
- LLM返回markdown格式文本
- 使用正则表达式提取结构化信息
- 如果解析失败，返回原始文本供前端直接展示

**实现方式**：
```python
def parse_learning_plan(llm_output: str) -> List[Dict[str, Any]]:
    """
    解析学习计划文本
    
    输入：LLM输出的markdown文本
    输出：结构化的学习计划列表
    """
    # 按### 分割
    # 提取技能名称、学习理由、学习路径等
    # 如果解析失败，返回包含原始文本的简单结构
```

### 3.5 数据缓存策略

**位置**：内存缓存或Redis（优先使用内存缓存简化实现）

**策略**：
- 缓存key: `career_plan:{user_id}:{job_id}`
- 缓存时间：24小时
- 缓存内容：完整的API响应
- 提供`force_refresh`参数强制刷新

**实现方式**：
```python
from functools import lru_cache
from datetime import datetime, timedelta

# 简单的内存缓存
_cache = {}

def get_cached_plan(user_id: int, job_id: int) -> Optional[Dict]:
    key = f"{user_id}:{job_id}"
    if key in _cache:
        cached_data, timestamp = _cache[key]
        if datetime.now() - timestamp < timedelta(hours=24):
            return cached_data
    return None

def set_cached_plan(user_id: int, job_id: int, data: Dict):
    key = f"{user_id}:{job_id}"
    _cache[key] = (data, datetime.now())
```

## 4. API接口设计

### 4.1 端点定义

**路径**：`POST /smarthire/api/ai/career-planning/{jobId}`

**认证**：JWT Bearer Token（必需）

**路径参数**：
- `jobId` (int): 职位ID

**查询参数**（可选）：
- `force_refresh` (bool): 是否强制刷新，默认false

### 4.2 响应结构

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
      "required_missing": ["Docker", "K8s"],
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
  "learning_plan": {
    "skills": [
      {
        "skill_name": "Docker",
        "reason": "职位核心要求",
        "learning_path": ["步骤1", "步骤2"],
        "resources": ["Docker官方文档"],
        "estimated_weeks": 4
      }
    ],
    "raw_text": "LLM原始输出（作为备用）"
  },
  "interview_prep": {
    "project_tips": "项目讲解建议...",
    "possible_questions": "可能的面试问题...",
    "weakness_strategy": "弱项应对策略...",
    "strength_emphasis": "优势展示建议...",
    "raw_text": "LLM原始输出（作为备用）"
  }
}
```

### 4.3 错误处理

- **401**: Token无效或过期
- **404**: 职位或求职者信息不存在
- **500**: 服务器内部错误
- **503**: LLM服务暂时不可用（返回降级数据，不包含LLM生成内容）

## 5. 实施步骤

### Phase 1: 数据层扩展（1天）
1. 扩展`get_job_info`增加字段
2. 实现`get_job_skills_detailed`
3. 实现`calculate_work_years`
4. 测试数据获取功能

### Phase 2: 差距分析（1天）
1. 创建`gap_analysis_service.py`
2. 实现`analyze_skill_gap`
3. 实现`analyze_experience_gap`
4. 实现`analyze_education_gap`
5. 单元测试

### Phase 3: LLM集成（2天）
1. 实现`llm_service.py`的`_call_api`方法
2. 设计和优化Prompt
3. 实现响应解析逻辑
4. 测试LLM调用（可能需要多次调整Prompt）

### Phase 4: API实现（1天）
1. 创建`api/career_planning.py`
2. 实现路由和请求处理
3. 集成数据获取、差距分析、LLM服务
4. 实现缓存机制

### Phase 5: 测试和优化（1天）
1. 端到端测试
2. 性能优化
3. 错误处理完善
4. 日志完善

## 6. 依赖项

### 新增Python包
```
requests>=2.31.0
python-dateutil>=2.8.2
```

### 现有依赖
- sqlalchemy (已有)
- fastapi (已有)
- sentence-transformers (已有)

## 7. 风险和注意事项

### 7.1 LLM调用风险
- **问题**：通义千问API可能响应慢（5-15秒）
- **缓解**：使用缓存机制，24小时内不重复调用

### 7.2 LLM输出不稳定
- **问题**：LLM可能不按预期格式输出
- **缓解**：提供raw_text字段，前端可直接展示原始文本

### 7.3 数据完整性
- **问题**：部分求职者可能没有填写完整信息
- **缓解**：
  - 判断必填字段是否存在
  - 如果工作经历为空，experience_gap标记为"无法计算"
  - 如果技能为空，skill_gap标记为"无技能信息"

### 7.4 性能问题
- **问题**：LLM调用耗时长
- **缓解**：
  - 考虑异步处理（前端先返回差距分析，LLM结果后续推送）
  - 或者前端显示loading状态，30秒超时

## 8. 后续优化方向

1. **异步处理**：使用Celery等任务队列处理LLM调用
2. **Redis缓存**：替换内存缓存，支持分布式部署
3. **Prompt优化**：根据实际使用效果持续优化Prompt
4. **结构化输出**：如果通义千问支持稳定的JSON输出，可改用结构化格式
5. **A/B测试**：测试不同Prompt的效果，选择最优方案

## 9. 测试计划

### 9.1 单元测试
- 差距分析函数
- 数据获取函数
- 缓存机制

### 9.2 集成测试
- 完整API调用流程
- 错误处理
- 边界情况（数据缺失、LLM失败等）

### 9.3 性能测试
- API响应时间
- 并发请求处理
- 缓存命中率

## 10. 监控和日志

### 10.1 日志记录
- API请求日志（user_id, job_id, timestamp）
- LLM调用日志（耗时、token消耗）
- 错误日志（详细堆栈）
- 缓存命中日志

### 10.2 监控指标
- API响应时间（P50, P95, P99）
- LLM调用成功率
- 缓存命中率
- 错误率

---

**文档版本**：v1.0  
**最后更新**：2025-12-31  
**作者**：SmartHire AI Team

