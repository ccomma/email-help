package com.awesomecat.emailhelp.service;

import com.awesomecat.emailhelp.bo.MailInfo;

import javax.mail.internet.MimeMessage;

/**
 * 解析器
 *
 * @author awesomecat
 * @date 2021/12/14 14:52
 */
public interface MailMessageParser {

    /**
     * 解析
     *
     * @return MailInfo
     * @author awesomecat
     * @date 2021/12/14 14:53
     */
    MailInfo parseMessage(MimeMessage message);
}
