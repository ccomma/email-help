package com.awesomecat.verificationcodetransmit.receiver;

import com.awesomecat.verificationcodetransmit.config.MailReceiverConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.MessagingException;

/**
 * 邮件监听器
 * 应用启动之后执行监听
 *
 * @author awesomecat
 */
@Slf4j
@Component
public class MailListener {

    @Resource
    private MailReceiverConnection mailReceiverConnection;


    /**
     * 异步监听
     */
    @Async
    public void asyncListener() throws MessagingException {
        if (!mailReceiverConnection.isEnabled()) {
            return;
        }

        // 建立连接
        mailReceiverConnection.connect();

        // 监听
        mailReceiverConnection.listener();
    }

}
