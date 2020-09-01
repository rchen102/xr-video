package com.rchen.controller;

import com.rchen.pojo.Users;
import com.rchen.pojo.UsersReport;
import com.rchen.pojo.vo.PublisherVideo;
import com.rchen.pojo.vo.UsersVO;
import com.rchen.service.UserService;
import com.rchen.utils.IMoocJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author : crz
 */
@Api(value = "用户相关业务接口", tags = {"用户相关业务 controller"})
@RestController
@RequestMapping("/user")
public class UserController extends BasicController {

    @Autowired
    private UserService userService;

    @ApiOperation(value = "获取用户信息", notes = "获取用户信息的接口")
    @ApiImplicitParam(name="userId", value="用户id", required=true,
            dataType="String", paramType="query")
    @PostMapping("/query")
    public IMoocJSONResult query(String userId, String fanId) throws Exception {
        if (StringUtils.isBlank(userId)) {
            return IMoocJSONResult.errorMsg("用户 id 不能为空");
        }

        Users userInfo = userService.queryUserInfo(userId);
        UsersVO userVO = new UsersVO();
        BeanUtils.copyProperties(userInfo, userVO);

        // 查询是否关注
        userVO.setFollow(userService.queryIfFollow(userId, fanId));


        return IMoocJSONResult.ok(userVO);
    }


    @ApiOperation(value = "上传头像", notes = "上传头像的接口")
    @ApiImplicitParam(name="userId", value="用户id", required=true,
            dataType="String", paramType="query")
    @PostMapping(value = "/uploadFace")
    public IMoocJSONResult uploadFace(String userId,
                                      @RequestParam("file") MultipartFile[] files) {
        if (StringUtils.isBlank(userId)) {
            return IMoocJSONResult.errorMsg("用户 id 不能为空");
        }
        // 头像存储路径前缀
        String facePathPrefix = "/" + userId + "/face";

        // 数据库存储的相对路径
        String uploadPathDB = "";
        // 文件最终保存的路径
        String finalFacePath = "";

        // 1. 保存文件到服务器本地
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        try {
            if (files != null && files.length > 0) {
                String fileName = files[0].getOriginalFilename();
                if (StringUtils.isNotBlank(fileName)) {
                    // 设置数据库保存的最终路径
                    uploadPathDB = facePathPrefix + "/" + fileName;
                    // 文件上传的最终保存路径
                    finalFacePath = FILE_SPACE + uploadPathDB;

                    File outFile = new File(finalFacePath);
                    // 如果父级目录不存在，则创建
                    if (outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
                        outFile.getParentFile().mkdirs();
                    }
                    // 保存头像文件
                    fileOutputStream = new FileOutputStream(outFile);
                    inputStream = files[0].getInputStream();
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
        Users user = new Users();
        user.setId(userId);
        user.setFaceImage(uploadPathDB);
        userService.updateUserInfo(user);

        return IMoocJSONResult.ok(uploadPathDB);
    }


    /**
     * 用户点击视频详情页时，拉取用户和视频之间相关的关系数据
     * 比如：是否点过赞？是否收藏？
     * @param loginUserId 当前登录用户 id
     * @param videoId 用户正在观看的视频 id
     * @param publishUserId 当前视频创建者 id
     * @return
     * @throws Exception
     */
    @PostMapping("/queryPublisher")
    public IMoocJSONResult queryPublisher(String loginUserId,
                                          String videoId, String publishUserId)  {
        if (StringUtils.isBlank(publishUserId)) {
            return IMoocJSONResult.errorMsg("错误：缺失必要信息");
        }

        // 1. 查询视频发布者的信息
        Users publishUserInfo = userService.queryUserInfo(publishUserId);
        UsersVO publisherVO = new UsersVO();
        BeanUtils.copyProperties(publishUserInfo, publisherVO);

        // 2. 查询当前登录者和视频的点赞关系
        boolean userLikeVideo = userService.isUserLikeVideo(loginUserId, videoId);

        PublisherVideo vo = new PublisherVideo();
        vo.setPublisher(publisherVO);
        vo.setUserLikeVideo(userLikeVideo);
        return IMoocJSONResult.ok(vo);
    }

    @PostMapping("/beyourfans")
    public IMoocJSONResult beyourfans(String userId, String fanId) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(fanId)) {
            return IMoocJSONResult.errorMsg("");
        }

        userService.saveUserFanRelation(userId, fanId);

        return IMoocJSONResult.ok("关注成功");
    }

    @PostMapping("/dontbeyourfans")
    public IMoocJSONResult dontbeyourfans(String userId, String fanId) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(fanId)) {
            return IMoocJSONResult.errorMsg("");
        }

        userService.deleteUserFanRelation(userId, fanId);

        return IMoocJSONResult.ok("取消关注成功");
    }

    @PostMapping("/reportUser")
    public IMoocJSONResult reportUser(@RequestBody UsersReport usersReport) {
        userService.reportUser(usersReport);
        return IMoocJSONResult.ok("举报成功");
    }
}
