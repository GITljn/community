package com.nowcoder.community;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.mapper.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.service.DiscussPostService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchExample {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private DiscussPostRepository discussPostRepository;
    @Autowired
    private ElasticsearchRepository<DiscussPost, Integer> elasticsearchRepository;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private Scheduler scheduler;


    @Test
    public void testInsert() {
//        DiscussPost discussPost = discussPostService.getById(109);
//        elasticsearchRepository.save(discussPost);
        elasticsearchRepository.saveAll(discussPostService.queryDiscussPosts(101, 1, 100));
        elasticsearchRepository.saveAll(discussPostService.queryDiscussPosts(102, 1, 100));
        elasticsearchRepository.saveAll(discussPostService.queryDiscussPosts(103, 1, 100));
        elasticsearchRepository.saveAll(discussPostService.queryDiscussPosts(111, 1, 100));
    }

    @Test
    public void testDelete() {
        elasticsearchRepository.deleteById(109);
    }

    @Test
    public void testUpdate() {
        DiscussPost discussPost = discussPostService.getById(109);
        discussPost.setContent("修改后");
        // 先删除在添加
        elasticsearchRepository.save(discussPost);
    }

    // elasticsearchRepository无法实现关键词的高亮显示
    @Test
    public void testSearchByRepository() {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                )
                .build();
        Page<DiscussPost> page = elasticsearchRepository.search(searchQuery);
        System.out.println("数据的总条数: "+page.getTotalElements());
        System.out.println("数据的总页数: "+page.getTotalPages());
        System.out.println("当前页码: "+page.getNumber());
        System.out.println("每页条数: "+page.getSize());
        for (DiscussPost discussPost : page.getContent()) {
            System.out.println(discussPost);
        }
    }

    @Test
    public void testSearchByTemplate() {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 1))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                )
                .build();
        Page<DiscussPost> page = elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
                SearchHits hits = response.getHits();
                List<DiscussPost> list = new ArrayList<>();
                for (SearchHit hit : hits) {
                    DiscussPost post = new DiscussPost();
                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.valueOf(id));

                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setUserId(userId);

                    String title = hit.getSourceAsMap().get("title").toString();
                    post.setTitle(title);

                    String content = hit.getSourceAsMap().get("content").toString();
                    post.setContent(content);

                    String status = hit.getSourceAsMap().get("status").toString();
                    post.setStatus(Integer.valueOf(status));

                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    post.setCreateTime(new Date(Long.parseLong(createTime)));

                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    post.setCommentCount(Integer.valueOf(commentCount));

                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if (titleField != null) {
                        post.setTitle(titleField.getFragments()[0].toString());
                    }
                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if (contentField != null) {
                        post.setContent(contentField.getFragments()[0].toString());
                        for (Text fragment : contentField.getFragments()) {
                            System.out.println("fragment: "+fragment.toString());
                        }
                    }
                    list.add(post);
                }
                return new AggregatedPageImpl(list, pageable, hits.getTotalHits(), response.getAggregations(),
                        response.getScrollId(), hits.getMaxScore());
            }
        });
        System.out.println("数据的总条数: "+page.getTotalElements());
        System.out.println("数据的总页数: "+page.getTotalPages());
        System.out.println("当前页码: "+page.getNumber());
        System.out.println("每页条数: "+page.getSize());
        for (DiscussPost discussPost : page.getContent()) {
            System.out.println(discussPost);
        }
    }

    @Test
    public void deleteJob() {
        try {
            boolean isDelete = scheduler.deleteJob(new JobKey("postScoreRefershJob", "postScoreRefershJobGroup"));
            System.out.println(isDelete);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void insertPost() {
        for (int i = 0; i < 300000; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId("111");
            post.setTitle("aaa");
            post.setContent("aaa");
            discussPostService.save(post);
        }
    }
}
