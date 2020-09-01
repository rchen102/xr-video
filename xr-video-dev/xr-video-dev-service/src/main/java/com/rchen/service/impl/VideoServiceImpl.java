package com.rchen.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rchen.mapper.CommentsMapper;
import com.rchen.mapper.CommentsMapperCustom;
import com.rchen.mapper.SearchRecordsMapper;
import com.rchen.mapper.UsersLikeVideosMapper;
import com.rchen.mapper.UsersMapper;
import com.rchen.mapper.VideosMapper;
import com.rchen.mapper.VideosMapperCustom;
import com.rchen.pojo.Comments;
import com.rchen.pojo.SearchRecords;
import com.rchen.pojo.UsersLikeVideos;
import com.rchen.pojo.Videos;
import com.rchen.pojo.vo.CommentsVO;
import com.rchen.pojo.vo.VideosVO;
import com.rchen.service.VideoService;
import com.rchen.utils.PagedResult;
import com.rchen.utils.TimeAgoUtils;
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
public class VideoServiceImpl implements VideoService {

    @Autowired
    private UsersLikeVideosMapper usersLikeVideosMapper;

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private VideosMapper videosMapper;

    /**
     * 自定义 mapper 用于关联查询
     */
    @Autowired
    private VideosMapperCustom videosMapperCustom;

    /**
     * 搜索结果的数据表 mapper
     */
    @Autowired
    private SearchRecordsMapper searchRecordsMapper;

    @Autowired
    private CommentsMapper commentsMapper;

    @Autowired
    private CommentsMapperCustom commentMapperCustom;

    @Autowired
    private Sid sid;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Videos delete(String videoId, String creatorId) {
        // 1. 查询视频存储目录和视频封面目录
        Example example = new Example(Videos.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", videoId);
        Videos video = videosMapper.selectOneByExample(example);

        // 2. 删除视频列表 videos 中的视频
        videosMapper.deleteByExample(example);

        // 3. 删除 user_like 表中的相关关系
        example = new Example(UsersLikeVideos.class);
        criteria = example.createCriteria();
        criteria.andEqualTo("videoId", videoId);
        usersLikeVideosMapper.deleteByExample(example);

        return video;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public String saveVideo(Videos video) {
        String id = sid.nextShort();
        video.setId(id);
        videosMapper.insertSelective(video);
        return id;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateVideoCover(String videoId, String coverPath) {
        Videos video = new Videos();
        video.setId(videoId);
        video.setCoverPath(coverPath);
        videosMapper.updateByPrimaryKeySelective(video);
    }

    /**
     * 分页查询，实现了三种功能
     * @param page 当前页数
     * @param pageSize 一页的大小
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public PagedResult getAllVideos(Videos video,
                                    Integer isSaveRecord,
                                    Integer page,
                                    Integer pageSize) {


        // 1. 判断本次调用是否是搜索功能（需要保存关键词）
        String desc = video.getVideoDesc();
        if (isSaveRecord != null && isSaveRecord == 1) {
            SearchRecords record = new SearchRecords();
            String recordId = sid.nextShort();
            record.setId(recordId);
            record.setContent(desc);
            searchRecordsMapper.insert(record);
        }
        // 2. 判断本次调用是否是个人所有作品查询功能
        String userId = video.getUserId();

        // 开始分页查询
        PageHelper.startPage(page, pageSize);
        /**
         * list 的大小和 pagesize 一样，除非是最后一页
         * desc 如果为空，就是正常的按照时间顺序返回视频发呢也查询列表
         * desc 不为空，则会进行关键词的搜素查找
         */
        List<VideosVO> list = videosMapperCustom.queryAllVideos(desc, userId);

        // 包装 page 对象，提供一些方便的方法
        PageInfo<VideosVO> pageList = new PageInfo<>(list);

        PagedResult pagedResult = new PagedResult();
        pagedResult.setPage(page);  // 当前页数
        pagedResult.setTotal(pageList.getPages()); // 总的页数
        pagedResult.setRows(list); // 查询出来的结果
        pagedResult.setRecords(pageList.getTotal()); // 总的记录条数
        return pagedResult;
    }

    /**
     * 分页查询该用户点赞（收藏）的视频
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedResult queryMyLikeVideos(String userId, Integer page, Integer pageSize) {

        // 开始分页查询
        PageHelper.startPage(page, pageSize);
        List<VideosVO> list = videosMapperCustom.queryMyLikeVideos(userId);

        // 包装 page 对象，提供一些方便的方法
        PageInfo<VideosVO> pageList = new PageInfo<>(list);

        PagedResult pagedResult = new PagedResult();
        pagedResult.setPage(page);  // 当前页数
        pagedResult.setTotal(pageList.getPages()); // 总的页数
        pagedResult.setRows(list); // 查询出来的结果
        pagedResult.setRecords(pageList.getTotal()); // 总的记录条数

        return pagedResult;
    }

    /**
     * 分页查询该用户该用户关注的人发布的视频列表
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedResult queryMyFollowVideos(String userId, Integer page, Integer pageSize) {

        // 开始分页查询
        PageHelper.startPage(page, pageSize);
        List<VideosVO> list = videosMapperCustom.queryMyFollowVideos(userId);

        // 包装 page 对象，提供一些方便的方法
        PageInfo<VideosVO> pageList = new PageInfo<>(list);

        PagedResult pagedResult = new PagedResult();
        pagedResult.setTotal(pageList.getPages());
        pagedResult.setRows(list);
        pagedResult.setPage(page);
        pagedResult.setRecords(pageList.getTotal());

        return pagedResult;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<String> getHotWords() {
        return searchRecordsMapper.getHotWords();
    }

    /**
     * @param userId 点击 Like 的用户 id
     * @param videoId 被点击 Like 的视频 id
     * @param videoCreaterId 视频创建者 id
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void userLikeVideo(String userId, String videoId, String videoCreaterId) {
        // 1. 更新关联表
        String likeId = sid.nextShort(); // 主键 id
        UsersLikeVideos ulv = new UsersLikeVideos();
        ulv.setId(likeId);
        ulv.setUserId(userId);
        ulv.setVideoId(videoId);
        usersLikeVideosMapper.insert(ulv);

        // 2. 视频受喜欢+1
        videosMapperCustom.addVideoLikeCount(videoId);

        // 3. 创建者受喜欢+1
        usersMapper.addReceiveLikeCount(videoCreaterId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void userUnLikeVideo(String userId, String videoId, String videoCreaterId) {
        // 1. 更新关联表，删除用户喜欢视频关系
        Example example = new Example(UsersLikeVideos.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId", userId)
                .andEqualTo("videoId", videoId);
        usersLikeVideosMapper.deleteByExample(example);

        // 2. 视频受喜欢-1
        videosMapperCustom.reduceVideoLikeCount(videoId);

        // 3. 创建者受喜欢-1
        usersMapper.reduceReceiveLikeCount(videoCreaterId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveComment(Comments comment) {
        String id = sid.nextShort();
        comment.setId(id);
        comment.setCreateTime(new Date());
        commentsMapper.insert(comment);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedResult getAllComments(String videoId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);

        List<CommentsVO> list = commentMapperCustom.queryComments(videoId);

        // 转换创建时间为 多少时间前 格式
        for (CommentsVO c : list) {
            String timeAgo = TimeAgoUtils.format(c.getCreateTime());
            c.setTimeAgoStr(timeAgo);
        }

        PageInfo<CommentsVO> pageList = new PageInfo<>(list);

        PagedResult grid = new PagedResult();
        grid.setTotal(pageList.getPages());
        grid.setRows(list);
        grid.setPage(page);
        grid.setRecords(pageList.getTotal());

        return grid;
    }
}
