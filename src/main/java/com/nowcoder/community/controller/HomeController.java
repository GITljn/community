package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.HomeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    private HomeService homeService;

    @GetMapping("/index")
    public String queryIndexPage(Model model, PageInfo pageInfo) {
        Map<String, Object> map = homeService.queryIndexPage(pageInfo);
        model.addAttribute("discussPostList", map.get("discussPostList"));
        model.addAttribute("pageInfo", map.get("pageInfo"));
        return "/index";
    }
}
