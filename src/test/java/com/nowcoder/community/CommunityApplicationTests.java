package com.nowcoder.community;
import java.util.ArrayList;
import java.util.Arrays;

import com.nowcoder.community.utils.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests {

	@Autowired
	private MailClient mailClient;

	@Autowired
	private TemplateEngine templateEngine;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private Scheduler scheduler;

	@Test
	public void contextLoads() {
		long a = redisTemplate.opsForZSet().zCard("aaa");
		System.out.println(a);
	}

	@Test
	public void testSendHtml() {
		Context context = new Context();
		context.setVariable("username", "sunday");
		String content = templateEngine.process("/mail/demo", context);
		System.out.println(content);
		mailClient.sendMail("742277203@qq.com", "HTML", content, true);
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
	public void test001() {
		List<Integer> list = new ArrayList<>();
		list.add(11);
		list.add(22);
		Integer[] ints = list.toArray(new Integer[0]);
		System.out.println(Arrays.toString(ints));
	}

}
