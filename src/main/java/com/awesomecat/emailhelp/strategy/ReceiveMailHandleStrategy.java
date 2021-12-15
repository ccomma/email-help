package com.awesomecat.emailhelp.strategy;

import com.awesomecat.emailhelp.bo.MailInfo;

/**
 * 接收邮件处理策略
 *
 * @author awesomecat
 * @date 2021/12/14 22:28
 */
public interface ReceiveMailHandleStrategy {

    /**
     * 执行策略
     *
     * @param mailInfo 邮件信息
     * @author awesomecat
     * @date 2021/12/15 13:02
     */
    void doHandle(MailInfo mailInfo);
}
