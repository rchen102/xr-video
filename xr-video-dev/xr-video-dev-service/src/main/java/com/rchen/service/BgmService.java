package com.rchen.service;

import com.rchen.pojo.Bgm;

import java.util.List;

/**
 * @Author : crz
 */
public interface BgmService {
    /**
     * 查询背景音乐列表
     * @return
     */
    List<Bgm> queryBgmList();

    /**
     * 根据 id 查询 BGM 信息
     * @param bgmId
     * @return
     */
    Bgm queryBgmById(String bgmId);
}
