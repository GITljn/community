package com.nowcoder.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.mapper.UserMapper;
import com.nowcoder.community.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ljn
 * @since 2022-09-29
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

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
}
