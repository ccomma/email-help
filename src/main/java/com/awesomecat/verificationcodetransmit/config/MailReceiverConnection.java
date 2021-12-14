package com.awesomecat.verificationcodetransmit.config;

import com.awesomecat.verificationcodetransmit.bo.MailInfo;
import com.awesomecat.verificationcodetransmit.service.MailHandlerService;
import com.sun.mail.imap.IMAPFolder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 邮箱接收配置
 *
 * @author mianXian
 * @date 2021/12/14 11:32
 */
@Slf4j
@Component
public class MailReceiverConnection {

    private IMAPFolder imapFolder;
    private final ExecutorService mailExecutorService = Executors.newFixedThreadPool(4);

    @Resource
    private MailReceiverConfigurationProperties mailReceiverConfigurationProperties;
    @Resource
    private MailHandlerService mailHandlerService;

    /**
     * 是否开启
     *
     * @return 是否开启
     */
    public boolean isEnabled() {
        if (mailReceiverConfigurationProperties.getRunNode()) {
            return true;
        }

        // 支持多节点判断，优先 runMode = true，runNodeKey 为兼容已有系统中已有的配置
        String runNodeKeyEnv = System.getenv(mailReceiverConfigurationProperties.getRunNodeKey());
        return mailReceiverConfigurationProperties.getRunNodeValue().equals(runNodeKeyEnv != null ? runNodeKeyEnv : "-1");
    }

    /**
     * 初始化连接
     */
    public void connect() throws MessagingException {
        if (imapFolder != null) {
            return;
        }

        log.info("mail init connect");

        Properties properties = System.getProperties();
        properties.setProperty("mail.imap.auth", "true");
        properties.setProperty("mail.store.protocol", mailReceiverConfigurationProperties.getProtocol());
        properties.setProperty("mail.imap.ssl.enable", String.valueOf(mailReceiverConfigurationProperties.getSsl()));

        // 连接邮件服务器
        Session session = Session.getInstance(properties, null);
        Store store = session.getStore(mailReceiverConfigurationProperties.getProtocol());
        store.connect(mailReceiverConfigurationProperties.getHost(), mailReceiverConfigurationProperties.getPort(), mailReceiverConfigurationProperties.getUsername(), mailReceiverConfigurationProperties.getPassword());

        // 获取收件箱
        Folder folder = store.getFolder("Inbox");
        if (folder == null || !folder.exists()) {
            String errorMsg = String.format("open inbox error, folder is null or not exists, host: %s, port: %s, username: %s", mailReceiverConfigurationProperties.getHost(), mailReceiverConfigurationProperties.getPort(), mailReceiverConfigurationProperties.getUsername());
            log.error(errorMsg);
            throw new MessagingException(errorMsg);
        }

        // 打开收件箱，并设置读写模式,如果不需要修改,则只读模式即可 READ_ONLY
        folder.open(Folder.READ_WRITE);
        this.imapFolder = (IMAPFolder) folder;
    }

    /**
     * 监听新邮件
     *
     * @author awesomecat
     * @date 2021/12/14 11:49
     */
    public void listener() throws MessagingException {
        log.info("start mail listener");

        imapFolder.addMessageCountListener(new MessageCountAdapter() {
            @SneakyThrows
            @Override
            public void messagesAdded(MessageCountEvent e) {
                Message[] messages = e.getMessages();
                log.info("new message length:[{}]", messages.length);
                for (Message msg : messages) {
                    mailExecutorService.submit(() -> {
                        mailHandlerService.doHandler(MailInfo.of(msg));
                    });
                    // 设置已读
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
            } catch (FolderClosedException e) {
                log.error("reopen folder error", e);
            }
        }
    }

    public IMAPFolder getImapFolder() {
        return imapFolder;
    }

    public void setImapFolder(IMAPFolder imapFolder) {
        this.imapFolder = imapFolder;
    }
}
