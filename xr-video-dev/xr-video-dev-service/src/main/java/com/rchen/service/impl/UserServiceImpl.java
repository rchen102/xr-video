package com.rchen.service.impl;


import com.rchen.mapper.UsersFansMapper;
import com.rchen.mapper.UsersLikeVideosMapper;
import com.rchen.mapper.UsersMapper;
import com.rchen.mapper.UsersReportMapper;
import com.rchen.pojo.Users;
import com.rchen.pojo.UsersFans;
import com.rchen.pojo.UsersLikeVideos;
import com.rchen.pojo.UsersReport;
import com.rchen.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import java.util.Date;
import java.util.List;

/**
 * @Author : crz
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersReportMapper usersReportMapper;

    @Autowired
    private UsersLikeVideosMapper usersLikeVideosMapper;

    @Autowired
    private UsersFansMapper usersFansMapper;

    @Autowired
    private UsersMapper userMapper;

    @Autowired
    private Sid sid;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameIsExist(String username) {
        Users user = new Users();
        user.setUsername(username);

        Users result = userMapper.selectOne(user);
        return !(result == null);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveUser(Users user) {
        // 不使用自增 id，手动生成全局唯一 id
        String userId = sid.nextShort();
        user.setId(userId);
        userMapper.insert(user);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String pwdEncoded) {
        Example userExample = new Example(Users.class);
        Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username", username)
                .andEqualTo("password", pwdEncoded);
        Users result = userMapper.selectOneByExample(userExample);
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateUserInfo(Users user) {
        Example userExample = new Example(Users.class);
        Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("id", user.getId());
        userMapper.updateByExampleSelective(user, userExample);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserInfo(String userId) {
        Example userExample = new Example(Users.class);
        Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("id", userId);
        return userMapper.selectOneByExample(userExample);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean isUserLikeVideo(String userId, String videoId) {

        // 用户可能未登录，不用查询，之间返回 false
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(videoId)) {
            return false;
        }

        Example example = new Example(UsersLikeVideos.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId", userId)
                .andEqualTo("videoId", videoId);

        List<UsersLikeVideos> list = usersLikeVideosMapper.selectByExample(example);
        if (list != null && list.size() > 0) {
            return true;
        }
        return false;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveUserFanRelation(String userId, String fanId) {
        // 1. 插入 user_fan 关系
        String relId = sid.nextShort(); // 手动生成主键 id
        UsersFans userFan = new UsersFans();
        userFan.setId(relId);
        userFan.setUserId(userId);
        userFan.setFanId(fanId);
        usersFansMapper.insert(userFan);

        // 2. 更新粉丝数 +1
        userMapper.addFansCount(userId);

        // 3. 更新用户关注数 +1
        userMapper.addFollowCount(fanId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteUserFanRelation(String userId, String fanId) {
        // 1. 删除 user_fan 关系
        Example example = new Example(UsersFans.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId", userId)
                .andEqualTo("fanId", fanId);
        usersFansMapper.deleteByExample(example);

        // 2. 更新粉丝数 -1
        userMapper.reduceFansCount(userId);

        // 3. 更新用户关注数量 -1
        userMapper.reduceFollowCount(fanId);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryIfFollow(String userId, String fanId) {
        Example example = new Example(UsersFans.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId", userId)
                .andEqualTo("fanId", fanId);

        List<UsersFans> list = usersFansMapper.selectByExample(example);
        if (list != null && list.size() > 0) {
            return true;
        }
        return false;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void reportUser(UsersReport usersReport) {
        // 手动生成主键 id
        String id = sid.nextShort();
        usersReport.setId(id);
        usersReport.setCreateDate(new Date());
        usersReportMapper.insert(usersReport);
    }
}
