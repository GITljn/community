package com.nowcoder.community.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.SearchService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class EventConsumer implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Value("${wk.image.command}")
    private String command;

    @Value("${wk.image.storage}")
    private String imgStoragePath;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleMessage(ConsumerRecord record) {
        if (record == null || record.value()==null) {
            logger.error("消息不可以为空");
            return;
        }
        Event event = JSON.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
            content.put(entry.getKey(), entry.getValue());
        }
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setContent(JSON.toJSONString(content));
        message.setStatus(0);
        message.setCreateTime(new Date());

        messageService.save(message);
    }

    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null || record.value()==null) {
            logger.error("消息不可以为空");
            return;
        }
        Event event = JSON.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }
        DiscussPost discussPost = discussPostService.getById(event.getEntityId());
        searchService.saveDiscussPost(discussPost);
    }

    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShareMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息不可以为空");
            return;
        }
        Event event = JSON.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        String fileName = (String) event.getData().get("fileName");
        String htmlUrl = (String) event.getData().get("htmlUrl");
        String suffix = (String) event.getData().get("suffix");

        String cmd = command + " --quality 75 " + htmlUrl + " " + imgStoragePath + "/" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("保存长图成功: " + cmd);
        } catch (IOException e) {
            logger.error("保存长图失败: " + e.getMessage());
        }

        UploadImgToQiniuyunTask task = new UploadImgToQiniuyunTask(fileName, suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task, 500);
        task.setFuture(future);
    }

    class UploadImgToQiniuyunTask implements Runnable {
        private String fileName;
        private String suffix;

        private int uploadTimes;
        private long startTime;

        private Future future;

        public UploadImgToQiniuyunTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        @Override
        public void run() {
            // 图片生成失败
            if (System.currentTimeMillis()-startTime > 1000*30) {
                logger.error("执行时间过长,终止任务:" + fileName);
                future.cancel(true);
                return;
            }
            // 图片上传失败
            if (uploadTimes > 3000) {
                logger.error("上传次数过多,终止任务:" + fileName);
                future.cancel(true);
                return;
            }

            String path = imgStoragePath + "/" + fileName + suffix;
            File file = new File(path);
            if (file.exists()) {
                logger.info(String.format("开始第%d次上传[%s].", ++uploadTimes, fileName));

                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                // 指定上传机房
                UploadManager manager = new UploadManager(new Configuration(Zone.zone2()));
                try {
                    Response response = manager.put(path, fileName+suffix, uploadToken, null, "image/" + suffix, false);
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")) {
                        logger.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
                    } else {
                        logger.info(String.format("第%d次上传成功[%s].", uploadTimes, fileName));
                        future.cancel(true);
                    }
                } catch (QiniuException e) {
                    logger.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
                }
            } else {
                logger.info("等待图片生成[" + fileName + "].");
            }
        }
    }
}
