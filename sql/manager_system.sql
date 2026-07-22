/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50737 (5.7.37-log)
 Source Host           : localhost:3306
 Source Schema         : manager_system

 Target Server Type    : MySQL
 Target Server Version : 50737 (5.7.37-log)
 File Encoding         : 65001

 Date: 22/07/2026 13:02:28
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
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_table_name`(`table_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码生成表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gen_table
-- ----------------------------
INSERT INTO `gen_table` VALUES (1, 'iot_device', 'IoT设备表', 'IotDevice', 'com.admin', 'iot', 'device', 'IoT设备表', 'admin', '', '2026-07-21 18:16:02');

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
  `last_online_time` datetime NULL DEFAULT NULL COMMENT '最后上线时间',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_device_key`(`device_key`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'IoT设备表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of iot_device
-- ----------------------------
INSERT INTO `iot_device` VALUES (1, '1号温度传感器', 'SN-TH-001', 'abc123', 1, 1, '2026-07-21 17:15:14', '安装在A车间', '2026-07-21 17:15:14', '2026-07-21 17:15:14');
INSERT INTO `iot_device` VALUES (2, '2号温度传感器', 'SN-TH-002', 'abc456', 1, 2, '2026-07-21 15:15:14', '安装在B车间', '2026-07-21 17:15:14', '2026-07-21 17:15:14');
INSERT INTO `iot_device` VALUES (3, '1号电表', 'SN-MT-001', 'def789', 2, 1, '2026-07-21 17:15:14', '主配电柜', '2026-07-21 17:15:14', '2026-07-21 17:15:14');
INSERT INTO `iot_device` VALUES (4, '边缘网关#1', 'SN-GW-001', 'ghj012', 3, 1, '2026-07-21 17:15:14', '厂区东侧', '2026-07-21 17:15:14', '2026-07-21 17:15:14');

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
  `report_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_device_time`(`device_id`, `report_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'IoT设备数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of iot_device_data
-- ----------------------------
INSERT INTO `iot_device_data` VALUES (1, 1, 'temperature', '26.5', 'float', '2026-07-21 17:15:14', '2026-07-21 17:15:14');
INSERT INTO `iot_device_data` VALUES (2, 1, 'humidity', '58.3', 'float', '2026-07-21 17:15:14', '2026-07-21 17:15:14');
INSERT INTO `iot_device_data` VALUES (3, 2, 'temperature', '31.2', 'float', '2026-07-21 16:15:14', '2026-07-21 16:15:14');
INSERT INTO `iot_device_data` VALUES (4, 3, 'voltage', '220.5', 'float', '2026-07-21 17:15:14', '2026-07-21 17:15:14');
INSERT INTO `iot_device_data` VALUES (5, 3, 'current', '15.8', 'float', '2026-07-21 17:15:14', '2026-07-21 17:15:14');
INSERT INTO `iot_device_data` VALUES (6, 3, 'power', '3.48', 'float', '2026-07-21 17:15:14', '2026-07-21 17:15:14');

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
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_product_key`(`product_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'IoT产品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of iot_product
-- ----------------------------
INSERT INTO `iot_product` VALUES (1, '温湿度传感器', 'temp_humidity_v1', '工业级温湿度采集设备', 'sensor', 'mqtt', 'json', 1, '2026-07-21 17:15:14', '2026-07-21 17:15:14');
INSERT INTO `iot_product` VALUES (2, '智能电表', 'smart_meter_v1', '三相智能电表，支持远程抄表', 'sensor', 'http', 'json', 1, '2026-07-21 17:15:14', '2026-07-21 17:15:14');
INSERT INTO `iot_product` VALUES (3, '智能网关', 'gateway_v1', '边缘计算网关，支持多协议接入', 'gateway', 'tcp', 'custom', 1, '2026-07-21 17:15:14', '2026-07-21 17:15:14');

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
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

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
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
  `exec_time` datetime NULL DEFAULT NULL COMMENT '执行时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
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
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `always_show` tinyint(1) NULL DEFAULT 1 COMMENT '是否总是显示：1=总是，0=不是；当为不是且只有一个子菜单时，折叠显示子菜单',
  `visible` tinyint(1) NULL DEFAULT 1 COMMENT '是否显示：1=显示，0=隐藏',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '菜单状态：1=启用，0=禁用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

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
INSERT INTO `sys_menu` VALUES (39, 0, 'IoT物联网', '/iot', 'Layout', '', 0, 'Cpu', 10, '2026-07-21 17:15:14', '2026-07-21 17:15:14', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (40, 39, '产品管理', '/iot/product', 'iot/product/index', 'iot:product:list', 1, 'Goods', 1, '2026-07-21 17:15:14', '2026-07-21 17:15:14', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (41, 39, '设备管理', '/iot/device', 'iot/device/index', 'iot:device:list', 1, 'Monitor', 2, '2026-07-21 17:15:14', '2026-07-21 17:15:14', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (42, 40, '产品新增', '', '', 'iot:product:add', 2, '', 1, '2026-07-21 17:15:14', '2026-07-21 17:15:14', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (43, 40, '产品编辑', '', '', 'iot:product:edit', 2, '', 2, '2026-07-21 17:15:14', '2026-07-21 17:15:14', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (44, 40, '产品删除', '', '', 'iot:product:remove', 2, '', 3, '2026-07-21 17:15:14', '2026-07-21 17:15:14', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (45, 41, '设备新增', '', '', 'iot:device:add', 2, '', 1, '2026-07-21 17:15:14', '2026-07-21 17:15:14', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (46, 41, '设备编辑', '', '', 'iot:device:edit', 2, '', 2, '2026-07-21 17:15:14', '2026-07-21 17:15:14', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (47, 41, '设备删除', '', '', 'iot:device:remove', 2, '', 3, '2026-07-21 17:15:14', '2026-07-21 17:15:14', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (48, 0, '开发工具', '/tool', 'Layout', '', 0, 'Tools', 20, '2026-07-21 17:15:14', '2026-07-21 17:15:14', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (49, 48, '代码生成', '/tool/gen', 'tool/gen/index', 'tool:gen:list', 1, 'EditPen', 1, '2026-07-21 17:15:14', '2026-07-21 17:15:14', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (50, 49, '生成代码', '', '', 'tool:gen:code', 2, '', 1, '2026-07-21 17:15:14', '2026-07-21 17:15:14', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (51, 32, '定时任务', '/job', 'monitor/job/index', 'monitor:job:list', 1, 'Clock', 2, '2026-07-21 17:15:14', '2026-07-21 17:15:14', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (52, 32, '调度日志', '/job/log', 'monitor/job/log', 'monitor:job:log', 1, 'Tickets', 3, '2026-07-21 17:15:14', '2026-07-21 17:15:14', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (53, 51, '任务新增', '', '', 'monitor:job:add', 2, '', 1, '2026-07-21 17:15:14', '2026-07-21 17:15:14', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (54, 51, '任务编辑', '', '', 'monitor:job:edit', 2, '', 2, '2026-07-21 17:15:14', '2026-07-21 17:15:14', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (55, 51, '任务删除', '', '', 'monitor:job:remove', 2, '', 3, '2026-07-21 17:15:14', '2026-07-21 17:15:14', 1, 1, 1);

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
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0=正常, 1=已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receiver_read`(`receiver_id`, `read_status`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消息通知表' ROW_FORMAT = DYNAMIC;

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
  `oper_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_oper_name`(`oper_name`) USING BTREE,
  INDEX `idx_business_type`(`business_type`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_oper_time`(`oper_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 188 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志记录' ROW_FORMAT = DYNAMIC;

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
INSERT INTO `sys_oper_log` VALUES (41, '接口请求失败', 0, 1, '', 'GET', '', '', '/auth/captcha', '127.0.0.1', '内网IP', '', '{\"code\":500,\"msg\":\"系统异常\",\"data\":null}', 1, '系统异常', '2026-07-21 17:16:31');
INSERT INTO `sys_oper_log` VALUES (42, '接口请求失败', 0, 1, '', 'GET', '', '', '/auth/captcha', '127.0.0.1', '内网IP', '', '{\"code\":500,\"msg\":\"系统异常\",\"data\":null}', 1, '系统异常', '2026-07-21 17:16:33');
INSERT INTO `sys_oper_log` VALUES (43, '用户登录', 5, 0, 'com.admin.controller.AuthController.login()', 'POST', 'admin', '', '/api/auth/login', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:17:28');
INSERT INTO `sys_oper_log` VALUES (44, '首页', 4, 1, '首页', 'GET', 'admin', '', '/home', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:17:32');
INSERT INTO `sys_oper_log` VALUES (45, '首页', 4, 1, '首页', 'GET', 'admin', '', '/home', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:17:32');
INSERT INTO `sys_oper_log` VALUES (46, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:17:37');
INSERT INTO `sys_oper_log` VALUES (47, '设备管理', 4, 1, '设备管理', 'GET', 'admin', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:17:49');
INSERT INTO `sys_oper_log` VALUES (48, '代码生成', 4, 1, '代码生成', 'GET', 'admin', '', '/tool/gen', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:17:54');
INSERT INTO `sys_oper_log` VALUES (49, '操作日志', 4, 1, '操作日志', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:18:36');
INSERT INTO `sys_oper_log` VALUES (50, '定时任务', 4, 1, '定时任务', 'GET', 'admin', '', '/monitor/job', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:18:38');
INSERT INTO `sys_oper_log` VALUES (51, '调度日志', 4, 1, '调度日志', 'GET', 'admin', '', '/monitor/job/log', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:18:41');
INSERT INTO `sys_oper_log` VALUES (52, '调度日志', 4, 1, '调度日志', 'GET', 'admin', '', '/monitor/job/log', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:18:56');
INSERT INTO `sys_oper_log` VALUES (53, '个人中心', 4, 1, '个人中心', 'GET', 'admin', '', '/system/user/profile', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:19:10');
INSERT INTO `sys_oper_log` VALUES (54, '操作日志', 4, 1, '操作日志', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:19:27');
INSERT INTO `sys_oper_log` VALUES (55, '操作日志', 4, 1, '操作日志', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:20:35');
INSERT INTO `sys_oper_log` VALUES (56, '操作日志', 4, 1, '操作日志', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:21:39');
INSERT INTO `sys_oper_log` VALUES (57, '个人中心', 4, 1, '个人中心', 'GET', 'admin', '', '/system/user/profile', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:21:47');
INSERT INTO `sys_oper_log` VALUES (58, '定时任务', 4, 1, '定时任务', 'GET', 'admin', '', '/monitor/job', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:22:22');
INSERT INTO `sys_oper_log` VALUES (59, '代码生成', 4, 1, '代码生成', 'GET', 'admin', '', '/tool/gen', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:22:42');
INSERT INTO `sys_oper_log` VALUES (60, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:22:44');
INSERT INTO `sys_oper_log` VALUES (61, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:23:14');
INSERT INTO `sys_oper_log` VALUES (62, '设备管理', 4, 1, '设备管理', 'GET', 'admin', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:23:16');
INSERT INTO `sys_oper_log` VALUES (63, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:23:17');
INSERT INTO `sys_oper_log` VALUES (64, '代码生成', 4, 1, '代码生成', 'GET', 'admin', '', '/tool/gen', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:23:18');
INSERT INTO `sys_oper_log` VALUES (65, '操作日志', 4, 1, '操作日志', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:23:19');
INSERT INTO `sys_oper_log` VALUES (66, '定时任务', 4, 1, '定时任务', 'GET', 'admin', '', '/monitor/job', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:23:20');
INSERT INTO `sys_oper_log` VALUES (67, '调度日志', 4, 1, '调度日志', 'GET', 'admin', '', '/monitor/job/log', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:23:21');
INSERT INTO `sys_oper_log` VALUES (68, '操作日志', 4, 1, '操作日志', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:23:22');
INSERT INTO `sys_oper_log` VALUES (69, '定时任务', 4, 1, '定时任务', 'GET', 'admin', '', '/monitor/job', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:23:24');
INSERT INTO `sys_oper_log` VALUES (70, '调度日志', 4, 1, '调度日志', 'GET', 'admin', '', '/monitor/job/log', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:23:26');
INSERT INTO `sys_oper_log` VALUES (71, '操作日志', 4, 1, '操作日志', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:23:27');
INSERT INTO `sys_oper_log` VALUES (72, '操作日志', 4, 1, '操作日志', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:25:02');
INSERT INTO `sys_oper_log` VALUES (73, '操作日志', 4, 1, '操作日志', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:25:10');
INSERT INTO `sys_oper_log` VALUES (74, '操作日志', 4, 1, '操作日志', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:25:11');
INSERT INTO `sys_oper_log` VALUES (75, '操作日志', 4, 1, '操作日志', 'GET', 'admin', '', '/monitor/operlog', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:25:40');
INSERT INTO `sys_oper_log` VALUES (76, '用户管理', 4, 1, '用户管理', 'GET', 'admin', '', '/system/user', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:25:46');
INSERT INTO `sys_oper_log` VALUES (77, '角色管理', 4, 1, '角色管理', 'GET', 'admin', '', '/system/role', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:29:26');
INSERT INTO `sys_oper_log` VALUES (78, '用户管理', 4, 1, '用户管理', 'GET', 'admin', '', '/system/user', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:29:27');
INSERT INTO `sys_oper_log` VALUES (79, '定时任务', 4, 1, '定时任务', 'GET', 'admin', '', '/monitor/job', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:29:35');
INSERT INTO `sys_oper_log` VALUES (80, '调度日志', 4, 1, '调度日志', 'GET', 'admin', '', '/monitor/job/log', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:29:40');
INSERT INTO `sys_oper_log` VALUES (81, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:29:43');
INSERT INTO `sys_oper_log` VALUES (82, '设备管理', 4, 1, '设备管理', 'GET', 'admin', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:29:46');
INSERT INTO `sys_oper_log` VALUES (83, '接口请求失败', 0, 1, '', 'GET', 'admin', '', '/iot/device/1/data', '127.0.0.1', '内网IP', '', '{\"code\":500,\"msg\":\"系统异常\",\"data\":null}', 1, '系统异常', '2026-07-21 17:29:47');
INSERT INTO `sys_oper_log` VALUES (84, '接口请求失败', 0, 1, '', 'GET', 'admin', '', '/iot/device/1/data', '127.0.0.1', '内网IP', '', '{\"code\":500,\"msg\":\"系统异常\",\"data\":null}', 1, '系统异常', '2026-07-21 17:29:58');
INSERT INTO `sys_oper_log` VALUES (85, '设备管理', 4, 1, '设备管理', 'GET', 'admin', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:32:04');
INSERT INTO `sys_oper_log` VALUES (86, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:33:14');
INSERT INTO `sys_oper_log` VALUES (87, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:33:18');
INSERT INTO `sys_oper_log` VALUES (88, '用户退出', 6, 0, 'com.admin.controller.AuthController.logout()', 'POST', '匿名', '', '/api/auth/logout', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:33:22');
INSERT INTO `sys_oper_log` VALUES (89, '用户登录', 5, 0, 'com.admin.controller.AuthController.login()', 'POST', 'aaa', '', '/api/auth/login', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:33:37');
INSERT INTO `sys_oper_log` VALUES (90, '首页', 4, 1, '首页', 'GET', 'aaa', '', '/home', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:33:37');
INSERT INTO `sys_oper_log` VALUES (91, '菜单管理', 4, 1, '菜单管理', 'GET', 'aaa', '', '/system/menu', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:33:42');
INSERT INTO `sys_oper_log` VALUES (92, '角色管理', 4, 1, '角色管理', 'GET', 'aaa', '', '/system/role', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:33:43');
INSERT INTO `sys_oper_log` VALUES (93, '角色管理', 4, 1, '角色管理', 'GET', 'aaa', '', '/system/role', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:34:17');
INSERT INTO `sys_oper_log` VALUES (94, '用户退出', 6, 0, 'com.admin.controller.AuthController.logout()', 'POST', '匿名', '', '/api/auth/logout', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:34:18');
INSERT INTO `sys_oper_log` VALUES (95, '用户登录', 5, 0, 'com.admin.controller.AuthController.login()', 'POST', 'admin', '', '/api/auth/login', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:34:29');
INSERT INTO `sys_oper_log` VALUES (96, '首页', 4, 1, '首页', 'GET', 'admin', '', '/home', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:34:29');
INSERT INTO `sys_oper_log` VALUES (97, '角色管理', 4, 1, '角色管理', 'GET', 'admin', '', '/system/role', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:34:34');
INSERT INTO `sys_oper_log` VALUES (98, '用户退出', 6, 0, 'com.admin.controller.AuthController.logout()', 'POST', '匿名', '', '/api/auth/logout', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:35:23');
INSERT INTO `sys_oper_log` VALUES (99, '用户登录', 5, 0, 'com.admin.controller.AuthController.login()', 'POST', 'aaa', '', '/api/auth/login', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:35:30');
INSERT INTO `sys_oper_log` VALUES (100, '首页', 4, 1, '首页', 'GET', 'aaa', '', '/home', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:35:30');
INSERT INTO `sys_oper_log` VALUES (101, '产品管理', 4, 1, '产品管理', 'GET', 'aaa', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:35:34');
INSERT INTO `sys_oper_log` VALUES (102, '用户退出', 6, 0, 'com.admin.controller.AuthController.logout()', 'POST', '匿名', '', '/api/auth/logout', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:35:39');
INSERT INTO `sys_oper_log` VALUES (103, '用户登录', 5, 0, 'com.admin.controller.AuthController.login()', 'POST', 'admin', '', '/api/auth/login', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:35:51');
INSERT INTO `sys_oper_log` VALUES (104, '首页', 4, 1, '首页', 'GET', 'admin', '', '/home', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:35:51');
INSERT INTO `sys_oper_log` VALUES (105, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:35:54');
INSERT INTO `sys_oper_log` VALUES (106, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:36:35');
INSERT INTO `sys_oper_log` VALUES (107, '用户管理', 4, 1, '用户管理', 'GET', 'admin', '', '/system/user', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:36:39');
INSERT INTO `sys_oper_log` VALUES (108, '角色管理', 4, 1, '角色管理', 'GET', 'admin', '', '/system/role', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:36:40');
INSERT INTO `sys_oper_log` VALUES (109, '角色管理', 4, 1, '角色管理', 'GET', 'admin', '', '/system/role', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:36:51');
INSERT INTO `sys_oper_log` VALUES (110, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:36:53');
INSERT INTO `sys_oper_log` VALUES (111, '设备管理', 4, 1, '设备管理', 'GET', 'admin', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:36:54');
INSERT INTO `sys_oper_log` VALUES (112, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:36:55');
INSERT INTO `sys_oper_log` VALUES (113, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:37:15');
INSERT INTO `sys_oper_log` VALUES (114, '接口请求失败', 0, 1, '', 'GET', 'admin', '', '/system/menu/user-permissions', '127.0.0.1', '内网IP', '', '{\"code\":500,\"msg\":\"系统异常\",\"data\":null}', 1, '系统异常', '2026-07-21 17:37:28');
INSERT INTO `sys_oper_log` VALUES (115, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:37:29');
INSERT INTO `sys_oper_log` VALUES (116, '接口请求失败', 0, 1, '', 'GET', 'admin', '', '/system/menu/user-permissions', '127.0.0.1', '内网IP', '', '{\"code\":500,\"msg\":\"系统异常\",\"data\":null}', 1, '系统异常', '2026-07-21 17:37:45');
INSERT INTO `sys_oper_log` VALUES (117, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:37:45');
INSERT INTO `sys_oper_log` VALUES (118, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:38:09');
INSERT INTO `sys_oper_log` VALUES (119, '用户退出', 6, 0, 'com.admin.controller.AuthController.logout()', 'POST', '匿名', '', '/api/auth/logout', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:38:12');
INSERT INTO `sys_oper_log` VALUES (120, '用户登录', 5, 0, 'com.admin.controller.AuthController.login()', 'POST', 'aaa', '', '/api/auth/login', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:38:20');
INSERT INTO `sys_oper_log` VALUES (121, '首页', 4, 1, '首页', 'GET', 'aaa', '', '/home', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:38:20');
INSERT INTO `sys_oper_log` VALUES (122, '产品管理', 4, 1, '产品管理', 'GET', 'aaa', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:38:23');
INSERT INTO `sys_oper_log` VALUES (123, '设备管理', 4, 1, '设备管理', 'GET', 'aaa', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:38:25');
INSERT INTO `sys_oper_log` VALUES (124, '用户退出', 6, 0, 'com.admin.controller.AuthController.logout()', 'POST', '匿名', '', '/api/auth/logout', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:38:28');
INSERT INTO `sys_oper_log` VALUES (125, '用户登录', 5, 0, 'com.admin.controller.AuthController.login()', 'POST', 'admin', '', '/api/auth/login', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:38:38');
INSERT INTO `sys_oper_log` VALUES (126, '首页', 4, 1, '首页', 'GET', 'admin', '', '/home', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:38:38');
INSERT INTO `sys_oper_log` VALUES (127, '设备管理', 4, 1, '设备管理', 'GET', 'admin', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:38:44');
INSERT INTO `sys_oper_log` VALUES (128, '消息通知', 4, 1, '消息通知', 'GET', 'admin', '', '/message/notify', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:38:46');
INSERT INTO `sys_oper_log` VALUES (129, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:38:48');
INSERT INTO `sys_oper_log` VALUES (130, '机房监控大屏', 4, 1, '机房监控大屏', 'GET', 'admin', '', '/dashboard/dashboard', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:38:50');
INSERT INTO `sys_oper_log` VALUES (131, '全球航运大屏', 4, 1, '全球航运大屏', 'GET', 'admin', '', '/dashboard/shipping', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:38:52');
INSERT INTO `sys_oper_log` VALUES (132, '全球航运大屏', 4, 1, '全球航运大屏', 'GET', 'admin', '', '/dashboard/shipping', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:42:42');
INSERT INTO `sys_oper_log` VALUES (133, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:42:59');
INSERT INTO `sys_oper_log` VALUES (134, '设备管理', 4, 1, '设备管理', 'GET', 'admin', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:43:01');
INSERT INTO `sys_oper_log` VALUES (135, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:43:02');
INSERT INTO `sys_oper_log` VALUES (136, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:43:36');
INSERT INTO `sys_oper_log` VALUES (137, '全球航运大屏', 4, 1, '全球航运大屏', 'GET', 'admin', '', '/dashboard/shipping', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:43:49');
INSERT INTO `sys_oper_log` VALUES (138, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:55:14');
INSERT INTO `sys_oper_log` VALUES (139, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:55:17');
INSERT INTO `sys_oper_log` VALUES (140, '设备管理', 4, 1, '设备管理', 'GET', 'admin', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:55:22');
INSERT INTO `sys_oper_log` VALUES (141, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:56:45');
INSERT INTO `sys_oper_log` VALUES (142, '设备管理', 4, 1, '设备管理', 'GET', 'admin', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 17:56:49');
INSERT INTO `sys_oper_log` VALUES (143, '设备管理', 4, 1, '设备管理', 'GET', 'admin', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:12:21');
INSERT INTO `sys_oper_log` VALUES (144, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:12:23');
INSERT INTO `sys_oper_log` VALUES (145, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:12:26');
INSERT INTO `sys_oper_log` VALUES (146, '设备管理', 4, 1, '设备管理', 'GET', 'admin', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:13:06');
INSERT INTO `sys_oper_log` VALUES (147, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:14:35');
INSERT INTO `sys_oper_log` VALUES (148, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:14:37');
INSERT INTO `sys_oper_log` VALUES (149, '设备管理', 4, 1, '设备管理', 'GET', 'admin', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:14:41');
INSERT INTO `sys_oper_log` VALUES (150, '设备管理', 4, 1, '设备管理', 'GET', 'admin', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:15:25');
INSERT INTO `sys_oper_log` VALUES (151, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:15:46');
INSERT INTO `sys_oper_log` VALUES (152, '首页', 4, 1, '首页', 'GET', 'admin', '', '/home', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:15:53');
INSERT INTO `sys_oper_log` VALUES (153, '代码生成', 4, 1, '代码生成', 'GET', 'admin', '', '/tool/gen', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:15:55');
INSERT INTO `sys_oper_log` VALUES (154, '接口请求失败', 0, 1, '', 'GET', 'admin', '', '/tool/gen/download/1', '127.0.0.1', '内网IP', '', '{}', 1, '业务处理失败', '2026-07-21 18:16:13');
INSERT INTO `sys_oper_log` VALUES (155, '接口请求失败', 0, 1, '', 'GET', 'admin', '', '/tool/gen/download/1', '127.0.0.1', '内网IP', '', '{}', 1, '业务处理失败', '2026-07-21 18:16:20');
INSERT INTO `sys_oper_log` VALUES (156, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:16:45');
INSERT INTO `sys_oper_log` VALUES (157, '设备管理', 4, 1, '设备管理', 'GET', 'admin', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:16:47');
INSERT INTO `sys_oper_log` VALUES (158, '设备管理', 4, 1, '设备管理', 'GET', 'admin', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:17:10');
INSERT INTO `sys_oper_log` VALUES (159, '设备管理', 4, 1, '设备管理', 'GET', 'admin', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:17:29');
INSERT INTO `sys_oper_log` VALUES (160, '代码生成', 4, 1, '代码生成', 'GET', 'admin', '', '/tool/gen', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:17:34');
INSERT INTO `sys_oper_log` VALUES (161, '设备管理', 4, 1, '设备管理', 'GET', 'admin', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:20:11');
INSERT INTO `sys_oper_log` VALUES (162, '产品管理', 4, 1, '产品管理', 'GET', 'admin', '', '/iot/product', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:20:16');
INSERT INTO `sys_oper_log` VALUES (163, '设备管理', 4, 1, '设备管理', 'GET', 'admin', '', '/iot/device', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-21 18:20:22');
INSERT INTO `sys_oper_log` VALUES (164, '用户登录', 5, 0, 'com.admin.controller.AuthController.login()', 'POST', 'admin', '', '/api/auth/login', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-22 09:50:49');
INSERT INTO `sys_oper_log` VALUES (165, '首页', 4, 1, '首页', 'GET', 'admin', '', '/home', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-22 09:50:50');
INSERT INTO `sys_oper_log` VALUES (166, '全球航运大屏', 4, 1, '全球航运大屏', 'GET', 'admin', '', '/dashboard/shipping', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-22 09:50:57');
INSERT INTO `sys_oper_log` VALUES (167, '首页', 4, 1, '首页', 'GET', 'admin', '', '/home', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-22 11:11:30');
INSERT INTO `sys_oper_log` VALUES (168, '首页', 4, 1, '首页', 'GET', 'admin', '', '/home', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-22 11:11:38');
INSERT INTO `sys_oper_log` VALUES (169, '用户退出', 6, 0, 'com.admin.controller.AuthController.logout()', 'POST', '匿名', '', '/api/auth/logout', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-22 11:11:40');
INSERT INTO `sys_oper_log` VALUES (170, '用户登录', 5, 0, 'com.admin.controller.AuthController.login()', 'POST', 'admin', '', '/api/auth/login', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-22 11:12:47');
INSERT INTO `sys_oper_log` VALUES (171, '首页', 4, 1, '首页', 'GET', 'admin', '', '/home', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-22 11:12:47');
INSERT INTO `sys_oper_log` VALUES (172, '用户管理', 4, 1, '用户管理', 'GET', 'admin', '', '/system/user', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-22 11:12:52');
INSERT INTO `sys_oper_log` VALUES (173, '接口请求失败', 0, 1, '', 'GET', 'admin', '', '/system/user/page', '127.0.0.1', '内网IP', '', '{\"code\":500,\"msg\":\"系统异常\",\"data\":null}', 1, '系统异常', '2026-07-22 11:12:52');
INSERT INTO `sys_oper_log` VALUES (174, '用户管理', 4, 1, '用户管理', 'GET', 'admin', '', '/system/user', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-22 11:12:54');
INSERT INTO `sys_oper_log` VALUES (175, '接口请求失败', 0, 1, '', 'GET', 'admin', '', '/system/user/page', '127.0.0.1', '内网IP', '', '{\"code\":500,\"msg\":\"系统异常\",\"data\":null}', 1, '系统异常', '2026-07-22 11:12:54');
INSERT INTO `sys_oper_log` VALUES (176, '用户管理', 4, 1, '用户管理', 'GET', 'admin', '', '/system/user', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-22 11:14:10');
INSERT INTO `sys_oper_log` VALUES (177, '用户管理', 4, 1, '用户管理', 'GET', 'admin', '', '/system/user', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-22 11:16:15');
INSERT INTO `sys_oper_log` VALUES (178, '用户管理', 2, 0, 'com.admin.controller.UserController.update()', 'PUT', 'admin', '', '/api/system/user', '127.0.0.1', '内网IP', 'Body: {\"id\":5,\"username\":\"acc\",\"password\":\"******\",\"nickname\":\"测试用户\",\"email\":null,\"deptId\":null,\"avatar\":null,\"phone\":\"15566778989\",\"status\":1,\"createTime\":\"2026-07-14 22:06:01\",\"updateTime\":\"2026-07-14 22:06:01\",\"roleIds\":[2]}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2026-07-22 11:16:41');
INSERT INTO `sys_oper_log` VALUES (179, '用户管理', 2, 0, 'com.admin.controller.UserController.update()', 'PUT', 'admin', '', '/api/system/user', '127.0.0.1', '内网IP', 'Body: {\"id\":4,\"username\":\"ces\",\"password\":\"******\",\"nickname\":\"测试\",\"email\":null,\"deptId\":null,\"avatar\":null,\"phone\":\"17766558899\",\"status\":1,\"createTime\":\"2026-07-06 22:17:24\",\"updateTime\":\"2026-07-14 00:02:54\",\"roleIds\":[2]}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2026-07-22 11:16:51');
INSERT INTO `sys_oper_log` VALUES (180, '用户管理', 2, 0, 'com.admin.controller.UserController.update()', 'PUT', 'admin', '', '/api/system/user', '127.0.0.1', '内网IP', 'Body: {\"id\":3,\"username\":\"aaa\",\"password\":\"******\",\"nickname\":\"西AOA\",\"email\":null,\"deptId\":null,\"avatar\":\"/uploads/eaa52e7b8fcd42a29b566fecc6989108.jpg\",\"phone\":\"16677889900\",\"status\":1,\"createTime\":\"2026-07-01 11:37:43\",\"updateTime\":\"2026-07-02 21:44:23\",\"roleIds\":[2]}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2026-07-22 11:16:57');
INSERT INTO `sys_oper_log` VALUES (181, '用户管理', 2, 0, 'com.admin.controller.UserController.update()', 'PUT', 'admin', '', '/api/system/user', '127.0.0.1', '内网IP', 'Body: {\"id\":2,\"username\":\"user\",\"password\":\"******\",\"nickname\":\"普通用户\",\"email\":null,\"deptId\":null,\"avatar\":null,\"phone\":\"13344556677\",\"status\":1,\"createTime\":\"2026-07-01 11:11:05\",\"updateTime\":\"2026-07-01 17:39:30\",\"roleIds\":[2]}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2026-07-22 11:17:03');
INSERT INTO `sys_oper_log` VALUES (182, '用户管理', 2, 0, 'com.admin.controller.UserController.update()', 'PUT', 'admin', '', '/api/system/user', '127.0.0.1', '内网IP', 'Body: {\"id\":1,\"username\":\"admin\",\"password\":\"******\",\"nickname\":\"超级管理员\",\"email\":\"adminx1@63.com\",\"deptId\":null,\"avatar\":\"/uploads/88664664b967473dba49ae6518b775fb.jpg\",\"phone\":\"17788990011\",\"status\":1,\"createTime\":\"2026-06-30 15:06:14\",\"updateTime\":\"2026-07-21 17:22:13\",\"roleIds\":[1]}', '{\"code\":200,\"msg\":\"操作成功\",\"data\":null}', 0, '', '2026-07-22 11:17:17');
INSERT INTO `sys_oper_log` VALUES (183, '用户退出', 6, 0, 'com.admin.controller.AuthController.logout()', 'POST', '匿名', '', '/api/auth/logout', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-22 11:17:22');
INSERT INTO `sys_oper_log` VALUES (184, '发送验证码', 0, 0, 'com.admin.controller.AuthController.sendCode()', 'POST', '匿名', '', '/api/auth/send-code', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-22 11:17:36');
INSERT INTO `sys_oper_log` VALUES (185, '验证码登录', 5, 0, 'com.admin.controller.AuthController.codeLogin()', 'POST', '匿名', '', '/api/auth/login/code', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-22 11:17:56');
INSERT INTO `sys_oper_log` VALUES (186, '首页', 4, 1, '首页', 'GET', 'admin', '', '/home', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-22 11:17:57');
INSERT INTO `sys_oper_log` VALUES (187, '用户退出', 6, 0, 'com.admin.controller.AuthController.logout()', 'POST', '匿名', '', '/api/auth/logout', '127.0.0.1', '内网IP', '', '', 0, '', '2026-07-22 11:18:00');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色标识',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

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
INSERT INTO `sys_role_menu` VALUES (1, 33);
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
INSERT INTO `sys_role_menu` VALUES (2, 4);
INSERT INTO `sys_role_menu` VALUES (2, 6);
INSERT INTO `sys_role_menu` VALUES (2, 9);
INSERT INTO `sys_role_menu` VALUES (2, 10);
INSERT INTO `sys_role_menu` VALUES (2, 11);
INSERT INTO `sys_role_menu` VALUES (2, 15);
INSERT INTO `sys_role_menu` VALUES (2, 19);
INSERT INTO `sys_role_menu` VALUES (2, 20);
INSERT INTO `sys_role_menu` VALUES (2, 21);
INSERT INTO `sys_role_menu` VALUES (2, 22);
INSERT INTO `sys_role_menu` VALUES (2, 23);
INSERT INTO `sys_role_menu` VALUES (2, 24);
INSERT INTO `sys_role_menu` VALUES (2, 25);
INSERT INTO `sys_role_menu` VALUES (2, 26);
INSERT INTO `sys_role_menu` VALUES (2, 28);
INSERT INTO `sys_role_menu` VALUES (2, 29);
INSERT INTO `sys_role_menu` VALUES (2, 30);
INSERT INTO `sys_role_menu` VALUES (2, 36);
INSERT INTO `sys_role_menu` VALUES (2, 37);
INSERT INTO `sys_role_menu` VALUES (2, 38);
INSERT INTO `sys_role_menu` VALUES (2, 42);
INSERT INTO `sys_role_menu` VALUES (2, 45);
INSERT INTO `sys_role_menu` VALUES (2, 48);
INSERT INTO `sys_role_menu` VALUES (2, 49);
INSERT INTO `sys_role_menu` VALUES (2, 50);
INSERT INTO `sys_role_menu` VALUES (2, 52);
INSERT INTO `sys_role_menu` VALUES (2, 53);

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
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$dzwo49U8ey7PYVdVSQajluHTYrhZcSJ2DUmFJ2MSdMU14LeOXJn42', '超级管理员', 'adminx1@63.com', NULL, 1, '2026-06-30 15:06:14', '2026-07-21 17:22:13', '/uploads/88664664b967473dba49ae6518b775fb.jpg', '17788990011');
INSERT INTO `sys_user` VALUES (2, 'user', '$2a$10$YaTrI48ZudQVDBR/7/CInec7YGbO9BKtTpJocxlruIaIMLDLFKKUK', '普通用户', NULL, NULL, 1, '2026-07-01 11:11:05', '2026-07-01 17:39:30', NULL, '13344556677');
INSERT INTO `sys_user` VALUES (3, 'aaa', '$2a$10$PDOjqQnW33Bq3qKerhdyv.G7JGdpFHvjpyZIdBSc6lYbvutx12oYW', '西AOA', NULL, NULL, 1, '2026-07-01 11:37:43', '2026-07-02 21:44:23', '/uploads/eaa52e7b8fcd42a29b566fecc6989108.jpg', '16677889900');
INSERT INTO `sys_user` VALUES (4, 'ces', '$2a$10$vLRiwnETdkGUS/.zK1r7z.sDLy5ZHP9Dr8F9p2iXz7J6trC4hIaX2', '测试', NULL, NULL, 1, '2026-07-06 22:17:24', '2026-07-14 00:02:54', NULL, '17766558899');
INSERT INTO `sys_user` VALUES (5, 'acc', '$2a$10$p0gF1iGmR93.WCF7JQD6GesCqLBBhX/Y16.SFeggHFi1fU0lZmvjO', '测试用户', NULL, NULL, 1, '2026-07-14 22:06:01', '2026-07-14 22:06:01', NULL, '15566778989');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2);
INSERT INTO `sys_user_role` VALUES (3, 2);
INSERT INTO `sys_user_role` VALUES (4, 2);
INSERT INTO `sys_user_role` VALUES (5, 2);

SET FOREIGN_KEY_CHECKS = 1;
