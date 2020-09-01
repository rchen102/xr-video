package com.rchen.mapper;

import com.rchen.pojo.Users;
import com.rchen.utils.MyMapper;

public interface UsersMapper extends MyMapper<Users> {

    /**
     * 用户受喜欢数量 +1
     * @param userId 当前被操作的用户id
     */
    void addReceiveLikeCount(String userId);

    /**
     * 用户受喜欢数量 -1
     * @param userId 当前被操作的用户id
     */
    void reduceReceiveLikeCount(String userId);

    /**
     * 用户粉丝数量 +1
     * @param userId
     */
    void addFansCount(String userId);

    /**
     * 用户粉丝数量 -1
     * @param userId
     */
    void reduceFansCount(String userId);


    /**
     * 用户所关注的数量 +1
     * @param userId
     */
    void addFollowCount(String userId);


    /**
     * 用户所关注的数量 -1
     * @param userId
     */
    void reduceFollowCount(String userId);


}