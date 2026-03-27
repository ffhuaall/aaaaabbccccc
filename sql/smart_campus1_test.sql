INSERT INTO `sys_user` (`username`, `password`, `realName`, `role_id`, `status`) 
VALUES ('student01', '$2a$10$7JB720yubVSZvUI0rEq0vucIEI/O/L/8g/H9f/z/yZ.M.QZ.k.x.O', '张三', 1, 1);



-- 教室基础信息表
CREATE TABLE `biz_classroom` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `building_name` varchar(50) NOT NULL COMMENT '教学楼名称(如: 第一教学楼)',
  `room_number` varchar(20) NOT NULL COMMENT '教室门牌号(如: 101)',
  `capacity` int(11) DEFAULT '50' COMMENT '容纳人数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教室基础信息表';

-- 教室排期(空闲状态)表
CREATE TABLE `biz_classroom_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `classroom_id` bigint(20) NOT NULL COMMENT '教室ID',
  `query_date` date NOT NULL COMMENT '日期(如: 2026-05-20)',
  `period` tinyint(4) NOT NULL COMMENT '节次：1-上午, 2-下午, 3-晚上',
  `is_free` tinyint(1) DEFAULT '1' COMMENT '是否空闲：1-空闲, 0-被占用(有课)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_date_period` (`classroom_id`,`query_date`,`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教室空闲排期表';

-- 插入一些测试数据
INSERT INTO `biz_classroom` VALUES (1, '第一教学楼', '101', 50), (2, '第一教学楼', '102', 100), (3, '第二教学楼', '201', 60);
INSERT INTO `biz_classroom_schedule` VALUES (1, 1, CURDATE(), 3, 1), (2, 2, CURDATE(), 3, 0), (3, 3, CURDATE(), 3, 1);
-- (注意：这里我用 CURDATE() 插入了今天的排期数据，101和201晚上(3)是空闲的，102晚上有课)