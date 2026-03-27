-- 用户表
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '学号/工号/登录名',
  `password` varchar(100) NOT NULL COMMENT '密码(加密后)',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `role_id` int(11) NOT NULL COMMENT '角色ID',
  `avatar` varchar(255) DEFAULT NULL COMMENT '自定义头像URL',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(1正常 0禁用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 角色表 (分为：1-学生, 2-维修工, 3-管理员)
CREATE TABLE `sys_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色标识',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 此处省略复杂的菜单权限表(sys_menu)和角色菜单关联表(sys_role_menu)，
-- 在毕设中，可以先用简单的 role_id 来控制前端路由和后端接口权限，后续再细化。

--------------------------------------------------------------------------------
-- 活动信息表
CREATE TABLE `biz_activity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL COMMENT '活动标题',
  `content` text COMMENT '活动详细内容(富文本)',
  `cover_image` varchar(255) DEFAULT NULL COMMENT '活动海报/封面图',
  `start_time` datetime NOT NULL COMMENT '活动开始时间',
  `end_time` datetime NOT NULL COMMENT '活动结束时间',
  `location` varchar(100) DEFAULT NULL COMMENT '活动地点',
  `publisher_id` bigint(20) NOT NULL COMMENT '发布者ID(社团/管理员)',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(1报名中 0已结束)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动中心-活动信息表';

-- 活动报名表
CREATE TABLE `biz_activity_registration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
  `user_id` bigint(20) NOT NULL COMMENT '报名用户ID(学生)',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(1已报名 0已取消)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动中心-报名记录表';

-- 活动收藏表 (用于协同过滤推荐算法的数据采集)
CREATE TABLE `biz_activity_collection` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activity_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动中心-收藏记录表';

------------------------------------------------------------------------------
-- 报修工单表
CREATE TABLE `biz_repair_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL COMMENT '发起报修的学生ID',
  `worker_id` bigint(20) DEFAULT NULL COMMENT '接单的维修工ID',
  `dorm_location` varchar(100) NOT NULL COMMENT '宿舍具体位置(如: 1号楼302)',
  `title` varchar(100) NOT NULL COMMENT '报修简述',
  `description` text COMMENT '图文详情描述',
  `images` json DEFAULT NULL COMMENT '上传的损坏图片(存JSON数组)',
  `status` tinyint(4) DEFAULT '0' COMMENT '工单状态(0-待接单, 1-维修中, 2-待评价, 3-已完成)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宿舍报修工单表';

-- 报修评价表
CREATE TABLE `biz_repair_evaluation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '工单ID',
  `score` int(11) NOT NULL COMMENT '评分(1-5星)',
  `comment` varchar(255) DEFAULT NULL COMMENT '文字评价',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报修评价表';

-----------------------------------------------------------------------------
-- 站内消息表 (配合异步事件驱动)
CREATE TABLE `sys_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `receiver_id` bigint(20) NOT NULL COMMENT '接收者ID',
  `title` varchar(100) NOT NULL COMMENT '消息标题',
  `content` varchar(255) NOT NULL COMMENT '消息内容',
  `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读(0未读 1已读)',
  `type` varchar(20) DEFAULT 'SYSTEM' COMMENT '消息类型(如: REPAIR, ACTIVITY, SYSTEM)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息通知表';

-- 失物招领表
CREATE TABLE `biz_lost_found` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` tinyint(1) NOT NULL COMMENT '类型(0-失物寻物, 1-捡到招领)',
  `item_name` varchar(100) NOT NULL COMMENT '物品名称',
  `description` text COMMENT '特征详细描述(用于ES检索)',
  `location` varchar(100) DEFAULT NULL COMMENT '丢失/捡到地点',
  `contact_info` varchar(100) NOT NULL COMMENT '联系方式',
  `images` json DEFAULT NULL COMMENT '物品图片',
  `publisher_id` bigint(20) NOT NULL COMMENT '发布者ID',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态(0-寻找中, 1-已解决, 2-违规下架)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='失物招领大厅表';



