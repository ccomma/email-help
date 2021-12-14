package com.awesomecat.emailhelp.service;

import com.awesomecat.emailhelp.bo.MailInfo;

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
     * @author awesomecat
     * @date 2021/12/14 14:11
     */
    void tiktok() throws MessagingException;

    /**
     * 监听新邮件
     *
     * @author awesomecat
     * @date 2021/12/14 14:20
     */
    void listen() throws MessagingException;

    /**
     * 发送邮件
     *
     * @param mailInfo 邮件信息
     * @author CComma
     * @date 2021/12/14 22:23
     */
    void sendEmail(MailInfo mailInfo) throws MessagingException;

}
