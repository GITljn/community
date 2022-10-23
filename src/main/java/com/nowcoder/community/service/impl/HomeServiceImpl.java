package com.nowcoder.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.HomeService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class HomeServiceImpl implements HomeService, CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(HomeServiceImpl.class);

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;

    // Caffeine核心接口: Cache
    // Cache有两个子接口: LoadingCache, AsyncLoadingCache
    // 缓存帖子列表
    private LoadingCache<String, List<DiscussPost>> postListCache;
    // 缓存帖子总数
    private LoadingCache<Integer, Integer> postSumCache;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    @PostConstruct
    public void init() {
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception {
                        if (StringUtils.isBlank(key)) {
                            throw new IllegalArgumentException("参数不能为空");
                        }
                        String[] params = key.split(":");
                        if (params.length != 2) {
                            throw new IllegalArgumentException("参数错误!");
                        }
                        PageInfo pageInfo = new PageInfo();
                        pageInfo.setCurrent(Integer.parseInt(params[0]));
                        pageInfo.setSize(Integer.parseInt(params[1]));
                        logger.info("load data from db");
                        return getDiscussPostList(pageInfo, 1);
                    }
                });
    }

    @Override
    public Map<String, Object> queryIndexPage(PageInfo pageInfo, int orderMode) {
        List<DiscussPost> discussPosts;
        if (orderMode == 1) {
            discussPosts = postListCache.get(pageInfo.getCurrent()+":"+ pageInfo.getSize());
//            discussPosts = getDiscussPostList(pageInfo, orderMode);
        } else {
            discussPosts = getDiscussPostList(pageInfo, orderMode);
        }

        pageInfo.setPath("/index");
        pageInfo.setTotalRows(discussPostService.count());
        List<Map<String, Object>> discussPostList = new ArrayList<>();
        for (DiscussPost discussPost : discussPosts) {
            String userId = discussPost.getUserId();
            User user = userService.getById(userId);
            Map<String, Object> map = new HashMap<>();
            map.put("discussPost", discussPost);
            map.put("user", user);
            long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
            map.put("likeCount", likeCount);
            discussPostList.add(map);
        }
        Map<String, Object> results = new HashMap<>();
        results.put("discussPostList", discussPostList);
        results.put("pageInfo", pageInfo);
        return results;
    }

    private List<DiscussPost> getDiscussPostList(PageInfo pageInfo, int orderMode) {
        Page<DiscussPost> discussPostPage = new Page<>(pageInfo.getCurrent(), pageInfo.getSize());
        LambdaQueryWrapper<DiscussPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(DiscussPost::getStatus, 2);
        if (orderMode == 0)
            wrapper.orderByDesc(DiscussPost::getType, DiscussPost::getCreateTime);
        else
            wrapper.orderByDesc(DiscussPost::getType, DiscussPost::getScore, DiscussPost::getCreateTime);
        discussPostService.page(discussPostPage, wrapper);
        logger.info("load data from db.");
        return discussPostPage.getRecords();
    }
}
