package com.nowcoder.community.service;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.domain.Page;

public interface SearchService {
    void saveDiscussPost(DiscussPost post);
    void deleteDiscussPost(int postId);
    Page<DiscussPost> searchDiscussPost(String keyWord, int current, int size);
}
