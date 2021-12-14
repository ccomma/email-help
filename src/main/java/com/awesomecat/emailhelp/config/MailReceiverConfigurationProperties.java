package com.awesomecat.emailhelp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 邮箱属性相关配置
 *
 * @author awesomecat
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "mail.receiver")
public class MailReceiverConfigurationProperties {

    /**
     * 邮箱服务地址
     */
    private String host;

    /**
     * 端口
     */
    private Integer port = 993;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 协议
     */
    private String protocol = "imap";

    /**
     * ssl
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
