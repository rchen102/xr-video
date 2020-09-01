package com.rchen.service;

import com.rchen.pojo.Comments;
import com.rchen.pojo.Videos;
import com.rchen.utils.PagedResult;

import java.util.List;

/**
 * @Author : crz
 */
public interface VideoService {
    /**
     * 保存视频
     * @param video
     * @return 返回视频 id
     */
    String saveVideo(Videos video);

    /**
     * 删除视频
     * @param videoId
     * @param creatorId
     * @return 返回被删除的视频信息
     */
    Videos delete(String videoId, String creatorId);

    /**
     * 更新视频封面
     * @param videoId
     * @param coverPath
     * @return
     */
    void updateVideoCover(String videoId, String coverPath);

    /**
     * 1. 分页查询视频列表
     * 2. 搜索查询视频列表（分页）
     * @param page
     * @param pageSize
     * @return
     */
    PagedResult getAllVideos(Videos video, Integer isSaveRecord, Integer page, Integer pageSize);


    /**
     * 分页查询该用户点赞的视频列表（收藏）
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PagedResult queryMyLikeVideos(String userId, Integer page, Integer pageSize);

    /**
     * 分页查询该用户关注的人发布的视频列表
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PagedResult queryMyFollowVideos(String userId, Integer page, Integer pageSize);

    /**
     * 获取当前热搜词
     * @return
     */
    List<String> getHotWords();

    /**
     * 用户喜欢视频
     * @param userId
     * @param videoId
     * @param videoCreaterId
     */
    void userLikeVideo(String userId, String videoId, String videoCreaterId);

    /**
     * 用户取消喜欢视频
     * @param userId
     * @param videoId
     * @param videoCreaterId
     */
    void userUnLikeVideo(String userId, String videoId, String videoCreaterId);

    /**
     * 保存一条评论
     * @param comment
     */
    void saveComment(Comments comment);

    /**
     * 分页查询某个视频的所有评论
     * @param videoId
     * @param page
     * @param pageSize
     * @return
     */
    PagedResult getAllComments(String videoId, Integer page, Integer pageSize);
}
