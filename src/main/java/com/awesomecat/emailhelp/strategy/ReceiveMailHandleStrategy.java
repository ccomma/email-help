package com.awesomecat.emailhelp.strategy;

import com.awesomecat.emailhelp.bo.MailInfo;

import javax.mail.MessagingException;

/**
 * 接收邮件处理策略
 *
 * @author awesome
 * @date 2021/12/14 22:28
 */
public interface ReceiveMailHandleStrategy {

    void doHandle(MailInfo mailInfo);
}
