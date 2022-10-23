package com.nowcoder.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.event.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.mapper.CommentMapper;
import com.nowcoder.community.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.HostHolder;
import com.nowcoder.community.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ljn
 * @since 2022-10-03
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService, CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Transactional
    @Override
    public void addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        baseMapper.insert(comment);

        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Comment::getEntityType, ENTITY_TYPE_POST);
            wrapper.eq(Comment::getEntityId, comment.getEntityId());
            Integer count = baseMapper.selectCount(wrapper);
            LambdaUpdateWrapper<DiscussPost> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(DiscussPost::getId, comment.getEntityId());
            updateWrapper.set(DiscussPost::getCommentCount, count);
            discussPostService.update(updateWrapper);
        }
    }
}
