package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.SearchService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {
    @Autowired
    private SearchService searchService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;

    @GetMapping("/search")
    public String searchPost(String keyword, PageInfo pageInfo, Model model) {
        if (StringUtils.hasText(keyword)) {
            Page<DiscussPost> postList = searchService.searchDiscussPost(keyword, pageInfo.getCurrent()-1, pageInfo.getSize());
            List<Map<String, Object>> postVoList = new ArrayList<>();
            for (DiscussPost post : postList) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                map.put("user", userService.queryById(Integer.parseInt(post.getUserId())));
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                postVoList.add(map);
            }
            model.addAttribute("discussPosts", postVoList);
            model.addAttribute("keyword", keyword);

            // 分页信息
            pageInfo.setPath("/search?keyword=" + keyword);
            pageInfo.setTotalRows(postList == null ? 0 : (int) postList.getTotalElements());
        }
        return "/site/search";
    }
}
