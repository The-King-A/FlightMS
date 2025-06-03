package com.isoft.service.impl;

import com.isoft.entity.User;
import com.isoft.mapper.UserMapper;
import com.isoft.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean register(User user) {
        // 检查用户是否已存在
        if (checkUserExists(user.getUserid())) {
            return false; // 用户已存在，注册失败
        }

        // 插入新用户
        int result = userMapper.insert(user);
        return result > 0;
    }

    @Override
    public boolean login(String userid, String userpwd) {
        // 查询用户
        User user = userMapper.findByUserid(userid);

        // 检查用户是否存在且密码是否匹配
        return user != null && user.getUserpwd().equals(userpwd);
    }

    @Override
    public boolean checkUserExists(String userid) {
        return userMapper.findByUserid(userid) != null;
    }
}