-- 聊天消息表
create table message
(
    id         bigint auto_increment comment 'id'
        primary key,
    senderId   bigint                             not null comment '发送者id',
    receiverId bigint                             not null comment '接收者id',
    content    varchar(1024)                      not null comment '消息内容',
    isRead     tinyint  default 0                 not null comment '0-未读，1-已读',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
)
    comment '聊天消息';

create index idx_message_sender
    on message (senderId, receiverId);

create index idx_message_receiver
    on message (receiverId, senderId);
