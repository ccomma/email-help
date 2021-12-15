package com.awesomecat.emailhelp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 邮箱属性相关配置
 *
 * @author awesomecat
 */
@Data
@ConfigurationProperties(prefix = "mail.receiver")
public class MailReceiverConfigurationProperties {

    /**
     * 收件箱地址
     */
    private String storeHost;

    /**
     * 发件箱地址
     */
    private String transportHost;

    /**
     * IMAP 端口
     */
    private Integer imapPort = 993;

    /**
     * SMTP 端口
     */
    private Integer smtpPort = 994;

    /**
     * 邮箱
     */
    private String username;

    /**
     * 授权码
     */
    private String password;

    /**
     * 收件协议
     */
    private String storeProtocol = "imap";

    /**
     * 发件协议
     */
    private String transportProtocol = "smtp";

    /**
     * SSL
     */
    private Boolean ssl = true;

    /**
     * idle 模式，默认开启
     */
    private Boolean idle = true;

    /**
     * 轮询评率，默认 10 秒一次
     */
    private Long pollFreq = 10000L;

    /**
     * 运行节点
     */
    private Boolean runNode = false;

    /**
     * 运行节点判断的 key
     */
    private String runNodeKey;

    /**
     * 运行节点判断的 value
     */
    private String runNodeValue;

}
