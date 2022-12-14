package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.service.impl.FollowService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.HostHolder;
import com.nowcoder.community.utils.UUIDUtil;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${quniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(Model model) {
        String fileName = CommunityUtil.generateUUID();

        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));

        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);

        return "/site/setting";
    }

    @PostMapping("/header/url")
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1, "?????????????????????!");
        }
        String headerUrl = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), headerUrl);

        return CommunityUtil.getJSONString(0);
    }

    @Deprecated
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage.isEmpty()) {
            model.addAttribute("error", "????????????????????????!");
            return "/site/setting";
        }
        String originalFilename = headerImage.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "????????????????????????!");
            return "/site/setting";
        }
        String newFileName = UUIDUtil.uuid()+suffix;
        File dest = new File(uploadPath+ "/" + newFileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("??????????????????: " + e.getMessage());
            throw new RuntimeException("??????????????????,?????????????????????!", e);
        }
        User user = hostHolder.getUser();
        // http://localhost:8080/community/user/getHeaderImage/newFileName;
        userService.updateHeader(user.getId(), domain+ contextPath + "/user/getHeaderImage/"+newFileName);
        return "redirect:/index";
    }

    @Deprecated
    @GetMapping("/getHeaderImage/{fileName}")
    public void getHeaderImage(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // ?????????????????????
        fileName = uploadPath + "/" + fileName;
        // ????????????
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        // ????????????
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                ServletOutputStream os = response.getOutputStream()
        ) {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            logger.error("??????????????????: " + e.getMessage());
        }
    }

    // ????????????
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.queryById(userId);
        if (user == null) {
            throw new RuntimeException("??????????????????!");
        }

        // ??????
        model.addAttribute("user", user);
        // ????????????
        long likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        // ????????????
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // ????????????
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        boolean hasFollowed = hostHolder.getUser() != null && followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }
}
