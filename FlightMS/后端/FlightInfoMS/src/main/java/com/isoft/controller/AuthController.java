package com.isoft.controller;

import com.isoft.entity.User;
import com.isoft.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器，处理用户登录和注册
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // 指定前端开发服务器地址
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * 
     * @param user 用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        // 参数验证
        if (user.getUserid() == null || user.getUserid().trim().isEmpty() ||
                user.getUserpwd() == null || user.getUserpwd().trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "用户名和密码不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        // 尝试注册
        boolean success = userService.register(user);

        if (success) {
            response.put("success", true);
            response.put("message", "注册成功！请登录");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "用户名已存在，请更换用户名");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 用户登录
     * 
     * @param user 用户信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        // 参数验证
        if (user.getUserid() == null || user.getUserid().trim().isEmpty() ||
                user.getUserpwd() == null || user.getUserpwd().trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "用户名和密码不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        // 尝试登录
        boolean success = userService.login(user.getUserid(), user.getUserpwd());

        if (success) {
            response.put("success", true);
            response.put("message", "登录成功！");
            // 在实际应用中，这里可以生成JWT令牌
            // response.put("token", generateToken(user));
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "用户名或密码错误");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 检查用户名是否可用
     * 
     * @param userid 用户ID
     * @return 检查结果
     */
    @GetMapping("/check/{userid}")
    public ResponseEntity<?> checkUsername(@PathVariable String userid) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = userService.checkUserExists(userid);

        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
}