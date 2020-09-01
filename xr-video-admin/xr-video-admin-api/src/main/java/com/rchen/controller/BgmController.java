package com.rchen.controller;

import com.rchen.pojo.Bgm;
import com.rchen.service.BgmService;
import com.rchen.utils.IMoocJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author : crz
 */
@Api(value = "BGM 管理业务", tags = {"BGM相关业务 controller"})
@RestController
@RequestMapping("/bgm")
public class BgmController extends BasicController {

    @Autowired
    private BgmService bgmService;


    @ApiOperation(value = "获取 BGM 列表", notes = "获取 BGM 列表的接口")
    @PostMapping("/list")
    public IMoocJSONResult list() {
        return IMoocJSONResult.ok(bgmService.queryBgmList());
    }

    @ApiOperation(value = "删除 BGM", notes = "删除 BGM 的接口")
    @ApiImplicitParam(name="bgmId", value="音乐id", required=true,
            dataType="String", paramType="query")
    @PostMapping("/delete")
    public IMoocJSONResult delete(String bgmId) throws IOException {
        if (StringUtils.isBlank(bgmId)) {
            return IMoocJSONResult.errorMsg("");
        }
        String dbPath = bgmService.deleteBgm(bgmId);
        // 磁盘删除文件
        File file = new File(FILE_SPACE + dbPath);
        FileUtils.forceDelete(file);
        return IMoocJSONResult.ok();
    }



    @ApiOperation(value = "上传 BGM", notes = "上传 BGM 的接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name="author", value="音乐作者", required=true,
                    dataType="String", paramType="form"),
            @ApiImplicitParam(name="name", value="音乐名称", required=true,
                    dataType="String", paramType="form"),
    })
    @PostMapping(value = "/upload")
    public IMoocJSONResult upload(String author, String name, @ApiParam(value="BGM", required=true) MultipartFile file) {

        // BGM存储路径前缀
        String bgmPathPrefix = "/bgm";

        // 数据库存储的相对路径
        String uploadPathDB = "";
        // 文件最终保存的路径
        String finalPath = "";

        // 1. 保存文件到服务器本地
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        try {
            if (file != null) {
                String fileName = file.getOriginalFilename();
                if (StringUtils.isNotBlank(fileName)) {
                    // 设置数据库保存的最终路径
                    uploadPathDB = bgmPathPrefix + "/" + fileName;
                    // 文件上传的最终保存路径
                    finalPath = FILE_SPACE + uploadPathDB;

                    File outFile = new File(finalPath);
                    // 如果父级目录不存在，则创建
                    if (outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
                        outFile.getParentFile().mkdirs();
                    }
                    // 保存文件
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
        Bgm bgm = new Bgm();
        bgm.setAuthor(author);
        bgm.setName(name);
        bgm.setPath(uploadPathDB);

        bgmService.saveBgm(bgm);
        return IMoocJSONResult.ok(uploadPathDB);
    }


}
