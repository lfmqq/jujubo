-- ==========================================
-- 迁移脚本：为 sys_user 表增加 phone 字段
-- 适用于已有数据库的增量升级
-- ==========================================
ALTER TABLE `sys_user` ADD COLUMN `phone` varchar(20) NULL DEFAULT NULL COMMENT '手机号' AFTER `avatar`;

-- 给管理员设置一个示例手机号（可选，按需修改）
-- UPDATE `sys_user` SET `phone` = '13800138000' WHERE `username` = 'admin';
