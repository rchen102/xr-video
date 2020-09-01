package com.rchen.service;

import com.rchen.pojo.Users;
import com.rchen.pojo.UsersReport;

/**
 * @Author : crz
 */
public interface UserService {
    /**
     * 判断用户是否存在
     * @param username
     * @return
     */
    boolean queryUsernameIsExist(String username);

    /**
     * 保存用户（注册）
     * @param user
     */
    void saveUser(Users user);

    /**
     * 校验用户名和密码
     * @param username
     * @param pwdEncoded
     * @return
     */
    Users queryUserForLogin(String username, String pwdEncoded);

    /**
     * 修改用户信息
     * @param user
     */
    void updateUserInfo(Users user);

    /**
     * 查询用户信息
     * @param userId
     * @return
     */
    Users queryUserInfo(String userId);

    /**
     * 查询用户是否点赞过视频
     * @param userId
     * @param videoId
     * @return true 喜欢 ; false 不喜欢
     */
    boolean isUserLikeVideo(String userId, String videoId);

    /**
     * 关注用户
     * @param userId 被关注用户 id
     * @param fanId 粉丝id
     */
    void saveUserFanRelation(String userId, String fanId);

    /**
     * 取消关注用户
     * @param userId
     * @param fanId
     */
    void deleteUserFanRelation(String userId, String fanId);

    /**
     * 查询用户是否是粉丝
     * @param userId
     * @param fanId
     * @return
     */
    boolean queryIfFollow(String userId, String fanId);

    /**
     * 处理举报请求
     * @param usersReport
     */
    void reportUser(UsersReport usersReport);
}
