package com.awesomecat.verificationcodetransmit.task;

import com.awesomecat.verificationcodetransmit.config.MailReceiverConnection;
import com.sun.mail.imap.IMAPFolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.time.LocalDateTime;

/**
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
    protected void noop() throws MessagingException {
        if (!mailReceiverConnection.isEnabled()) {
            return;
        }

        log.info("定时通信,保持邮箱连接有效，time:{}", LocalDateTime.now());
        mailReceiverConnection.connect();
        log.info("发送 NOOP 命令");
        try {
            IMAPFolder imapFolder = mailReceiverConnection.getImapFolder();
            imapFolder.doCommand(p -> {
                p.simpleCommand("NOOP", null);
                return null;
            });
        } catch (MessagingException e) {
            log.error("NOOP 异常", e);
        }
        log.info("发送 NOOP 命令 done!");
    }
}
