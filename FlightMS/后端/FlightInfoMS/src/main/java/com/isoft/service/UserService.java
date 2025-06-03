package com.isoft.service;

import com.isoft.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     * 
     * @param user 用户信息
     * @return 注册结果，true表示成功，false表示失败
     */
    boolean register(User user);

    /**
     * 用户登录
     * 
     * @param userid  用户ID
     * @param userpwd 用户密码
     * @return 登录结果，true表示成功，false表示失败
     */
    boolean login(String userid, String userpwd);

    /**
     * 检查用户是否存在
     * 
     * @param userid 用户ID
     * @return 存在返回true，不存在返回false
     */
    boolean checkUserExists(String userid);
}