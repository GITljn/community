package com.nowcoder.community.service;

import com.nowcoder.community.utils.PageInfo;

import java.util.Map;

public interface HomeService {
    Map<String, Object> queryIndexPage(PageInfo pageInfo);
}
