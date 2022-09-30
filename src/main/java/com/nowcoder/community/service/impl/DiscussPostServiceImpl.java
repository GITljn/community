package com.nowcoder.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.mapper.DiscussPostMapper;
import com.nowcoder.community.service.DiscussPostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
}
