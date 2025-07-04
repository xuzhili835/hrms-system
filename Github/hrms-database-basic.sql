/*
 HRMS 人力资源管理系统 - 基础数据库初始化脚本
 
 此脚本包含：
 - 完整的表结构
 - 基础测试数据（管理员账号、员工账号、少量业务数据）
 
 使用方法：
 1. 创建数据库：CREATE DATABASE hrms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
 2. 使用数据库：USE hrms_db;
 3. 执行此脚本：SOURCE /path/to/hrms-database-basic.sql;
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '管理员用户名',
  `pwd` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（加密后）',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'ADMIN' COMMENT '角色',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
-- 默认管理员账号：admin / 123456
INSERT INTO `admin` VALUES (1, 'admin', '$2a$10$OPJxcxBXiLjMKb/5vt9EeO609pi/EOh26FzAi8xQM6iXJhPhiYNW2', 'ADMIN', NOW(), NOW());

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告内容',
  `pub_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发布者ID',
  `pub_date` date NOT NULL COMMENT '发布日期',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片URL',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '已发布' COMMENT '公告状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_announcement_pub_date`(`pub_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of announcement
-- ----------------------------
-- 示例公告数据
INSERT INTO `announcement` VALUES (1, '欢迎使用HRMS系统', '欢迎使用人力资源管理系统！本系统提供员工管理、薪资管理、请假管理等功能。如有问题请联系系统管理员。', 'admin', CURDATE(), NULL, '已发布', NOW(), NOW());
INSERT INTO `announcement` VALUES (2, '系统使用指南', '请各位员工熟悉系统操作流程：1. 登录系统 2. 查看个人信息 3. 提交请假申请 4. 查看薪资记录。详细操作手册请咨询人事部。', 'admin', CURDATE(), NULL, '已发布', NOW(), NOW());
INSERT INTO `announcement` VALUES (3, '测试公告（草稿）', '这是一条测试草稿公告，用于展示公告管理功能。', 'admin', CURDATE(), NULL, '草稿', NOW(), NOW());

-- ----------------------------
-- Table structure for employee
-- ----------------------------
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `emp_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '员工工号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `dept` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门',
  `pos` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职位',
  `pwd` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（加密后）',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'EMPLOYEE' COMMENT '角色',
  `entry_time` date NULL DEFAULT NULL COMMENT '入职时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '在职' COMMENT '员工状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `emp_id`(`emp_id` ASC) USING BTREE,
  INDEX `idx_employee_emp_id`(`emp_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of employee
-- ----------------------------
-- 测试员工账号，密码都是 123456
INSERT INTO `employee` VALUES (1, 'E001', '张三', '技术部', 'Java开发工程师', '$2a$10$OPJxcxBXiLjMKb/5vt9EeO609pi/EOh26FzAi8xQM6iXJhPhiYNW2', 'EMPLOYEE', '2023-01-15', '在职', NOW(), NOW());
INSERT INTO `employee` VALUES (2, 'E002', '李四', '人事部', '人事专员', '$2a$10$OPJxcxBXiLjMKb/5vt9EeO609pi/EOh26FzAi8xQM6iXJhPhiYNW2', 'EMPLOYEE', '2023-02-20', '在职', NOW(), NOW());
INSERT INTO `employee` VALUES (3, 'E003', '王五', '财务部', '会计', '$2a$10$OPJxcxBXiLjMKb/5vt9EeO609pi/EOh26FzAi8xQM6iXJhPhiYNW2', 'EMPLOYEE', '2023-03-10', '在职', NOW(), NOW());

-- ----------------------------
-- Table structure for leave_application
-- ----------------------------
DROP TABLE IF EXISTS `leave_application`;
CREATE TABLE `leave_application`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `emp_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '员工工号',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请假类型',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `days` int NOT NULL COMMENT '请假天数',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请假原因',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '待审批' COMMENT '审批状态',
  `approver` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批人',
  `approve_time` timestamp NULL DEFAULT NULL COMMENT '审批时间',
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '审批备注',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_leave_emp_id`(`emp_id` ASC) USING BTREE,
  INDEX `idx_leave_status`(`status` ASC) USING BTREE,
  CONSTRAINT `leave_application_ibfk_1` FOREIGN KEY (`emp_id`) REFERENCES `employee` (`emp_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '请假申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of leave_application
-- ----------------------------
-- 示例请假申请数据
INSERT INTO `leave_application` VALUES (1, 'E001', '年假', '2024-12-25', '2024-12-27', 3, '年底休假', '已批准', 'admin', NOW(), '同意申请', NOW(), NOW());
INSERT INTO `leave_application` VALUES (2, 'E002', '病假', '2024-12-20', '2024-12-21', 2, '身体不适', '已批准', 'admin', NOW(), NULL, NOW(), NOW());
INSERT INTO `leave_application` VALUES (3, 'E001', '事假', CURDATE() + INTERVAL 7 DAY, CURDATE() + INTERVAL 7 DAY, 1, '处理个人事务', '待审批', NULL, NULL, NULL, NOW(), NOW());
INSERT INTO `leave_application` VALUES (4, 'E003', '年假', CURDATE() + INTERVAL 10 DAY, CURDATE() + INTERVAL 12 DAY, 3, '家庭聚会', '待审批', NULL, NULL, NULL, NOW(), NOW());

-- ----------------------------
-- Table structure for salaries
-- ----------------------------
DROP TABLE IF EXISTS `salaries`;
CREATE TABLE `salaries`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `emp_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '员工工号',
  `base_salary` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '基本工资',
  `allowance` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '津贴',
  `bonus` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '奖金',
  `deduction` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '扣款',
  `total_salary` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '总工资',
  `month` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工资月份 格式：YYYY-MM',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '已发放' COMMENT '发放状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_emp_month`(`emp_id` ASC, `month` ASC) USING BTREE,
  INDEX `idx_salaries_emp_id`(`emp_id` ASC) USING BTREE,
  INDEX `idx_salaries_month`(`month` ASC) USING BTREE,
  CONSTRAINT `salaries_ibfk_1` FOREIGN KEY (`emp_id`) REFERENCES `employee` (`emp_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '薪资表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of salaries
-- ----------------------------
-- 示例薪资数据（最近几个月）
INSERT INTO `salaries` VALUES (1, 'E001', 8000.00, 1000.00, 2000.00, 200.00, 10800.00, '2024-10', '已发放', NOW(), NOW());
INSERT INTO `salaries` VALUES (2, 'E001', 8000.00, 1000.00, 2500.00, 250.00, 11250.00, '2024-11', '已发放', NOW(), NOW());
INSERT INTO `salaries` VALUES (3, 'E001', 8000.00, 1000.00, 3000.00, 300.00, 11700.00, '2024-12', '已确认', NOW(), NOW());

INSERT INTO `salaries` VALUES (4, 'E002', 6000.00, 800.00, 1200.00, 120.00, 7880.00, '2024-10', '已发放', NOW(), NOW());
INSERT INTO `salaries` VALUES (5, 'E002', 6000.00, 800.00, 1500.00, 150.00, 8150.00, '2024-11', '已发放', NOW(), NOW());
INSERT INTO `salaries` VALUES (6, 'E002', 6000.00, 800.00, 1800.00, 180.00, 8420.00, '2024-12', '已确认', NOW(), NOW());

INSERT INTO `salaries` VALUES (7, 'E003', 7000.00, 900.00, 1500.00, 150.00, 9250.00, '2024-10', '已发放', NOW(), NOW());
INSERT INTO `salaries` VALUES (8, 'E003', 7000.00, 900.00, 1600.00, 160.00, 9340.00, '2024-11', '已发放', NOW(), NOW());
INSERT INTO `salaries` VALUES (9, 'E003', 7000.00, 900.00, 2000.00, 200.00, 9700.00, '2024-12', '草稿', NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 数据库初始化完成
-- ----------------------------
-- 
-- 🎉 数据库初始化成功！
-- 
-- 默认账号信息：
-- 管理员：admin / 123456
-- 员工1：E001 (张三) / 123456
-- 员工2：E002 (李四) / 123456
-- 员工3：E003 (王五) / 123456
-- 
-- 包含的测试数据：
-- - 3条公告（2条已发布，1条草稿）
-- - 4条请假申请（2条已批准，2条待审批）
-- - 9条薪资记录（展示不同状态）
-- 
-- ⚠️  生产环境请务必修改默认密码！
-- 
-- ---------------------------- 