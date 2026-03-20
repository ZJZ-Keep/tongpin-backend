# 数据库初始化

-- 创建库
create database if not exists  zjz;

-- 切换库
use  zjz;

# 用户表
create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    phone        varchar(256)                       null comment '电话',
    email        varchar(512)                       null comment '邮箱',
    userStatus   int      default 0                 not null comment '用户状态',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    userName     varchar(256)                       null comment '用户名',
    userAccount  varchar(256)                       null comment '账号',
    gender       tinyint                            null comment '性别',
    avatarUrl    varchar(1024)                      null comment '头像',
    userPassword varchar(512)                       not null comment '密码',
    UserRole     tinyint  default 0                 not null comment '角色',
    planetCode   varchar(512)                       null comment '星球编号',
    tags         varchar(1024)                      null comment '标签json列表'
)
    comment '用户';

# 导入示例用户
INSERT INTO  zjz.user (username, userAccount, avatarUrl, gender, userPassword, phone, email, userStatus, createTime, updateTime, isDelete, userRole, planetCode) VALUES (' ', ' zjz', 'https://himg.bdimg.com/sys/portraitn/item/public.1.e137c1ac.yS1WqOXfSWEasOYJ2-0pvQ', null, 'b0dd3697a192885d7c055db46155b26a', null, null, 0, '2023-08-06 14:14:22', '2023-08-06 14:39:37', 0, 1, '1');

alter table user add COLUMN tags varchar(1024) null comment '标签列表';


-- auto-generated definition
create table tag
(
    id         bigint auto_increment comment 'id'
        primary key,
    tagName    varchar(256)                       not null comment '标签名称',
    userId     bigint                             not null comment '用户id',
    parentId   bigint                             not null comment '父标签id',
    isParent   tinyint                            not null comment '0-不是，1-是父标签',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
)
    comment '标签';

create index ind_userId
    on tag (userId);

create index unInd_tagName
    on tag (tagName);

create table team
(
    id           bigint auto_increment comment 'id'
        primary key,
    name         varchar(256)                       not null comment '队伍名',
    description  varchar(1024)                      null comment '描述',
    maxNum       varchar(256)    default 1          not null comment '最大人数',
    expireTime   datetime                           null comment '过期时间',
    userId       bigint                             comment '队长id',
    status       int    default 0               not null comment '0 - 公开，1 - 私有，2 - 加密',
    password     varchar(512)                       null comment '密码',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除'
)
    comment '队伍';

create table user_team
(
    id           bigint auto_increment comment 'id'
        primary key,
    userId            bigint comment '用户id',
    teamId            bigint comment '队伍id',
    joinTime datetime  null comment '加入时间',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete     tinyint  default 0                 not null comment '是否删除'
)
    comment '用户队伍关系';

