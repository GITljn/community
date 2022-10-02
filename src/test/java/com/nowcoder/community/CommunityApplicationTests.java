package com.nowcoder.community;
import java.util.Date;
import java.util.List;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
class CommunityApplicationTests {
	@Autowired
	private UserService userService;

	@Autowired
	private DiscussPostService discussPostService;

	@Autowired
	private MailClient mailClient;

	@Autowired
	private TemplateEngine templateEngine;

	@Test
	void contextLoads() {
		mailClient.sendMail("742277203@qq.com", "text", "测试", true);
	}

	@Test
	void testSendHtml() {
		Context context = new Context();
		context.setVariable("username", "sunday");
		String content = templateEngine.process("/mail/demo", context);
		System.out.println(content);
		mailClient.sendMail("742277203@qq.com", "HTML", content, true);
	}

}
