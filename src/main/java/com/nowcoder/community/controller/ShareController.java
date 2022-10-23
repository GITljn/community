package com.nowcoder.community.controller;

import com.nowcoder.community.event.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ShareController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    @Autowired
    private EventProducer eventProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${wk.image.storage}")
    private String imgStoragePath;

    @Value("${qiniu.bucket.share.url}")
    private String shareBucketUrl;

    @GetMapping("/share")
    @ResponseBody
    public String share(String htmlUrl) {
        String fileName = CommunityUtil.generateUUID();

        Event event = new Event();
        event.setTopic(TOPIC_SHARE);
        event.setData("fileName", fileName)
                .setData("htmlUrl", htmlUrl);
        event.setData("suffix", ".png");
        eventProducer.fireEvent(event);

        // 返回访问路径
        Map<String, Object> map = new HashMap<>();
//        map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);
        map.put("shareUrl", shareBucketUrl + "/" + fileName + ".png");

        return CommunityUtil.getJSONString(0, null, map);
    }

    @Deprecated
    @GetMapping("/share/image/{fileName}")
    public void getShareImg(@PathVariable("fileName") String fileName,
                            HttpServletResponse response) {
        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        response.setContentType("image/png");
        File file = new File(imgStoragePath+"/"+fileName+".png");
        try(FileInputStream fis = new FileInputStream(file)) {
            byte[] buf = new byte[1024];
            int len;
            OutputStream os = response.getOutputStream();
            while ((len = fis.read(buf)) != -1) {
                os.write(buf, 0, len);
            }
        } catch (IOException e) {
            logger.error("获取长图失败: " + e.getMessage());
        }
    }
}
