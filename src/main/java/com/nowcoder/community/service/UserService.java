package com.nowcoder.community.service;

import com.nowcoder.community.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ljn
 * @since 2022-09-29
 */
public interface UserService extends IService<User> {
    User queryById(int id);
    User queryByUserName(String userName);
    User queryByEmail(String email);

    int addUser(User user);
    int updateStatus(int id, int status);
    int updateHeader(int id, String headerUrl);
    int updatePassword(int id, String password);

    public Map<String, Object> register(User user);

    public int activation(int userId, String code);

    public Map<String, Object> login(String username, String password, int expiredSeconds);

    public void logout(String ticket);
}
