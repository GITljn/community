package com.nowcoder.community.controller;


import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.event.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.HostHolder;
import com.nowcoder.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ljn
 * @since 2022-10-03
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
    @Autowired
    private CommentService commentService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    @LoginRequired
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        // 发送系统通知
        Event event = new Event();
        event.setTopic(TOPIC_COMMENT);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(comment.getEntityType());
        event.setEntityId(comment.getEntityId());
        event.setData("postId", discussPostId);
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost discussPost = discussPostService.getById(comment.getEntityId());
            event.setEntityUserId(Integer.parseInt(discussPost.getUserId()));
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Integer targetUserId = commentService.getById(comment.getEntityId()).getUserId();
            event.setEntityUserId(targetUserId);
        }
        eventProducer.fireEvent(event);

        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, discussPostId);
        }

        return "redirect:/discuss/detail/" + discussPostId;
    }
}

