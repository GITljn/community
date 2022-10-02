package com.nowcoder.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.mapper.UserMapper;
import com.nowcoder.community.service.LoginTicketService;
import com.nowcoder.community.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.MD5Util;
import com.nowcoder.community.utils.MailClient;
import com.nowcoder.community.utils.UUIDUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ljn
 * @since 2022-09-29
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService, CommunityConstant {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketService loginTicketService;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public User queryById(int id) {
        return baseMapper.selectById(id);
    }

    @Override
    public User queryByUserName(String userName) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, userName);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public User queryByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public int addUser(User user) {
        return baseMapper.insert(user);
    }

    @Override
    public int updateStatus(int id, int status) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, id);
        wrapper.set(User::getStatus, status);
        return baseMapper.update(null, wrapper);
    }

    @Override
    public int updateHeader(int id, String headerUrl) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, id);
        wrapper.set(User::getHeaderUrl, headerUrl);
        return baseMapper.update(null, wrapper);
    }

    @Override
    public int updatePassword(int id, String password) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, id);
        wrapper.set(User::getPassword, password);
        return baseMapper.update(null, wrapper);
    }

    @Override
    public Map<String, Object> register(User user) {
        // 1.判断参数的有效性 2.判断手机号、邮箱是否注册 3.将用户信息插入数据库 4.发送激活邮件
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        // 验证账号
        User u = queryByUserName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }

        // 验证邮箱
        u = queryByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }

        // 注册用户
        user.setSalt(UUIDUtil.uuid().substring(0, 5));
        user.setPassword(MD5Util.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(UUIDUtil.uuid());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        addUser(user);

        // 激活邮件
        // context用于存储html中需要的数据
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content, true);

        return map;
    }

    @Override
    public int activation(int userId, String code) {
        User user = queryById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    @Override
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = queryByUserName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 验证密码
        password = MD5Util.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(UUIDUtil.uuid());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000L));
        loginTicketService.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    @Override
    public void logout(String ticket) {
        loginTicketService.updateStatus(ticket, 1);
    }
}
