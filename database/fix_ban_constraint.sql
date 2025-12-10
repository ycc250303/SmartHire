-- 修复封禁表的唯一约束，支持重复解封
-- 删除原有的唯一约束
ALTER TABLE ban_record DROP INDEX uk_user_active_ban;

-- 添加新的唯一约束，只限制active状态
-- 这样确保一个用户只能有一条active状态的记录，但可以有多条lifted状态的记录
ALTER TABLE ban_record ADD UNIQUE KEY uk_user_active_ban (user_id, (CASE WHEN ban_status = 'active' THEN 'active' ELSE CONCAT(ban_status, '_', id) END));