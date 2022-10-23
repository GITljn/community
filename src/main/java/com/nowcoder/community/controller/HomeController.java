package com.nowcoder.community.controller;

import com.nowcoder.community.service.HomeService;
import com.nowcoder.community.utils.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    private HomeService homeService;

    @GetMapping("/index")
    public String queryIndexPage(Model model, PageInfo pageInfo,
                                 @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        Map<String, Object> map = homeService.queryIndexPage(pageInfo, orderMode);
        model.addAttribute("discussPostList", map.get("discussPostList"));
        model.addAttribute("pageInfo", map.get("pageInfo"));
        return "/index";
    }

    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }

    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }
}
