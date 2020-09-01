package com.rchen.controller;

import com.rchen.service.BgmService;
import com.rchen.utils.IMoocJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author : crz
 */
@Api(value = "BGM 相关业务接口", tags = {"BGM 相关业务 controller"})
@RestController
@RequestMapping("/bgm")
public class BgmController {

    @Autowired
    private BgmService bgmService;

    @ApiOperation(value = "获取背景音乐列表", notes = "获取背景音乐列表的接口")
    @PostMapping("/list")
    public IMoocJSONResult list() {
        return IMoocJSONResult.ok(bgmService.queryBgmList());
    }
}
