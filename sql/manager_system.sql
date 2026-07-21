/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50737
 Source Host           : localhost:3306
 Source Schema         : manager_system

 Target Server Type    : MySQL
 Target Server Version : 50737
 File Encoding         : 65001

 Date: 21/07/2026 21:27:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `table_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '表名',
  `table_comment` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '表描述',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '实体类名',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '包路径',
  `module_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '模块名',
  `business_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '业务名',
  `function_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '功能名',
  `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '作者',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_table_name`(`table_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码生成表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gen_table
-- ----------------------------

-- ----------------------------
-- Table structure for iot_device
-- ----------------------------
DROP TABLE IF EXISTS `iot_device`;
CREATE TABLE `iot_device`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `device_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备名称',
  `device_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备标识（SN/MAC）',
  `device_secret` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备密钥',
  `product_id` bigint(20) NOT NULL COMMENT '所属产品ID',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态：0=未激活 1=在线 2=离线',
  `last_online_time` datetime(0) NULL DEFAULT NULL COMMENT '最后上线时间',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备描述',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_device_key`(`device_key`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'IoT设备表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of iot_device
-- ----------------------------
INSERT INTO `iot_device` VALUES (1, '1号温度传感器', 'SN-TH-001', 'abc123', 1, 1, '2026-07-21 21:22:19', '安装在A车间', '2026-07-21 21:22:19', '2026-07-21 21:22:19');
INSERT INTO `iot_device` VALUES (2, '2号温度传感器', 'SN-TH-002', 'abc456', 1, 2, '2026-07-21 19:22:19', '安装在B车间', '2026-07-21 21:22:19', '2026-07-21 21:22:19');
INSERT INTO `iot_device` VALUES (3, '1号电表', 'SN-MT-001', 'def789', 2, 1, '2026-07-21 21:22:19', '主配电柜', '2026-07-21 21:22:19', '2026-07-21 21:22:19');
INSERT INTO `iot_device` VALUES (4, '边缘网关#1', 'SN-GW-001', 'ghj012', 3, 1, '2026-07-21 21:22:19', '厂区东侧', '2026-07-21 21:22:19', '2026-07-21 21:22:19');

-- ----------------------------
-- Table structure for iot_device_data
-- ----------------------------
DROP TABLE IF EXISTS `iot_device_data`;
CREATE TABLE `iot_device_data`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `device_id` bigint(20) NOT NULL COMMENT '设备ID',
  `property_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '属性名',
  `property_value` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '属性值',
  `data_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'string' COMMENT '数据类型',
  `report_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '上报时间',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_device_time`(`device_id`, `report_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'IoT设备数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of iot_device_data
-- ----------------------------
INSERT INTO `iot_device_data` VALUES (1, 1, 'temperature', '26.5', 'float', '2026-07-21 21:22:19', '2026-07-21 21:22:19');
INSERT INTO `iot_device_data` VALUES (2, 1, 'humidity', '58.3', 'float', '2026-07-21 21:22:19', '2026-07-21 21:22:19');
INSERT INTO `iot_device_data` VALUES (3, 2, 'temperature', '31.2', 'float', '2026-07-21 20:22:19', '2026-07-21 20:22:19');
INSERT INTO `iot_device_data` VALUES (4, 3, 'voltage', '220.5', 'float', '2026-07-21 21:22:19', '2026-07-21 21:22:19');
INSERT INTO `iot_device_data` VALUES (5, 3, 'current', '15.8', 'float', '2026-07-21 21:22:19', '2026-07-21 21:22:19');
INSERT INTO `iot_device_data` VALUES (6, 3, 'power', '3.48', 'float', '2026-07-21 21:22:19', '2026-07-21 21:22:19');

-- ----------------------------
-- Table structure for iot_product
-- ----------------------------
DROP TABLE IF EXISTS `iot_product`;
CREATE TABLE `iot_product`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '产品ID',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '产品名称',
  `product_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '产品标识',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '产品描述',
  `device_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'sensor' COMMENT '设备类型：sensor/actuator/gateway',
  `protocol_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'mqtt' COMMENT '通信协议：mqtt/http/coap/tcp',
  `data_format` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'json' COMMENT '数据格式：json/custom',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：1=启用 0=禁用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_product_key`(`product_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'IoT产品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of iot_product
-- ----------------------------
INSERT INTO `iot_product` VALUES (1, '温湿度传感器', 'temp_humidity_v1', '工业级温湿度采集设备', 'sensor', 'mqtt', 'json', 1, '2026-07-21 21:22:19', '2026-07-21 21:22:19');
INSERT INTO `iot_product` VALUES (2, '智能电表', 'smart_meter_v1', '三相智能电表，支持远程抄表', 'sensor', 'http', 'json', 1, '2026-07-21 21:22:19', '2026-07-21 21:22:19');
INSERT INTO `iot_product` VALUES (3, '智能网关', 'gateway_v1', '边缘计算网关，支持多协议接入', 'gateway', 'tcp', 'custom', 1, '2026-07-21 21:22:19', '2026-07-21 21:22:19');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) NULL DEFAULT 0,
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `sort` int(11) NULL DEFAULT 0,
  `status` tinyint(4) NULL DEFAULT 1,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (1, 0, '总经办', 1, 1, '2026-07-01 17:39:30', '2026-07-01 17:39:30');
INSERT INTO `sys_dept` VALUES (2, 1, '销售部', 1, 1, '2026-07-01 17:39:30', '2026-07-01 17:39:30');
INSERT INTO `sys_dept` VALUES (3, 1, '研发部', 2, 1, '2026-07-01 17:39:30', '2026-07-01 17:39:30');
INSERT INTO `sys_dept` VALUES (4, 1, '测试部门', 3, 1, '2026-07-06 22:19:25', '2026-07-06 22:19:25');

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'DEFAULT' COMMENT '任务组',
  `cron_expression` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'cron表达式',
  `invoke_target` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标(beanName.method)',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：1=运行 0=暂停',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '定时任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_job
-- ----------------------------

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `job_id` bigint(20) NULL DEFAULT NULL COMMENT '任务ID',
  `job_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '任务名称',
  `invoke_target` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '调用目标',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '执行状态：0=成功 1=失败',
  `duration` bigint(20) NULL DEFAULT 0 COMMENT '耗时(毫秒)',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '异常信息',
  `exec_time` datetime(0) NULL DEFAULT NULL COMMENT '执行时间',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_job_id`(`job_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '定时任务日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_job_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `path` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由地址',
  `component` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '前端组件',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限标识',
  `type` tinyint(4) NULL DEFAULT NULL COMMENT '0目录 1菜单 2按钮',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `always_show` tinyint(1) NULL DEFAULT 1 COMMENT '是否总是显示：1=总是，0=不是；当为不是且只有一个子菜单时，折叠显示子菜单',
  `visible` tinyint(1) NULL DEFAULT 1 COMMENT '是否显示：1=显示，0=隐藏',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '菜单状态：1=启用，0=禁用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '系统管理', '/system', 'Layout', '', 0, 'setting', 1, '2026-07-01 17:39:30', '2026-07-01 17:39:30', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (2, 1, '用户管理', '/system/user', 'system/user', 'system:user:list', 1, 'user', 1, '2026-07-01 17:39:30', '2026-07-01 17:39:30', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (3, 1, '角色管理', '/system/role', 'system/role', 'system:role:list', 1, 'avatar', 2, '2026-07-01 17:39:30', '2026-07-01 17:39:30', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (4, 1, '菜单管理', '/system/menu', 'system/menu', 'system:menu:list', 1, 'menu', 3, '2026-07-01 17:39:30', '2026-07-01 17:39:30', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (5, 1, '部门管理', '/system/dept', 'system/dept', 'system:dept:list', 1, 'office-building', 4, '2026-07-01 17:39:30', '2026-07-01 17:39:30', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (6, 2, '用户新增', '', '', 'system:user:add', 2, '', 1, '2026-07-01 17:39:30', '2026-07-01 17:39:30', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (7, 2, '用户编辑', '', '', 'system:user:edit', 2, '', 2, '2026-07-01 17:39:30', '2026-07-01 17:39:30', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (8, 2, '用户删除', '', '', 'system:user:remove', 2, '', 3, '2026-07-01 17:39:30', '2026-07-01 17:39:30', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (9, 4, '菜单新增', '', '', 'system:menu:add', 2, '', 1, '2026-07-01 17:39:30', '2026-07-01 17:39:30', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (10, 4, '菜单编辑', '', '', 'system:menu:edit', 2, '', 2, '2026-07-01 17:39:30', '2026-07-01 17:39:30', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (11, 4, '菜单删除', '', '', 'system:menu:remove', 2, '', 3, '2026-07-01 17:39:30', '2026-07-01 17:39:30', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (12, 3, '角色新增', '', '', 'system:role:add', 2, '', 1, '2026-07-01 17:39:30', '2026-07-01 17:39:30', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (13, 3, '角色编辑', '', '', 'system:role:edit', 2, '', 2, '2026-07-01 17:39:30', '2026-07-01 17:39:30', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (14, 3, '角色删除', '', '', 'system:role:remove', 2, '', 3, '2026-07-01 17:39:30', '2026-07-01 17:39:30', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (15, 5, '部门新增', '', '', 'system:dept:add', 2, '', 1, '2026-07-01 17:39:30', '2026-07-01 17:39:30', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (16, 5, '部门编辑', '', '', 'system:dept:edit', 2, '', 2, '2026-07-01 17:39:30', '2026-07-01 17:39:30', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (17, 5, '部门删除', '', '', 'system:dept:remove', 2, '', 3, '2026-07-01 17:39:30', '2026-07-01 17:39:30', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (19, 0, '消息管理', '/message', '', '', 0, 'chat-dot-round', 2, '2026-07-02 11:05:30', '2026-07-06 22:19:05', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (20, 19, '消息通知', '/message/notify', '/system/notify', '', 1, 'bell-filled', 1, '2026-07-02 11:07:20', '2026-07-02 23:00:37', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (21, 20, '消息查询', '', '', 'system:notify:list', 2, '', 1, '2026-07-02 11:11:19', '2026-07-02 11:11:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (22, 20, '通知详情', '', '', 'system:notify:list', 2, '', 2, '2026-07-02 11:12:05', '2026-07-02 11:12:05', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (23, 20, '新增通知', '', '', 'system:notify:add', 2, '', 0, '2026-07-02 11:12:25', '2026-07-02 11:12:25', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (24, 20, '删除通知', '', '', 'system:notify:delete', 2, '', 3, '2026-07-02 11:12:57', '2026-07-02 11:12:57', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (25, 0, '数据面板', '/statistics', '', '', 0, 'data-board', 3, '2026-07-02 22:06:56', '2026-07-06 22:18:57', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (26, 25, '数据统计', '/statistics', 'statistics/index', '', 1, 'data-analysis', 1, '2026-07-02 22:07:53', '2026-07-02 23:00:37', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (28, 0, '首页', '/home', '', '', 0, 'home-filled', 0, '2026-07-02 22:48:52', '2026-07-02 22:48:52', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (29, 26, '数据概览', '', '', 'statistics:overview:list', 2, '', 1, '2026-07-02 23:37:04', '2026-07-02 23:37:04', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (30, 0, '个人中心', '/system/user/profile', '/system/user/profile', '', 1, 'avatar', 4, '2026-07-05 19:27:42', '2026-07-05 21:51:24', 0, 0, 1);
INSERT INTO `sys_menu` VALUES (31, 2, '重置密码', '', '', 'system:user:reset', 2, '', 3, '2026-07-14 00:01:26', '2026-07-14 00:01:26', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (32, 0, '系统监控', 'monitor', 'Layout', '', 0, 'Monitor', 99, '2026-07-14 21:54:49', '2026-07-14 21:54:49', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (33, 32, '操作日志', 'operlog', 'monitor/operlog', 'monitor:operlog:list', 1, 'Document', 1, '2026-07-14 21:54:49', '2026-07-14 21:54:49', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (34, 33, '删除', '', '', 'monitor:operlog:delete', 2, '', 1, '2026-07-14 21:54:49', '2026-07-14 21:54:49', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (35, 33, '清空', '', '', 'monitor:operlog:clean', 2, '', 2, '2026-07-14 21:54:49', '2026-07-14 21:54:49', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (36, 0, '可视化大屏', '/dashboard', 'Layout', '', 0, 'Odometer', 5, '2026-07-21 10:00:00', '2026-07-21 10:00:00', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (37, 36, '机房监控大屏', '/dashboard', 'dashboard/index', '', 1, 'Monitor', 1, '2026-07-21 10:00:00', '2026-07-21 10:00:00', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (38, 36, '全球航运大屏', '/shipping', 'dashboard/shipping', '', 1, 'Ship', 2, '2026-07-21 11:00:00', '2026-07-21 11:00:00', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (39, 0, 'IoT物联网', '/iot', 'Layout', '', 0, 'Cpu', 10, '2026-07-21 21:22:19', '2026-07-21 21:22:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (40, 39, '产品管理', '/iot/product', 'iot/product/index', 'iot:product:list', 1, 'Goods', 1, '2026-07-21 21:22:19', '2026-07-21 21:22:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (41, 39, '设备管理', '/iot/device', 'iot/device/index', 'iot:device:list', 1, 'Monitor', 2, '2026-07-21 21:22:19', '2026-07-21 21:22:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (42, 40, '产品新增', '', '', 'iot:product:add', 2, '', 1, '2026-07-21 21:22:19', '2026-07-21 21:22:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (43, 40, '产品编辑', '', '', 'iot:product:edit', 2, '', 2, '2026-07-21 21:22:19', '2026-07-21 21:22:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (44, 40, '产品删除', '', '', 'iot:product:remove', 2, '', 3, '2026-07-21 21:22:19', '2026-07-21 21:22:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (45, 41, '设备新增', '', '', 'iot:device:add', 2, '', 1, '2026-07-21 21:22:19', '2026-07-21 21:22:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (46, 41, '设备编辑', '', '', 'iot:device:edit', 2, '', 2, '2026-07-21 21:22:19', '2026-07-21 21:22:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (47, 41, '设备删除', '', '', 'iot:device:remove', 2, '', 3, '2026-07-21 21:22:19', '2026-07-21 21:22:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (48, 0, '开发工具', '/tool', 'Layout', '', 0, 'Tools', 20, '2026-07-21 21:22:19', '2026-07-21 21:22:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (49, 48, '代码生成', '/tool/gen', 'tool/gen/index', 'tool:gen:list', 1, 'EditPen', 1, '2026-07-21 21:22:19', '2026-07-21 21:22:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (50, 49, '生成代码', '', '', 'tool:gen:code', 2, '', 1, '2026-07-21 21:22:19', '2026-07-21 21:22:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (51, 32, '定时任务', '/job', 'monitor/job/index', 'monitor:job:list', 1, 'Clock', 2, '2026-07-21 21:22:19', '2026-07-21 21:22:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (52, 32, '调度日志', '/job/log', 'monitor/job/log', 'monitor:job:log', 1, 'Tickets', 3, '2026-07-21 21:22:19', '2026-07-21 21:22:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (53, 51, '任务新增', '', '', 'monitor:job:add', 2, '', 1, '2026-07-21 21:22:19', '2026-07-21 21:22:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (54, 51, '任务编辑', '', '', 'monitor:job:edit', 2, '', 2, '2026-07-21 21:22:19', '2026-07-21 21:22:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (55, 51, '任务删除', '', '', 'monitor:job:remove', 2, '', 3, '2026-07-21 21:22:19', '2026-07-21 21:22:19', 1, 1, 1);

-- ----------------------------
-- Table structure for sys_notify_message
-- ----------------------------
DROP TABLE IF EXISTS `sys_notify_message`;
CREATE TABLE `sys_notify_message`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '通知内容',
  `type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '通知类型：1=系统通知, 2=提醒, 3=私信',
  `sender_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '发送者ID（0=系统）',
  `receiver_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '接收者ID（0=全部用户）',
  `read_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '阅读状态：0=未读, 1=已读',
  `read_time` datetime(0) NULL DEFAULT NULL COMMENT '阅读时间',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0=正常, 1=已删除',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receiver_read`(`receiver_id`, `read_status`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消息通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notify_message
-- ----------------------------
INSERT INTO `sys_notify_message` VALUES (1, '系统升级通知', '系统将于本周末进行版本升级，届时服务可能短暂中断，请提前做好工作安排。', 1, 0, 0, 1, '2026-07-02 11:41:57', 0, '2026-07-02 11:00:39', '2026-07-02 11:41:57');
INSERT INTO `sys_notify_message` VALUES (2, '欢迎使用管理系统', '欢迎加入团队！请及时完善个人资料并修改初始密码。', 1, 0, 0, 1, '2026-07-02 13:38:37', 0, '2026-07-02 11:00:39', '2026-07-02 13:38:36');
INSERT INTO `sys_notify_message` VALUES (3, '任务提醒', '您有一个待审批的工单即将超时，请尽快处理。', 2, 0, 0, 1, '2026-07-02 13:30:01', 0, '2026-07-01 11:00:39', '2026-07-02 13:30:01');
INSERT INTO `sys_notify_message` VALUES (4, '密码修改提醒', '您的账户密码已超过90天未修改，建议及时更新密码以保障账户安全。', 2, 0, 0, 1, NULL, 0, '2026-06-29 11:00:39', '2026-07-02 11:00:39');
INSERT INTO `sys_notify_message` VALUES (5, '1', '11', 1, 0, 0, 1, '2026-07-02 13:36:39', 1, '2026-07-02 13:36:20', '2026-07-02 13:37:37');
INSERT INTO `sys_notify_message` VALUES (6, '1111', '2222', 1, 0, 4, 1, '2026-07-06 23:30:58', 0, '2026-07-06 22:19:47', '2026-07-06 23:30:57');
INSERT INTO `sys_notify_message` VALUES (7, '111', '测试', 1, 0, 4, 1, '2026-07-06 23:30:58', 0, '2026-07-06 22:54:36', '2026-07-06 23:30:57');
INSERT INTO `sys_notify_message` VALUES (8, '1111', '测试', 1, 0, 0, 1, '2026-07-06 23:06:47', 0, '2026-07-06 22:55:04', '2026-07-06 23:06:47');
INSERT INTO `sys_notify_message` VALUES (9, '222', '33333', 2, 0, 4, 1, '2026-07-06 23:30:58', 0, '2026-07-06 23:08:02', '2026-07-06 23:30:57');
INSERT INTO `sys_notify_message` VALUES (10, '222', '3333', 1, 0, 4, 1, '2026-07-06 23:30:39', 0, '2026-07-06 23:08:13', '2026-07-06 23:30:38');
INSERT INTO `sys_notify_message` VALUES (11, '222', '22222', 1, 0, 4, 1, '2026-07-06 23:30:58', 0, '2026-07-06 23:08:35', '2026-07-06 23:30:57');
INSERT INTO `sys_notify_message` VALUES (12, '222', '2222', 1, 0, 3333, 1, '2026-07-06 23:27:09', 0, '2026-07-06 23:08:44', '2026-07-06 23:27:09');
INSERT INTO `sys_notify_message` VALUES (13, '222', '3333', 1, 0, 4, 1, '2026-07-06 23:27:06', 0, '2026-07-06 23:17:34', '2026-07-06 23:27:06');
INSERT INTO `sys_notify_message` VALUES (14, '2222', '2222', 1, 0, 3, 1, '2026-07-06 23:25:06', 0, '2026-07-06 23:18:37', '2026-07-06 23:25:05');

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '模块标题',
  `business_type` int(11) NULL DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除 4查询 5登录 6退出）',
  `operator_type` int(11) NULL DEFAULT 0 COMMENT '操作人类别（0后台 1前端 2手机端）',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求方法（类.方法）',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求方式（GET/POST/...）',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '主机地址（真实IP）',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '返回参数',
  `status` int(11) NULL DEFAULT 0 COMMENT '操作状态（0成功 1失败）',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_oper_name`(`oper_name`) USING BTREE,
  INDEX `idx_business_type`(`business_type`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_oper_time`(`oper_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO `sys_oper_log` VALUES (1, '首页', 4, 1, '', '', 'admin', '', '/home', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 21:54:59');
INSERT INTO `sys_oper_log` VALUES (2, '操作日志', 4, 1, '', '', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 21:55:04');
INSERT INTO `sys_oper_log` VALUES (3, '用户管理', 4, 1, '', '', 'admin', '', '/system/user', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 21:56:14');
INSERT INTO `sys_oper_log` VALUES (4, '操作日志', 4, 1, '', '', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 21:56:15');
INSERT INTO `sys_oper_log` VALUES (5, '首页', 4, 1, '', '', 'admin', '', '/home', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 21:57:29');
INSERT INTO `sys_oper_log` VALUES (6, '操作日志', 4, 1, '', '', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 21:57:31');
INSERT INTO `sys_oper_log` VALUES (7, '角色管理', 4, 1, '', '', 'admin', '', '/system/role', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 21:58:25');
INSERT INTO `sys_oper_log` VALUES (8, '角色管理', 4, 1, '', 'GET', 'admin', '', '/system/role', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 21:59:28');
INSERT INTO `sys_oper_log` VALUES (9, '角色管理', 4, 1, '', 'GET', 'admin', '', '/system/role', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 21:59:29');
INSERT INTO `sys_oper_log` VALUES (10, '角色管理', 4, 1, '', 'GET', 'admin', '', '/system/role', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 21:59:31');
INSERT INTO `sys_oper_log` VALUES (11, '角色管理', 4, 1, '', 'GET', 'admin', '', '/system/role', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 21:59:50');
INSERT INTO `sys_oper_log` VALUES (12, '操作日志', 4, 1, '', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 21:59:52');
INSERT INTO `sys_oper_log` VALUES (13, '角色管理', 4, 1, '', 'GET', 'admin', '', '/system/role', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:00:15');
INSERT INTO `sys_oper_log` VALUES (14, '操作日志', 4, 1, '', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:00:16');
INSERT INTO `sys_oper_log` VALUES (15, '操作日志', 4, 1, '', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:00:30');
INSERT INTO `sys_oper_log` VALUES (16, '用户管理', 4, 1, '', 'GET', 'admin', '', '/system/user', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:00:33');
INSERT INTO `sys_oper_log` VALUES (17, '操作日志', 4, 1, '', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:00:34');
INSERT INTO `sys_oper_log` VALUES (18, '操作日志', 4, 1, '', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:03:47');
INSERT INTO `sys_oper_log` VALUES (19, '操作日志', 4, 1, '', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:03:48');
INSERT INTO `sys_oper_log` VALUES (20, '操作日志', 4, 1, '', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:03:50');
INSERT INTO `sys_oper_log` VALUES (21, '操作日志', 4, 1, '', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:03:51');
INSERT INTO `sys_oper_log` VALUES (22, '操作日志', 4, 1, '', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:03:52');
INSERT INTO `sys_oper_log` VALUES (23, '操作日志', 4, 1, '操作日志', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:05:03');
INSERT INTO `sys_oper_log` VALUES (24, '用户管理', 4, 1, '用户管理', 'GET', 'admin', '', '/system/user', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:05:11');
INSERT INTO `sys_oper_log` VALUES (25, '操作日志', 4, 1, '操作日志', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:05:12');
INSERT INTO `sys_oper_log` VALUES (26, '用户管理', 4, 1, '用户管理', 'GET', 'admin', '', '/system/user', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:05:45');
INSERT INTO `sys_oper_log` VALUES (27, '用户管理', 1, 0, 'com.admin.controller.UserController.add()', 'POST', 'admin', '', '/api/system/user', '127.0.0.1', '内网IP', 'Body: {\"id\":5,\"username\":\"acc\",\"password\":\"******\",\"nickname\":\"测试用户\",\"email\":null,\"deptId\":null,\"avatar\":null,\"status\":1,\"createTime\":\"2026-07-14 22:06:01\",\"updateTime\":\"2026-07-14 22:06:01\",\"roleIds\":[2]}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2026-07-14 22:06:01');
INSERT INTO `sys_oper_log` VALUES (28, '操作日志', 4, 1, '操作日志', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:06:03');
INSERT INTO `sys_oper_log` VALUES (29, '角色管理', 4, 1, '角色管理', 'GET', 'admin', '', '/system/role', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:06:28');
INSERT INTO `sys_oper_log` VALUES (30, '操作日志', 4, 1, '操作日志', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:06:29');
INSERT INTO `sys_oper_log` VALUES (31, '用户退出', 6, 0, 'com.admin.controller.AuthController.logout()', 'POST', '匿名', '', '/api/auth/logout', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:07:51');
INSERT INTO `sys_oper_log` VALUES (32, '用户登录', 5, 0, 'com.admin.controller.AuthController.login()', 'POST', 'admin', '', '/api/auth/login', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:13:50');
INSERT INTO `sys_oper_log` VALUES (33, '首页', 4, 1, '首页', 'GET', 'admin', '', '/home', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:13:50');
INSERT INTO `sys_oper_log` VALUES (34, '用户管理', 4, 1, '用户管理', 'GET', 'admin', '', '/system/user', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:14:14');
INSERT INTO `sys_oper_log` VALUES (35, '角色管理', 4, 1, '角色管理', 'GET', 'admin', '', '/system/role', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:14:14');
INSERT INTO `sys_oper_log` VALUES (36, '菜单管理', 4, 1, '菜单管理', 'GET', 'admin', '', '/system/menu', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:14:15');
INSERT INTO `sys_oper_log` VALUES (37, '部门管理', 4, 1, '部门管理', 'GET', 'admin', '', '/system/dept', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:14:17');
INSERT INTO `sys_oper_log` VALUES (38, '用户管理', 4, 1, '用户管理', 'GET', 'admin', '', '/system/user', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:14:19');
INSERT INTO `sys_oper_log` VALUES (39, '菜单管理', 4, 1, '菜单管理', 'GET', 'admin', '', '/system/menu', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:14:21');
INSERT INTO `sys_oper_log` VALUES (40, '操作日志', 4, 1, '操作日志', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-14 22:14:36');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色标识',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', '2026-06-30 15:06:14', '2026-07-01 17:39:30');
INSERT INTO `sys_role` VALUES (2, '普通用户', 'user', '2026-07-01 10:56:15', '2026-07-01 17:39:30');

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint(20) NOT NULL,
  `menu_id` bigint(20) NOT NULL,
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1, 1);
INSERT INTO `sys_role_menu` VALUES (1, 2);
INSERT INTO `sys_role_menu` VALUES (1, 3);
INSERT INTO `sys_role_menu` VALUES (1, 4);
INSERT INTO `sys_role_menu` VALUES (1, 5);
INSERT INTO `sys_role_menu` VALUES (1, 6);
INSERT INTO `sys_role_menu` VALUES (1, 7);
INSERT INTO `sys_role_menu` VALUES (1, 8);
INSERT INTO `sys_role_menu` VALUES (1, 9);
INSERT INTO `sys_role_menu` VALUES (1, 10);
INSERT INTO `sys_role_menu` VALUES (1, 11);
INSERT INTO `sys_role_menu` VALUES (1, 12);
INSERT INTO `sys_role_menu` VALUES (1, 13);
INSERT INTO `sys_role_menu` VALUES (1, 14);
INSERT INTO `sys_role_menu` VALUES (1, 15);
INSERT INTO `sys_role_menu` VALUES (1, 16);
INSERT INTO `sys_role_menu` VALUES (1, 17);
INSERT INTO `sys_role_menu` VALUES (1, 19);
INSERT INTO `sys_role_menu` VALUES (1, 20);
INSERT INTO `sys_role_menu` VALUES (1, 21);
INSERT INTO `sys_role_menu` VALUES (1, 22);
INSERT INTO `sys_role_menu` VALUES (1, 23);
INSERT INTO `sys_role_menu` VALUES (1, 24);
INSERT INTO `sys_role_menu` VALUES (1, 25);
INSERT INTO `sys_role_menu` VALUES (1, 26);
INSERT INTO `sys_role_menu` VALUES (1, 28);
INSERT INTO `sys_role_menu` VALUES (1, 29);
INSERT INTO `sys_role_menu` VALUES (1, 30);
INSERT INTO `sys_role_menu` VALUES (1, 31);
INSERT INTO `sys_role_menu` VALUES (1, 32);
INSERT INTO `sys_role_menu` VALUES (1, 34);
INSERT INTO `sys_role_menu` VALUES (1, 35);
INSERT INTO `sys_role_menu` VALUES (1, 36);
INSERT INTO `sys_role_menu` VALUES (1, 37);
INSERT INTO `sys_role_menu` VALUES (1, 38);
INSERT INTO `sys_role_menu` VALUES (1, 39);
INSERT INTO `sys_role_menu` VALUES (1, 40);
INSERT INTO `sys_role_menu` VALUES (1, 41);
INSERT INTO `sys_role_menu` VALUES (1, 42);
INSERT INTO `sys_role_menu` VALUES (1, 43);
INSERT INTO `sys_role_menu` VALUES (1, 44);
INSERT INTO `sys_role_menu` VALUES (1, 45);
INSERT INTO `sys_role_menu` VALUES (1, 46);
INSERT INTO `sys_role_menu` VALUES (1, 47);
INSERT INTO `sys_role_menu` VALUES (1, 48);
INSERT INTO `sys_role_menu` VALUES (1, 49);
INSERT INTO `sys_role_menu` VALUES (1, 50);
INSERT INTO `sys_role_menu` VALUES (1, 51);
INSERT INTO `sys_role_menu` VALUES (1, 52);
INSERT INTO `sys_role_menu` VALUES (1, 53);
INSERT INTO `sys_role_menu` VALUES (1, 54);
INSERT INTO `sys_role_menu` VALUES (1, 55);
INSERT INTO `sys_role_menu` VALUES (2, 3);
INSERT INTO `sys_role_menu` VALUES (2, 4);
INSERT INTO `sys_role_menu` VALUES (2, 6);
INSERT INTO `sys_role_menu` VALUES (2, 9);
INSERT INTO `sys_role_menu` VALUES (2, 10);
INSERT INTO `sys_role_menu` VALUES (2, 11);
INSERT INTO `sys_role_menu` VALUES (2, 12);
INSERT INTO `sys_role_menu` VALUES (2, 13);
INSERT INTO `sys_role_menu` VALUES (2, 14);
INSERT INTO `sys_role_menu` VALUES (2, 19);
INSERT INTO `sys_role_menu` VALUES (2, 20);
INSERT INTO `sys_role_menu` VALUES (2, 21);
INSERT INTO `sys_role_menu` VALUES (2, 22);
INSERT INTO `sys_role_menu` VALUES (2, 23);
INSERT INTO `sys_role_menu` VALUES (2, 24);
INSERT INTO `sys_role_menu` VALUES (2, 28);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `dept_id` bigint(20) NULL DEFAULT NULL COMMENT '部门ID',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态 0禁用 1正常',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$FfA.CcDrNcWXPCQvt6q2ZOV8rRJzPuO.263EiFQTEspS5Rp7G7nmW', '超级管理员', 'adminx1@63.com', NULL, 1, '2026-06-30 15:06:14', '2026-07-06 22:15:45', '/uploads/184e72be4cf34d64bb90995f86846321.png');
INSERT INTO `sys_user` VALUES (2, 'user', '$2a$10$ZlPLGJ8Q0BfN0dIa.yh8SOha/IfXZPuegSs3aC.Fs/7ABskNComfC', '普通用户', NULL, NULL, 1, '2026-07-01 11:11:05', '2026-07-01 17:39:30', NULL);
INSERT INTO `sys_user` VALUES (3, 'aaa', '$2a$10$MjHUAa7oWfSOIyigElHqS.OCw7SS3ACUSPQcZDGSdmy0GCAU6DOGe', '西AOA', NULL, NULL, 1, '2026-07-01 11:37:43', '2026-07-02 21:44:23', '/uploads/eaa52e7b8fcd42a29b566fecc6989108.jpg');
INSERT INTO `sys_user` VALUES (4, 'ces', '$2a$10$/N.RDCBEuB/FOoMw82C07er5wWp0ttDBkzihrhYJIApWIrX7qUCB6', '测试', NULL, NULL, 1, '2026-07-06 22:17:24', '2026-07-14 00:02:54', NULL);
INSERT INTO `sys_user` VALUES (5, 'acc', '$2a$10$.TxIHv/8OLyCh5Q/E.URe./.slAnunnZIO7B8FUqZ2wcEiOIoVAI2', '测试用户', NULL, NULL, 1, '2026-07-14 22:06:01', '2026-07-14 22:06:01', NULL);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2);
INSERT INTO `sys_user_role` VALUES (3, 2);
INSERT INTO `sys_user_role` VALUES (4, 2);
INSERT INTO `sys_user_role` VALUES (5, 2);

SET FOREIGN_KEY_CHECKS = 1;
