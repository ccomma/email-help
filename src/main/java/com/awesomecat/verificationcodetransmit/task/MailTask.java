package com.awesomecat.verificationcodetransmit.task;

import com.awesomecat.verificationcodetransmit.config.MailReceiverConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.time.LocalDateTime;

/**
 * 邮件任务
 *
 * @author mianXian
 * @date 2021/12/14 11:23
 */
@Slf4j
@Component
public class MailTask {

    @Resource
    private MailReceiverConnection mailReceiverConnection;

    /**
     * 定时每10分钟和服务器通信一次，保持连接
     */
    @Scheduled(cron = "${mail.receiver.cron}")
    public void noop() throws MessagingException {
        if (!mailReceiverConnection.isEnabled()) {
            return;
        }

        log.info("the ticktock is start, time:{}", LocalDateTime.now());

        // 连接
        mailReceiverConnection.connect();

        // 发送心跳
        mailReceiverConnection.handle(imapFolder -> {
            try {
                log.info("send NOOP command");
                imapFolder.doCommand(p -> {
                    p.simpleCommand("NOOP", null);
                    return null;
                });
            } catch (MessagingException e) {
                log.error("error send NOOP command", e);
            }
            log.info("end send NOOP command");
        });
    }

}
