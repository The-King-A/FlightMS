package com.isoft.mapper;

import com.isoft.entity.User;
import org.apache.ibatis.annotations.*;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户ID查询用户
     * 
     * @param userid 用户ID
     * @return 用户对象，如果不存在则返回null
     */
    @Select("SELECT * FROM userinfo WHERE Userid = #{userid}")
    User findByUserid(String userid);

    /**
     * 插入新用户
     * 
     * @param user 用户对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO userinfo (Userid, Userpwd) VALUES (#{userid}, #{userpwd})")
    int insert(User user);

    /**
     * 更新用户密码
     * 
     * @param user 用户对象
     * @return 影响的行数
     */
    @Update("UPDATE userinfo SET Userpwd = #{userpwd} WHERE Userid = #{userid}")
    int updatePassword(User user);

    /**
     * 删除用户
     * 
     * @param userid 用户ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM userinfo WHERE Userid = #{userid}")
    int deleteByUserid(String userid);
}