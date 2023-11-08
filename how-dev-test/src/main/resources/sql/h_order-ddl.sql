--1.user_order(用户订单表)
CREATE TABLE `user_order` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `order_id` bigint(20) unsigned NOT NULL COMMENT '订单id',
  `user_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '用户id',
  `order_number` varchar(128) NOT NULL DEFAULT '' COMMENT '订单编号',
  `total_price` decimal(10,2) DEFAULT '0.00' COMMENT '订单总价',
  `status` int(11) DEFAULT NULL COMMENT '状态，1-待付款，2-待发货，3-已发货，4-已签收，5-已取消',
  `created` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户订单表';

--2.order_detail(订单详情表)
CREATE TABLE `order_detail` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `order_detail_id` bigint(20) unsigned NOT NULL COMMENT '订单详情id',
  `order_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '订单id',
  `product_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '产品id',
  `quantity` int(10) NOT NULL DEFAULT '0' COMMENT '数量',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '价格',
  `created` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单详情表';

--3.product(产品表)
CREATE TABLE `product` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `product_id` bigint(20) unsigned NOT NULL COMMENT '产品id',
  `name` varchar(128) NOT NULL DEFAULT '' COMMENT '产品名称',
  `description` varchar(128) DEFAULT '' COMMENT '产品描述',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '产品单价',
  `category_id` bigint(20) unsigned NOT NULL COMMENT '分类id',
  `created` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表';

--4.category(分类表)
CREATE TABLE `category` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `category_id` bigint(20) unsigned NOT NULL COMMENT '分类id',
  `name` varchar(128) NOT NULL DEFAULT '' COMMENT '分类名称',
  `description` varchar(128) DEFAULT '' COMMENT '分类描述',
  `created` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';
