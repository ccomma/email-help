package com.awesomecat.emailhelp.config;

import com.awesomecat.emailhelp.constants.MailConstant;
import com.sun.mail.imap.IMAPFolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * 邮箱接收配置
 *
 * @author awesomecat
 * @date 2021/12/14 11:32
 */
@Slf4j
@Component
public class MailReceiverConnection {

    private IMAPFolder imapFolder;
    private Store store;

    @Resource
    private MailReceiverConfigurationProperties mailReceiverConfigurationProperties;

    /**
     * 初始化连接
     */
    public void connect() throws MessagingException {
        if (this.isNotEnabled()) {
            return;
        }

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
        store = session.getStore(mailReceiverConfigurationProperties.getProtocol());
        store.connect(mailReceiverConfigurationProperties.getHost(), mailReceiverConfigurationProperties.getPort(), mailReceiverConfigurationProperties.getUsername(), mailReceiverConfigurationProperties.getPassword());

        // 获取收件箱
        Folder folder = store.getFolder(MailConstant.INBOX_FOLDER_NAME);
        if (folder == null || !folder.exists()) {
            String errorMsg = String.format("open inbox error, folder is null or not exists, host: %s, port: %s, username: %s", mailReceiverConfigurationProperties.getHost(), mailReceiverConfigurationProperties.getPort(), mailReceiverConfigurationProperties.getUsername());
            log.error(errorMsg);
            throw new MessagingException(errorMsg);
        }

        // 打开收件箱，并设置读写模式,如果不需要修改,则只读模式即可 READ_ONLY
        folder.open(Folder.READ_WRITE);
        this.imapFolder = (IMAPFolder) folder;
    }

    public void handle(Consumer<IMAPFolder> consumer) {
        consumer.accept(imapFolder);
    }

    /**
     * 是否开启
     *
     * @return 是否开启
     */
    public boolean isNotEnabled() {
        if (mailReceiverConfigurationProperties.getRunNode()) {
            return false;
        }

        // 支持多节点判断，优先 runMode = true，runNodeKey 为兼容已有系统中已有的配置
        String runNodeKeyEnv = System.getenv(mailReceiverConfigurationProperties.getRunNodeKey());
        return !mailReceiverConfigurationProperties.getRunNodeValue().equals(runNodeKeyEnv != null ? runNodeKeyEnv : "-1");
    }

    public void close() throws MessagingException {
        // 关闭收件箱
        if (imapFolder != null) {
            imapFolder.close(false);
        }

        // 关闭连接
        if (store != null) {
            store.close();
        }
    }

    public IMAPFolder getImapFolder() {
        return imapFolder;
    }

    public void setImapFolder(IMAPFolder imapFolder) {
        this.imapFolder = imapFolder;
    }

}
