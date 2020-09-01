package com.rchen.mapper;

import com.rchen.pojo.Videos;
import com.rchen.pojo.vo.VideosVO;
import com.rchen.utils.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VideosMapperCustom extends MyMapper<Videos> {

    /**
     * 条件查询所有视频（按照时间发布先后顺序）
     * @param videoDesc 如果不为空，查询所有包含 videoDesc 描述的视频
     * @param userId 如果不为空，查询所有该用户 userId 发表的视频
     * @return
     */
    List<VideosVO> queryAllVideos(@Param("videoDesc") String videoDesc,
                                  @Param("userId") String userId);


    /**
     * 查询该用户关注者们发布的视频（按照时间发布先后顺序）
     * @param userId
     * @return
     */
    List<VideosVO> queryMyFollowVideos(String userId);


    /**
     * 查询该用户点赞（收藏）的视频（按照时间发布先后顺序）
     * @param userId
     * @return
     */
    List<VideosVO> queryMyLikeVideos(@Param("userId") String userId);

    /**
     * 视频 Like +1
     * @param videoId
     */
    void addVideoLikeCount(String videoId);

    /**
     * 视频 Like-1
     * @param videoId
     */
    void reduceVideoLikeCount(String videoId);
}