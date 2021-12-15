package com.awesomecat.emailhelp.service.impl;

import com.awesomecat.emailhelp.bo.MailInfo;
import com.awesomecat.emailhelp.config.MailReceiverConnection;
import com.awesomecat.emailhelp.service.MailHandlerService;
import com.awesomecat.emailhelp.service.MailMessageParser;
import com.awesomecat.emailhelp.strategy.ReceiveMailHandleStrategy;
import com.sun.mail.imap.IMAPFolder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.Transport;
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
    @Resource
    private ReceiveMailHandleStrategy forwardVerificationCodeStrategy;


    @Override
    public void tiktok() throws MessagingException {
        log.info("the ticktock is start, time:{}", LocalDateTime.now());

        // 连接
        mailReceiverConnection.storeConnect();

        // 打开收件箱
        IMAPFolder imapFolder = mailReceiverConnection.openInBox();

        // 发送心跳
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
    }

    @Override
    public void listen() throws MessagingException {
        log.info("start mail listener");
        try (Store store = mailReceiverConnection.storeConnect();
             IMAPFolder imapFolder = mailReceiverConnection.openInBox()) {

            if (mailReceiverConnection.isSupportIdle()) {
                throw new MessagingException("not support idle");
            }

            // 监听
            imapFolder.addMessageCountListener(new MessageCountAdapter() {
                @SneakyThrows
                @Override
                public void messagesAdded(MessageCountEvent e) {
                    for (Message msg : e.getMessages()) {
                        mailExecutorService.submit(() -> {
                            MailInfo mailInfo = mailMessageParser.parseMessage((MimeMessage) msg);
                            postHandleMessage(mailInfo);
                        });
                        // 设置已读，IMAP 可以更改状态，POP3 不支持
                        // msg.setFlag(Flags.Flag.SEEN, true);
                    }
                }
            });

            while (true) {
                this.openIdleMode(imapFolder);
            }
        }
    }

    @Override
    public void sendEmail(MailInfo mailInfo) throws MessagingException {
        log.info("start mail send email");
        try (Transport transport = mailReceiverConnection.transportConnect()) {
            // 创建邮件对象
            MimeMessage mimeMessage = new MimeMessage(mailReceiverConnection.getTransportSession());
            // 邮件发送人
            mimeMessage.setFrom(mailInfo.getFrom());
            // 邮件接收人
            mimeMessage.setRecipients(Message.RecipientType.TO, mailInfo.getTo().toArray(new Address[0]));
            // 邮件标题
            mimeMessage.setSubject(mailInfo.getSubject());
            // 邮件内容
            mimeMessage.setContent(mailInfo.getContent(), "text/html;charset=UTF-8");
            // 发送邮件
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        }
    }

    /**
     * 打开空闲模式
     * <p>
     * 使用 idle 模式，服务器可能会在超时结束时隐式地注销客户端，
     * 所以需要定时发送 NOOP 指令，以确保客户端仍然在线，{@link MailHandlerServiceImpl#tiktok()}。
     * <p>
     * NOOP 指令什么也不做，但这可以让 idle 终止并重新在 while 循环中运行。
     * GMail 超时时间很低，大约为 10 分钟，所以可以 NOOP 轮询时间可设为 9 分钟。
     *
     * @param imapFolder 收件箱
     * @author awesomecat
     * @date 2021/12/14 23:52
     */
    private void openIdleMode(IMAPFolder imapFolder) {
        try {
            if (!imapFolder.isOpen()) {
                log.warn("reopen folder");
                imapFolder.open(Folder.READ_WRITE);
            }

            // 空闲模式，保持监听
            imapFolder.idle();
        } catch (MessagingException e) {
            log.error("open idle mode error", e);
        }
    }

    /**
     * 接收邮件后的处理事件
     *
     * @param mailInfo 邮件信息
     * @author awesomecat
     * @date 2021/12/14 22:37
     */
    private void postHandleMessage(MailInfo mailInfo) {
        Assert.notNull(mailInfo, "mailInfo must not be null");
        log.info("you have a new message:[{}]", mailInfo);

        forwardVerificationCodeStrategy.doHandle(mailInfo);
    }

}
