package com.rchen.controller;

import com.rchen.pojo.Users;
import com.rchen.pojo.vo.UsersVO;
import com.rchen.service.UserService;
import com.rchen.utils.IMoocJSONResult;
import com.rchen.utils.MD5Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @Author : crz
 */
@RestController
@Api(value = "账户相关业务接口", tags = {"账户相关业务 controller"})
public class RegistLoginController extends BasicController {

    @Autowired
    private UserService userService;

    @ApiOperation(value = "用户注册", notes = "用户注册的接口")
    @PostMapping("/regist")
    public IMoocJSONResult register(@RequestBody Users user) throws Exception {
        // 1. 判断用户名和密码不能为空
        if (StringUtils.isBlank(user.getUsername())
                || StringUtils.isBlank(user.getPassword())) {
            return IMoocJSONResult.errorMsg("用户名和密码不能为空");
        }
        // 2. 判断用户是否存在
        boolean userNameIsExist = userService.queryUsernameIsExist(user.getUsername());

        // 3. 保存用户，注册成功
        if (!userNameIsExist) {
            // 完善用户信息
            user.setNickname(user.getUsername());
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
            user.setFansCounts(0);
            user.setReceiveLikeCounts(0);
            user.setFollowCounts(0);
            userService.saveUser(user);
        } else {
            return IMoocJSONResult.errorMsg("用户名已经存在");
        }

        // 安全考虑，密码不再传回前端
        user.setPassword("");

        // 4. 创建 session 保存到 redis，并返回 VO 对象
        UsersVO usersVO = setUserRedisSessionToken(user);
        return IMoocJSONResult.ok(usersVO);
    }

    @ApiOperation(value = "用户登录", notes = "用户登录的接口")
    @PostMapping("/login")
    public IMoocJSONResult login(@RequestBody Users user) throws Exception {
        String username = user.getUsername();
        String password = user.getPassword();

        // 1. 判断用户名和密码不能为空
        if (StringUtils.isBlank(username)
                || StringUtils.isBlank(password)) {
            return IMoocJSONResult.errorMsg("用户名和密码不能为空");
        }

        // 2. 校验用户名和密码
        Users userResult = userService.queryUserForLogin(username, MD5Utils.getMD5Str(password));

        // 3. 准备返回
        if (userResult != null) {
            userResult.setPassword("");
            // 创建 session，并保存到 VO 对象
            UsersVO userVO = setUserRedisSessionToken(userResult);
            return IMoocJSONResult.ok(userVO);
        } else {
            return IMoocJSONResult.errorMsg("用户名和密码不正确，请重试");
        }
    }

    /**
     * 用户注销的接口，因为传入对象不再是 User，需要自己定义 Swagger 的接口参数描述
     * @param userId
     * @return
     */
    @ApiOperation(value = "用户注销", notes = "用户注销的接口")
    @ApiImplicitParam(name="userId", value="用户id", required=true,
            dataType="String", paramType="query")
    @PostMapping("/logout")
    public IMoocJSONResult logout(String userId) {
        redis.del(USER_REDIS_SESSION + ":" + userId);
        return IMoocJSONResult.ok();
    }

    /**
     * 创建 session，保存到 redis，并返回 VO 对象
     * @param user
     * @return
     */
    private UsersVO setUserRedisSessionToken(Users user) {
        // 1. 创建 session 并保存到 redis
        String uniqueToken = UUID.randomUUID().toString();
        redis.set(USER_REDIS_SESSION + ":" + user.getId(), uniqueToken, 1000 * 60 * 30);
        // 2. 创建 VO 对象，并返回
        UsersVO userVO = new UsersVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setUserToken(uniqueToken);
        return userVO;
    }
}
