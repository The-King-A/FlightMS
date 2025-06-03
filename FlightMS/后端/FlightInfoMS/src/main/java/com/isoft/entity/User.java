package com.isoft.entity;

import lombok.Data;

/**
 * 用户实体类，对应数据库中的userinfo表
 */
@Data
public class User {
    private String userid; // 用户ID
    private String userpwd; // 用户密码

    // 无参构造函数
    public User() {
    }

    // 带参构造函数
    public User(String userid, String userpwd) {
        this.userid = userid;
        this.userpwd = userpwd;
    }
}