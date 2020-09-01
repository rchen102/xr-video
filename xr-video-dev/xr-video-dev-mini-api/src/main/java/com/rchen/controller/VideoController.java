package com.rchen.controller;

import com.rchen.enums.VideoStatusEnum;
import com.rchen.pojo.Bgm;
import com.rchen.pojo.Comments;
import com.rchen.pojo.Videos;
import com.rchen.service.BgmService;
import com.rchen.service.VideoService;
import com.rchen.utils.FileUtils;
import com.rchen.utils.IMoocJSONResult;
import com.rchen.utils.PagedResult;
import com.rchen.utils.VideoUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

/**
 * @Author : crz
 */
@Api(value = "Video 相关业务接口", tags = {"Video 相关业务 controller"})
@RestController
@RequestMapping("/video")
public class VideoController extends BasicController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private BgmService bgmService;

    @PostMapping(value = "/delete")
    public IMoocJSONResult delete(String videoId, String creatorId) {
        if (StringUtils.isBlank(videoId) || StringUtils.isBlank(creatorId)) {
            return IMoocJSONResult.errorMsg("");
        }
        Videos video = videoService.delete(videoId, creatorId);
        String videoPath = FILE_SPACE + video.getVideoPath();
        String coverPath = FILE_SPACE + video.getCoverPath();
        if (!FileUtils.deleteFile(videoPath) || !FileUtils.deleteFile(coverPath)) {
            System.out.println("磁盘删除失败");
        }
        return IMoocJSONResult.ok();
    }

    @ApiOperation(value = "上传短视频", notes = "上传短视频的接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name="userId", value="用户id", required=true,
                    dataType="String", paramType="form"),
            @ApiImplicitParam(name="bgmId", value="背景音乐id", required=false,
                    dataType="String", paramType="form"),
            @ApiImplicitParam(name="videoSeconds", value="背景音乐播放长度", required=true,
                    dataType="String", paramType="form"),
            @ApiImplicitParam(name="videoWidth", value="视频宽度", required=true,
                    dataType="String", paramType="form"),
            @ApiImplicitParam(name="videoHeight", value="视频高度", required=true,
                    dataType="String", paramType="form"),
            @ApiImplicitParam(name="desc", value="视频描述", required=false,
                    dataType="String", paramType="form")
    })
    @PostMapping(value = "/upload", headers = "content-type=multipart/form-data")
    public IMoocJSONResult upload(String userId,
                                      String bgmId,
                                      double videoSeconds,
                                      int videoWidth,
                                      int videoHeight,
                                      String desc,
                                      @ApiParam(value="短视频", required=true) MultipartFile file) {

        if (StringUtils.isBlank(userId)) {
            return IMoocJSONResult.errorMsg("用户 id 不能为空");
        }
        // 视频文件的目录前缀
        String videoPathPrefix = "/" + userId + "/video";
        // 数据库存储的相对路径
        String uploadPathDB = "";
        // 文件最终保存的绝对路径
        String finalVideoPath = "";

        // 1. 保存文件到服务器本地
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        try {
            if (file != null) {
                String fileName = file.getOriginalFilename();
                if (StringUtils.isNotBlank(fileName)) {
                    // 设置数据库保存的最终路径
                    uploadPathDB = videoPathPrefix + "/" + fileName;
                    // 文件上传的最终保存路径
                    finalVideoPath = FILE_SPACE + uploadPathDB;

                    File outFile = new File(finalVideoPath);
                    if (outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
                        // 创建父级文件夹
                        outFile.getParentFile().mkdirs();
                    }
                    fileOutputStream = new FileOutputStream(outFile);
                    inputStream = file.getInputStream();
                    IOUtils.copy(inputStream, fileOutputStream);
                } else {
                    return IMoocJSONResult.errorMsg("上传出现错误，请稍后重试");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return IMoocJSONResult.errorMsg("上传出现错误，请稍后重试");
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 2. 判断是否需要合并 BGM（通过 bgmId）
        VideoUtils tool = new VideoUtils(FFMPEG_EXE);
        if (StringUtils.isNotBlank(bgmId)) {
            Bgm bgm = bgmService.queryBgmById(bgmId);
            String bgmInputPath = FILE_SPACE + bgm.getPath(); // 数据库中存的是相对路径，需要和命名空间拼接
            String videoInputPath = finalVideoPath;
            String videoOuputName = UUID.randomUUID().toString() + ".mp4";
            // 更新数据库相对地址和文件存储的最终地址
            uploadPathDB = videoPathPrefix + "/" + videoOuputName;
            finalVideoPath = FILE_SPACE + uploadPathDB;

            // 执行 视频-BGM 拼接合并
            tool.mergeVideoAudio(videoInputPath, bgmInputPath, videoSeconds, finalVideoPath);
        }

        // 3. 抽取视频第一秒截图作为封面
        String coverPathPrefix = "/" + userId + "/video";
        String coverName = UUID.randomUUID().toString() + ".jpg";
        String coverPathDB = coverPathPrefix +  "/" + coverName;
        tool.getCover(finalVideoPath, FILE_SPACE + coverPathDB);


        // 3. 更新数据库
        Videos video = new Videos();
        video.setAudioId(bgmId);
        video.setUserId(userId);
        video.setVideoSeconds((float)videoSeconds); // 数据库中设置的类型是 float
        video.setVideoHeight(videoHeight);
        video.setVideoWidth(videoWidth);
        video.setVideoDesc(desc);
        video.setVideoPath(uploadPathDB);
        video.setCoverPath(coverPathDB);
        video.setStatus(VideoStatusEnum.SUCCESS.value);
        video.setCreateTime(new Date());

        String videoId = videoService.saveVideo(video);
        return IMoocJSONResult.ok(videoId);
    }

    @ApiOperation(value="上传封面", notes="上传封面的接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name="userId", value="用户id", required=true,
                    dataType="String", paramType="form"),
            @ApiImplicitParam(name="videoId", value="视频id", required=true,
                    dataType="String", paramType="form")
    })
    @PostMapping(value="/uploadCover", headers="content-type=multipart/form-data")
    public IMoocJSONResult uploadCover(String userId,
                                       String videoId,
                                       @ApiParam(value="视频封面", required=true)
                                               MultipartFile file) throws Exception {
        if (StringUtils.isBlank(videoId) || StringUtils.isBlank(userId)) {
            return IMoocJSONResult.errorMsg("视频主键id和用户id不能为空...");
        }

        // 视频封面图片的目录前缀
        String coverPathPrefix = "/" + userId + "/video";
        // 数据库存储的相对路径
        String uploadPathDB = "";
        // 文件最终保存的绝对路径
        String finalCoverPath = "";

        // 1. 保存文件到服务器本地
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        try {
            if (file != null) {
                String fileName = file.getOriginalFilename();
                if (StringUtils.isNotBlank(fileName)) {
                    // 设置数据库保存的最终路径
                    uploadPathDB = coverPathPrefix + "/" + fileName;
                    // 文件上传的最终保存路径
                    finalCoverPath = FILE_SPACE + uploadPathDB;

                    File outFile = new File(finalCoverPath);
                    if (outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
                        // 创建父级文件夹
                        outFile.getParentFile().mkdirs();
                    }
                    fileOutputStream = new FileOutputStream(outFile);
                    inputStream = file.getInputStream();
                    IOUtils.copy(inputStream, fileOutputStream);
                } else {
                    return IMoocJSONResult.errorMsg("上传出现错误，请稍后重试");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return IMoocJSONResult.errorMsg("上传出现错误，请稍后重试");
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 2. 更新数据库
        videoService.updateVideoCover(videoId, uploadPathDB);
        return IMoocJSONResult.ok();
    }

    /**
     * 1. 用于首页视频查询，查询当前最新的视频，返回分页结果
     * 2. 用于视频搜索，根据关键词 desc，查询相关视频，返回分页结果，并保存搜索关键词
     * 3. 用于个人所有作品查询，根据 userId，查询最新的作品，返回分页结果
     *
     * @param video 主要用于传递 desc 和 userId 两个参数，实际上与 video 没有直接关系
     * @param isSaveRecord 是否保存搜索关键词，如果是 1 则需要保存，0 或者 null 则不需要
     * @param page 当前页数
     * @param pageSize 每页大小，默认 5 条
     * @return
     * @throws Exception
     */
    @PostMapping(value="/showAll")
    public IMoocJSONResult showAll(@RequestBody Videos video, Integer isSaveRecord,
                                   Integer page, Integer pageSize) {

        if (page == null) {
            page = 1;
        }

        if (pageSize == null) {
            pageSize = PAGE_SIZE;
        }

        PagedResult result = videoService.getAllVideos(video, isSaveRecord, page, pageSize);
        return IMoocJSONResult.ok(result);
    }


    /**
     * 分页查询，用户点赞过的视频列表（收藏=点赞=喜欢）
     * @param userId
     * @param page
     * @param pageSize 每页的大小，默认 6 条
     * @return
     */
    @PostMapping("/showMyLike")
    public IMoocJSONResult showMyLike(String userId, Integer page, Integer pageSize) {

        if (StringUtils.isBlank(userId)) {
            return IMoocJSONResult.ok();
        }

        if (page == null) {
            page = 1;
        }

        if (pageSize == null) {
            pageSize = 6;
        }

        PagedResult videosList = videoService.queryMyLikeVideos(userId, page, pageSize);

        return IMoocJSONResult.ok(videosList);
    }

    /**
     * 分页查询，用户关注者发布的视频列表
     * @param userId
     * @param page
     * @param pageSize 每页的大小，默认 6 条
     * @return
     */
    @PostMapping("/showMyFollow")
    public IMoocJSONResult showMyFollow(String userId, Integer page, Integer pageSize) {
        if (StringUtils.isBlank(userId)) {
            return IMoocJSONResult.ok();
        }

        if (page == null) {
            page = 1;
        }

        if (pageSize == null) {
            pageSize = 6;
        }

        PagedResult videosList = videoService.queryMyFollowVideos(userId, page, pageSize);
        return IMoocJSONResult.ok(videosList);
    }

    /**
     * 返回当前热搜关键词列表
     * @return
     * @throws Exception
     */
    @PostMapping(value="/hot")
    public IMoocJSONResult hot() throws Exception {
        return IMoocJSONResult.ok(videoService.getHotWords());
    }

    @PostMapping(value="/userLike")
    public IMoocJSONResult userLike(String userId, String videoId, String videoCreaterId)
            throws Exception {
        videoService.userLikeVideo(userId, videoId, videoCreaterId);
        return IMoocJSONResult.ok();
    }

    @PostMapping(value="/userUnLike")
    public IMoocJSONResult userUnLike(String userId, String videoId, String videoCreaterId) throws Exception {
        videoService.userUnLikeVideo(userId, videoId, videoCreaterId);
        return IMoocJSONResult.ok();
    }

    @PostMapping(value="/saveComment")
    public IMoocJSONResult saveComment(@RequestBody Comments comment,
                                       String fatherCommentId, String toUserId) {
        if (StringUtils.isNotBlank(fatherCommentId) && StringUtils.isNotBlank(toUserId)) {
            comment.setFatherCommentId(fatherCommentId);
            comment.setToUserId(toUserId);
        }
        videoService.saveComment(comment);
        return IMoocJSONResult.ok();
    }

    /**
     * 分页查询视频的所有评论
     * @param videoId
     * @param page 默认 1
     * @param pageSize 默认 10
     * @return
     * @throws Exception
     */
    @PostMapping("/getVideoComments")
    public IMoocJSONResult getVideoComments(String videoId, Integer page, Integer pageSize) throws Exception {

        if (StringUtils.isBlank(videoId)) {
            return IMoocJSONResult.ok();
        }

        // 分页查询视频列表，时间顺序倒序排序
        if (page == null) {
            page = 1;
        }

        if (pageSize == null) {
            pageSize = 10;
        }

        PagedResult list = videoService.getAllComments(videoId, page, pageSize);

        return IMoocJSONResult.ok(list);
    }
}
