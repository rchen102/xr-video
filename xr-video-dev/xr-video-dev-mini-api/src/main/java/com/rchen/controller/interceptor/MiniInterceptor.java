package com.rchen.controller.interceptor;

import com.rchen.utils.IMoocJSONResult;
import com.rchen.utils.JsonUtils;
import com.rchen.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * 拦截器
 *
 * @Author : crz
 */
public class MiniInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisOperator redis;
    private static final String USER_REDIS_SESSION = "user-redis-session";

    /**
     * 拦截请求，在到达 controller 之前，被调用
     *
     * @param request
     * @param response
     * @param o
     * @return fals: 请求被拦截; true: 请求放行
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {

        String userId = request.getHeader("userId");
        String userToken = request.getHeader("userToken");

        if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userToken)) {
            String uniqueToken = redis.get(USER_REDIS_SESSION + ":" + userId);
            if (StringUtils.isEmpty(uniqueToken) && StringUtils.isBlank(uniqueToken)) {
                returnErrorResponse(response, new IMoocJSONResult().errorTokenMsg("登录已过期"));
                return false;
            } else {
                if (!uniqueToken.equals(userToken)) {
                    returnErrorResponse(response, new IMoocJSONResult().errorTokenMsg("其他设备已登录"));
                    return false;
                }
            }
        } else {
            returnErrorResponse(response,
                    new IMoocJSONResult().errorTokenMsg("未登录"));
            return false;
        }
        return true;
    }

    /**
     * 发送错误信息，返回
     * @param response
     * @param result
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public void returnErrorResponse(HttpServletResponse response, IMoocJSONResult result)
            throws IOException, UnsupportedEncodingException {
        OutputStream out=null;
        try{
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/json");
            out = response.getOutputStream();
            out.write(JsonUtils.objectToJson(result).getBytes("utf-8"));
            out.flush();
        } finally{
            if(out!=null){
                out.close();
            }
        }
    }


    /**
     * 请求被 controller 处理之后，视图渲染之前（如页面跳转），被调用
     * @param httpServletRequest
     * @param httpServletResponse
     * @param o
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    /**
     * 整个请求处理完毕，视图渲染完毕之后，被调用
     * @param httpServletRequest
     * @param httpServletResponse
     * @param o
     * @param e
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
