package com.nowcoder.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.mapper.MessageMapper;
import com.nowcoder.community.service.MessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.utils.PageInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ljn
 * @since 2022-10-04
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public List<Message> findConversations(int userId, PageInfo pageInfo) {
        Page<Message> page = new Page<>(pageInfo.getCurrent(), pageInfo.getSize());
        LambdaQueryWrapper<Message> queryWrapper = new QueryWrapper<Message>().select("max(id) id").lambda();
        queryWrapper.like(Message::getConversationId, userId);
        queryWrapper.ne(Message::getStatus, 2);
        queryWrapper.ne(Message::getFromId, 1);
        queryWrapper.groupBy(Message::getConversationId);
        List<Object> idList = baseMapper.selectObjs(queryWrapper);
        if (idList.isEmpty()) {
            pageInfo.setTotalRows(0);
            return new ArrayList<>();
        }
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Message::getId, idList);
        queryWrapper.orderByDesc(Message::getId);
        baseMapper.selectPage(page, queryWrapper);
        pageInfo.setTotalRows((int) page.getTotal());
        return page.getRecords();
    }

    @Override
    public int findConversationCount(int userId) {
        LambdaQueryWrapper<Message> queryWrapper = new QueryWrapper<Message>().select("DISTINCT conversation_id").lambda();
        queryWrapper.like(Message::getConversationId, userId);
        queryWrapper.ne(Message::getStatus, 2);
        queryWrapper.ne(Message::getFromId, 1);
        return baseMapper.selectCount(queryWrapper);
    }

    @Override
    public List<Message> findLetters(String conversationsId, PageInfo pageInfo) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getConversationId, conversationsId);
        queryWrapper.ne(Message::getStatus, 2);
        queryWrapper.ne(Message::getFromId, 1);
        queryWrapper.orderByDesc(Message::getId);
        Page<Message> page = new Page<>(pageInfo.getCurrent(), pageInfo.getSize());
        baseMapper.selectPage(page, queryWrapper);
        pageInfo.setTotalRows((int) page.getTotal());
        return page.getRecords();
    }

    @Override
    public int findLetterCount(String conversationsId) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getConversationId, conversationsId);
        queryWrapper.ne(Message::getStatus, 2);
        queryWrapper.ne(Message::getFromId, 1);
        return baseMapper.selectCount(queryWrapper);
    }

    @Override
    public int findLetterUnreadCount(int userId, String conversationsId) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(conversationsId != null, Message::getConversationId, conversationsId);
        queryWrapper.eq(Message::getToId, userId);
        queryWrapper.eq(Message::getStatus, 0);
        queryWrapper.ne(Message::getFromId, 1);
        return baseMapper.selectCount(queryWrapper);
    }

    @Override
    public void readMessage(List<Integer> ids) {
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Message::getId, ids);
        wrapper.set(Message::getStatus, 1);
        baseMapper.update(null, wrapper);
    }

    @Override
    public Message findLatestNotice(int userId, String topic) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getFromId, 1);
        wrapper.eq(Message::getToId, userId);
        wrapper.eq(Message::getConversationId, topic);
        wrapper.ne(Message::getStatus, 2);
        wrapper.orderByDesc(Message::getCreateTime);
        List<Message> messageList = baseMapper.selectList(wrapper);
        return messageList == null ? null : messageList.get(0);
    }

    @Override
    public int findNoticeCount(int userId, String topic) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getFromId, 1);
        wrapper.eq(Message::getToId, userId);
        wrapper.eq(Message::getConversationId, topic);
        wrapper.ne(Message::getStatus, 2);
        return baseMapper.selectCount(wrapper);
    }

    @Override
    public int findNoticeUnreadCount(int userId, String topic) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getFromId, 1);
        wrapper.eq(Message::getToId, userId);
        wrapper.eq(topic != null, Message::getConversationId, topic);
        wrapper.eq(Message::getStatus, 0);
        return baseMapper.selectCount(wrapper);
    }

    @Override
    public List<Message> findNotices(int userId, String topic, PageInfo pageInfo) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getFromId, 1);
        wrapper.eq(Message::getToId, userId);
        wrapper.eq(Message::getConversationId, topic);
        wrapper.ne(Message::getStatus, 2);
        wrapper.orderByDesc(Message::getCreateTime);
        Page<Message> page = new Page<>(pageInfo.getCurrent(), pageInfo.getSize());
        baseMapper.selectPage(page, wrapper);
        return page.getRecords();
    }
}
