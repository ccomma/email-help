package com.awesomecat.verificationcodetransmit.service;

import javax.mail.MessagingException;

/**
 * 收到邮件后统一回调此接口，业务方如需使用，实现此接口即可
 *
 * @author awesomecat
 */
public interface MailHandlerService {

    /**
     * 心跳
     *
     * @author mianXian
     * @date 2021/12/14 14:11
     */
    void tiktok() throws MessagingException;

    /**
     * 监听新邮件
     *
     * @author mianXian
     * @date 2021/12/14 14:20
     */
    void listen() throws MessagingException;
}
