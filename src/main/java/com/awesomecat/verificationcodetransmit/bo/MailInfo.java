package com.awesomecat.verificationcodetransmit.bo;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 邮件内容
 *
 * @author awesomecat
 */
@Slf4j
@Data
@ToString
public class MailInfo {

    /** 邮件正文内容 */
    private String bodyText;

    /** 发件人的地址和姓名 */
    private String from;

    /** 收件人 */
    private String to;

    /** 抄送人 */
    private String cc;

    /** 私密抄送 */
    private String bcc;

    /** 邮件主题 */
    private String subject;

    /** 邮件发送日期 */
    private Date sentDate;

    /** 邮件接收日期 */
    private Date receivedDate;

    /** 邮件的 Message-ID */
    private String messageId;

    private String box;

}
