# 数据库脚本说明文档

## 脚本清单

### 1. `create_job_audit_tables.sql`
**用途**: 创建职位审核系统所需的表结构（新建数据库时使用）

**包含内容**:
- 创建 `job_audit_record` 表（包含两级审核字段）
  - 基础字段：id, job_id, job_title, company_id, hr_id, company_name, hr_name 等
  - 兼容字段：audit_status, auditor_id, auditor_name, audited_at
  - 公司审核字段：company_audit_status, company_auditor_id, company_auditor_name, company_audited_at
  - 系统审核字段：system_audit_status, system_auditor_id, system_auditor_name, system_audited_at
  - 所有索引（包括两级审核相关索引）
- 更新 `job_info` 表
  - 添加字段：audit_status, company_audit_status, submitted_at, audited_at
  - 添加索引：idx_audit_status, idx_company_audit_status, idx_submitted_at, idx_audited_at

**使用场景**: 新建数据库或需要完整重建表结构时

---

### 2. `create_hr_audit_table.sql`
**用途**: 创建 HR 审核记录表（新建数据库时使用）

**包含内容**:
- 创建 `hr_audit_record` 表
  - 所有字段：id, hr_id, user_id, company_id, hr_name, audit_status, auditor_id, auditor_name, audit_reason, reject_reason, audited_at, created_at, updated_at
  - 所有索引：idx_hr_id, idx_user_id, idx_company_id, idx_audit_status, idx_auditor_id

**使用场景**: 新建数据库或需要单独创建 HR 审核表时

---

### 3. `update_company_admin_schema.sql`
**用途**: 更新现有数据库表结构，支持公司管理员和两级审核功能（更新现有数据库时使用）

**包含内容**:
- 更新 `hr_info` 表
  - 添加字段：is_company_admin
  - 添加索引：idx_company_admin
- 更新 `job_audit_record` 表（如果表已存在）
  - 添加公司审核字段：company_audit_status, company_auditor_id, company_auditor_name, company_audited_at
  - 添加系统审核字段：system_audit_status, system_auditor_id, system_auditor_name, system_audited_at
  - 添加索引：idx_company_audit_status, idx_system_audit_status, idx_company_auditor_id
- 更新 `job_info` 表
  - 添加字段：audit_status, company_audit_status, submitted_at, audited_at
  - 添加索引：idx_audit_status, idx_company_audit_status, idx_submitted_at, idx_audited_at
- 创建 `hr_audit_record` 表（如果不存在）
  - 完整的表结构和索引

**使用场景**: 更新现有数据库，添加新功能支持

**注意事项**:
- 如果字段或索引已存在，ALTER TABLE 语句会报错，这是正常的
- 建议在执行前备份数据库
- 可以使用 `SHOW COLUMNS` 和 `SHOW INDEX` 检查字段和索引是否存在

---

## 执行顺序

### 场景一：新建数据库
1. 执行 `schema_phase1.sql`（基础表结构）
2. 执行 `create_job_audit_tables.sql`（职位审核表）
3. 执行 `create_hr_audit_table.sql`（HR审核表）
4. 执行其他功能相关的 SQL 脚本

### 场景二：更新现有数据库
1. **备份数据库**（重要！）
2. 执行 `update_company_admin_schema.sql`（一次性更新所有相关表）
3. 检查执行结果，确认所有字段和索引都已添加

---

## 字段说明

### hr_info 表新增字段
- `is_company_admin`: TINYINT，默认值 0
  - 0: 普通HR
  - 1: 公司管理员

### job_audit_record 表新增字段
**公司审核相关**:
- `company_audit_status`: 公司审核状态
- `company_auditor_id`: 公司审核员ID
- `company_auditor_name`: 公司审核员姓名
- `company_audited_at`: 公司审核时间

**系统审核相关**:
- `system_audit_status`: 系统审核状态
- `system_auditor_id`: 系统审核员ID
- `system_auditor_name`: 系统审核员姓名
- `system_audited_at`: 系统审核时间

### job_info 表新增字段
- `audit_status`: 审核状态（兼容字段，包含完整流程状态）
- `company_audit_status`: 公司审核状态（冗余字段，用于快速查询）
- `submitted_at`: 提交审核时间
- `audited_at`: 审核完成时间

### hr_audit_record 表字段
- 完整的 HR 审核记录信息
- 支持公司管理员审核 HR 的功能

---

## 审核状态值说明

### 岗位审核状态
- `draft`: 草稿
- `company_pending`: 待公司审核
- `system_pending`: 待系统审核
- `pending`: 待审核（兼容字段）
- `approved`: 已通过
- `rejected`: 已拒绝
- `modified`: 需修改

### HR审核状态
- `pending`: 待审核
- `approved`: 已通过
- `rejected`: 已拒绝

---

## 索引说明

所有新增索引都用于优化查询性能：

1. **hr_info 表**
   - `idx_company_admin`: 按公司ID和公司管理员状态查询

2. **job_audit_record 表**
   - `idx_company_audit_status`: 按公司审核状态查询
   - `idx_system_audit_status`: 按系统审核状态查询
   - `idx_company_auditor_id`: 按公司审核员ID查询

3. **job_info 表**
   - `idx_audit_status`: 按审核状态查询
   - `idx_company_audit_status`: 按公司审核状态查询
   - `idx_submitted_at`: 按提交时间查询
   - `idx_audited_at`: 按审核完成时间查询

4. **hr_audit_record 表**
   - `idx_hr_id`: 按HR ID查询
   - `idx_user_id`: 按用户ID查询
   - `idx_company_id`: 按公司ID查询
   - `idx_audit_status`: 按审核状态查询
   - `idx_auditor_id`: 按审核员ID查询

---

## 验证脚本

执行完脚本后，可以使用以下 SQL 验证：

```sql
-- 检查 hr_info 表字段
SHOW COLUMNS FROM `hr_info` LIKE 'is_company_admin';
SHOW INDEX FROM `hr_info` WHERE Key_name = 'idx_company_admin';

-- 检查 job_audit_record 表字段
SHOW COLUMNS FROM `job_audit_record` LIKE 'company_audit_status';
SHOW COLUMNS FROM `job_audit_record` LIKE 'system_audit_status';
SHOW INDEX FROM `job_audit_record` WHERE Key_name = 'idx_company_audit_status';

-- 检查 job_info 表字段
SHOW COLUMNS FROM `job_info` LIKE 'company_audit_status';
SHOW INDEX FROM `job_info` WHERE Key_name = 'idx_company_audit_status';

-- 检查 hr_audit_record 表是否存在
SHOW TABLES LIKE 'hr_audit_record';
SHOW COLUMNS FROM `hr_audit_record`;
```

---

## 更新日期
2025-12-13

## 版本
2.0 - 支持公司管理员和两级审核功能

