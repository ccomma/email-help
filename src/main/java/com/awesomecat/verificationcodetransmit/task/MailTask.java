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
     * 心跳：每 10 分钟一次
     */
    @Scheduled(cron = "${mail.receiver.cron}")
    public void noop() throws MessagingException {
        if (!mailReceiverConnection.isEnabled()) {
            return;
        }

        log.info("the ticktock is start, time:{}", LocalDateTime.now());

        // TODO: 2021/12/14 移至 Service 类中

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
