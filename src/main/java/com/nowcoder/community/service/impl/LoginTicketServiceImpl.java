package com.nowcoder.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.mapper.LoginTicketMapper;
import com.nowcoder.community.service.LoginTicketService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ljn
 * @since 2022-10-01
 */
@Service
public class LoginTicketServiceImpl extends ServiceImpl<LoginTicketMapper, LoginTicket> implements LoginTicketService {

    @Override
    public int insertLoginTicket(LoginTicket loginTicket) {
        return baseMapper.insert(loginTicket);
    }

    @Override
    public LoginTicket selectByTicket(String ticket) {
        LambdaQueryWrapper<LoginTicket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LoginTicket::getTicket, ticket);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public int updateStatus(String ticket, int status) {
        LambdaUpdateWrapper<LoginTicket> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(LoginTicket::getTicket, ticket);
        wrapper.set(LoginTicket::getStatus, status);
        return baseMapper.update(null, wrapper);
    }
}
