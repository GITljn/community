package com.nowcoder.community.service;

import com.nowcoder.community.entity.Message;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nowcoder.community.utils.PageInfo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ljn
 * @since 2022-10-04
 */
public interface MessageService extends IService<Message> {
    List<Message> findConversations(int userId, PageInfo pageInfo);
    int findConversationCount(int userId);
    List<Message> findLetters(String conversationsId, PageInfo pageInfo);
    int findLetterCount(String conversationsId);
    int findLetterUnreadCount(int userId, String conversationsId);
    void readMessage(List<Integer> ids);
    Message findLatestNotice(int userId, String topic);
    int findNoticeCount(int userId, String topic);
    int findNoticeUnreadCount(int userId, String topic);
    List<Message> findNotices(int userId, String topic, PageInfo pageInfo);
}
