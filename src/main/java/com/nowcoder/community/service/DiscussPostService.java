package com.nowcoder.community.service;

import com.nowcoder.community.entity.DiscussPost;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ljn
 * @since 2022-09-29
 */
public interface DiscussPostService extends IService<DiscussPost> {
    List<DiscussPost> queryDiscussPosts(int userId, int page, int size);

    int queryDiscussPostRows(int userId);
}
