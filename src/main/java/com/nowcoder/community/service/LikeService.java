package com.nowcoder.community.service;

public interface LikeService {
    void like(int userId, int entityType, int entityId, int entityUserId);

    int findEntityLikeStatus(int userId, int entityType, int entityId);

    long findEntityLikeCount(int entityType, int entityId);

    long findUserLikeCount(int userId);
}
