package com.nowcoder.community.service;

import com.nowcoder.community.entity.LoginTicket;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ljn
 * @since 2022-10-01
 */
@Deprecated
public interface LoginTicketService extends IService<LoginTicket> {
    int insertLoginTicket(LoginTicket loginTicket);
    LoginTicket selectByTicket(String ticket);
    int updateStatus(String ticket, int status);
}
