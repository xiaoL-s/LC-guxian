package com.guxian.system.vo;
import lombok.Data;

@Data
public class LoginRespVO {
    private Long id;
    private String username;
    private String realName;
    private String token;
}