package com.awesomecat.emailhelp.service.impl;

import com.awesomecat.emailhelp.bo.MailInfo;
import com.awesomecat.emailhelp.config.MailReceiverConnection;
import com.awesomecat.emailhelp.service.MailHandlerService;
import com.awesomecat.emailhelp.service.MailMessageParser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 业务处理的方法
 *
 * @author awesomecat
 */
@Slf4j
@Service
public class MailHandlerServiceImpl implements MailHandlerService {

    private final ExecutorService mailExecutorService = Executors.newFixedThreadPool(4);

    @Resource
    private MailReceiverConnection mailReceiverConnection;
    @Resource
    private MailMessageParser mailMessageParser;

    @Override
    public void tiktok() throws MessagingException {
        log.info("the ticktock is start, time:{}", LocalDateTime.now());

        // 连接
        mailReceiverConnection.connect();

        // 发送心跳
        mailReceiverConnection.handle(imapFolder -> {
            try {
                log.debug("send NOOP command");
                imapFolder.doCommand(p -> {
                    p.simpleCommand("NOOP", null);
                    return null;
                });
            } catch (MessagingException e) {
                log.error("error send NOOP command", e);
            }
            log.debug("end send NOOP command");
        });
    }

    @Override
    public void listen() throws MessagingException {
        log.info("start mail listener");

        try {
            // 建立连接
            mailReceiverConnection.connect();

            mailReceiverConnection.handle(imapFolder -> {
                imapFolder.addMessageCountListener(new MessageCountAdapter() {
                    @SneakyThrows
                    @Override
                    public void messagesAdded(MessageCountEvent e) {
                        for (Message msg : e.getMessages()) {
                            mailExecutorService.submit(() -> {
                                MailInfo mailInfo = mailMessageParser.parseMessage((MimeMessage) msg);
                                handlerMessage(mailInfo);
                            });
                            // 设置已读，IMAP 可以更改状态，POP3 不支持
                            // msg.setFlag(Flags.Flag.SEEN, true);
                        }
                    }
                });

                // 中断后继续 idle
                while (true) {
                    try {
                        if (!imapFolder.isOpen()) {
                            log.warn("reopen folder");
                            imapFolder.open(Folder.READ_WRITE);
                        }
                        imapFolder.idle();
                    } catch (MessagingException e) {
                        log.error("reopen folder error", e);
                    }
                }
            });
        } finally {
            // 关闭连接
            mailReceiverConnection.close();
        }

    }

    // TODO: 2021/12/14 策略
    private boolean handlerMessage(MailInfo mailInfo) {
        Assert.notNull(mailInfo, "mailInfo must not be null");

        log.info("you have a message:[{}]", mailInfo);
        return true;
    }

}
