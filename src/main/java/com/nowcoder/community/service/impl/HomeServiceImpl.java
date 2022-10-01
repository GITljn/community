package com.nowcoder.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.HomeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HomeServiceImpl implements HomeService {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;

    @Override
    public Map<String, Object> queryIndexPage(PageInfo pageInfo) {
        Page<DiscussPost> discussPostPage = new Page<>(pageInfo.getCurrent(), pageInfo.getSize());
        LambdaQueryWrapper<DiscussPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(DiscussPost::getStatus, 2);
        wrapper.orderByDesc(DiscussPost::getScore, DiscussPost::getCreateTime);
        discussPostService.page(discussPostPage, wrapper);
        pageInfo.setTotalRows((int)discussPostPage.getTotal());
        pageInfo.setPath("/index");
        List<DiscussPost> discussPosts = discussPostPage.getRecords();
        List<Map<String, Object>> discussPostList = new ArrayList<>();
        for (DiscussPost discussPost : discussPosts) {
            String userId = discussPost.getUserId();
            User user = userService.queryById(Integer.parseInt(userId));
            Map<String, Object> map = new HashMap<>();
            map.put("discussPost", discussPost);
            map.put("user", user);
            discussPostList.add(map);
        }
        Map<String, Object> results = new HashMap<>();
        results.put("discussPostList", discussPostList);
        results.put("pageInfo", pageInfo);
        return results;
    }
}
