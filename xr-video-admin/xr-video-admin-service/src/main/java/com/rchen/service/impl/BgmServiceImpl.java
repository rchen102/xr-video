package com.rchen.service.impl;

import com.rchen.enums.BGMOperatorTypeEnum;
import com.rchen.mapper.BgmMapper;
import com.rchen.pojo.Bgm;
import com.rchen.service.BgmService;
import com.rchen.util.ZKCuratorClient;
import com.rchen.utils.JsonUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author : crz
 */
@Service
public class BgmServiceImpl implements BgmService {

    @Autowired
    private BgmMapper bgmMapper;

    @Autowired
    private Sid sid;

    @Autowired
    private ZKCuratorClient zkClient;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<Bgm> queryBgmList() {
        return bgmMapper.selectAll();
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Bgm queryBgmById(String bgmId) {
        return bgmMapper.selectByPrimaryKey(bgmId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveBgm(Bgm bgm) {
        String id = sid.nextShort();
        bgm.setId(id);
        bgmMapper.insert(bgm);
        // 构造数据对象，将被存储在 zknode
        Map<String, String> payload = new HashMap<>();
        payload.put("op", BGMOperatorTypeEnum.ADD.type);
        payload.put("path", bgm.getPath());
        zkClient.sendBgmOperator(id, JsonUtils.objectToJson(payload));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public String deleteBgm(String bgmId) {
        Example example = new Example(Bgm.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", bgmId);
        Bgm bgm = bgmMapper.selectOneByExample(example);
        bgmMapper.deleteByExample(example);

        // 构造数据对象，将被存储在 zknode
        Map<String, String> payload = new HashMap<>();
        payload.put("op", BGMOperatorTypeEnum.DELETE.type);
        payload.put("path", bgm.getPath());
        zkClient.sendBgmOperator(bgmId, JsonUtils.objectToJson(payload));
        return bgm.getPath();
    }
}
