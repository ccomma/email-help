package com.awesomecat.emailhelp.bo;

import com.awesomecat.emailhelp.constants.MailConstant;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import java.util.Date;
import java.util.List;

/**
 * 邮件信息
 *
 * @author awesomecat
 */
@Slf4j
@Data
@ToString
public class MailInfo {

    /** 邮件正文内容 */
    private String content;

    /** 发件人的地址和姓名 */
    private Address from;

    /** 收件人 */
    private List<Address> to;

    /** 抄送人 */
    private List<Address> cc;

    /** 私密抄送 */
    private List<Address> bcc;

    /** 邮件主题 */
    private String subject;

    /** 邮件发送日期 */
    private Date sentDate;

    /** 邮件接收日期 */
    private Date receivedDate;

    /** 邮件的 Message-ID */
    private String messageId;

    /**
     * 收件箱
     * <p> 默认 {@link MailConstant#INBOX_FOLDER_NAME}
     */
    private String boxName;

}
