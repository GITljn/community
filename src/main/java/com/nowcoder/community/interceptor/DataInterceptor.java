package com.nowcoder.community.interceptor;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DataService;
import com.nowcoder.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataInterceptor implements HandlerInterceptor {

    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = request.getRemoteHost();
        dataService.saveUV(ip);

        User user = hostHolder.getUser();
        if (user != null) {
            dataService.saveDAU(user.getId());
        }

        return true;
    }
}
