package com.nowcoder.community.service;

import com.nowcoder.community.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ljn
 * @since 2022-10-03
 */
public interface CommentService extends IService<Comment> {

    void addComment(Comment comment);
}
