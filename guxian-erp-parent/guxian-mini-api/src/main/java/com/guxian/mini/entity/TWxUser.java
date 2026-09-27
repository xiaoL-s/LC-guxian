package com.guxian.mini.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_wx_user")
public class TWxUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String openid;
    private Long sysUserId;
    private String nickName;
    private String avatar;
    private String phone;
    private String sessionKey;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
