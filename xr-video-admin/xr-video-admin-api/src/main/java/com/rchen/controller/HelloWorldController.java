package com.rchen.controller;

import com.rchen.mapper.AdminMapper;
import com.rchen.pojo.Admin;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author : crz
 */
@RestController
public class HelloWorldController {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private Sid sid;

    @RequestMapping("/hello")
    public String hello() {
        Admin admin = new Admin();
        admin.setId(sid.nextShort());
        admin.setUsername("rchen102");
        admin.setPassword("123");
        admin.setNickname("rchen");
        adminMapper.insert(admin);
        return "Hello, World!";
    }
}
