package com.awesomecat.verificationcodetransmit.service;

import com.awesomecat.verificationcodetransmit.bo.MailInfo;

import javax.mail.internet.MimeMessage;

/**
 * 解析器
 *
 * @author mianXian
 * @date 2021/12/14 14:52
 */
public interface MailMessageParser {

    /**
     * 解析
     *
     * @return MailInfo
     * @author mianXian
     * @date 2021/12/14 14:53
     */
    MailInfo parseMessage(MimeMessage message);
}
