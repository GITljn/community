package com.nowcoder.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.mapper.DiscussPostMapper;
import com.nowcoder.community.service.DiscussPostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ljn
 * @since 2022-09-29
 */
@Service
public class DiscussPostServiceImpl extends ServiceImpl<DiscussPostMapper, DiscussPost> implements DiscussPostService {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Override
    public List<DiscussPost> queryDiscussPosts(int userId, int page, int size) {
        Page<DiscussPost> discussPostPage = new Page<>(page, size);
        LambdaQueryWrapper<DiscussPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != 0, DiscussPost::getUserId, userId);
        wrapper.ne(DiscussPost::getStatus, 2);
        wrapper.orderByDesc(DiscussPost::getScore, DiscussPost::getCreateTime);
        baseMapper.selectPage(discussPostPage, wrapper);
        return discussPostPage.getRecords();
    }

    @Override
    public int queryDiscussPostRows(int userId) {
        LambdaQueryWrapper<DiscussPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != 0, DiscussPost::getUserId, userId);
        wrapper.ne(DiscussPost::getStatus, 2);
        return baseMapper.selectCount(wrapper);
    }

    @Override
    public void addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // 过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        post.setType(0);
        post.setStatus(0);
        baseMapper.insert(post);
    }

    @Override
    public void updateType(int id, int i) {
        LambdaUpdateWrapper<DiscussPost> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(DiscussPost::getId, id);
        wrapper.set(DiscussPost::getType, i);
        baseMapper.update(null, wrapper);
    }

    @Override
    public void updateStatus(int id, int i) {
        LambdaUpdateWrapper<DiscussPost> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(DiscussPost::getId, id);
        wrapper.set(DiscussPost::getStatus, i);
        baseMapper.update(null, wrapper);
    }
}
