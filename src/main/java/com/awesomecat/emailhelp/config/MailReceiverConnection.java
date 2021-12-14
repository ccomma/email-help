package com.awesomecat.emailhelp.config;

import com.awesomecat.emailhelp.constants.MailConstant;
import com.sun.mail.imap.IMAPFolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.*;
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

    private Session storeSession;
    private Store store;
    private Session transportSession;
    private Transport transport;

    @Resource
    private MailReceiverConfigurationProperties mailReceiverConfigurationProperties;

    /**
     * 收件连接
     */
    public Store storeConnect() throws MessagingException {
        if (this.isNotEnabled()) {
            throw new MessagingException();
        }

        log.info("mail init store connect");

        if (storeSession == null) {
            storeSession = this.createSession();
        }

        if (store != null) {
            return store;
        }

        store = storeSession.getStore(mailReceiverConfigurationProperties.getStoreProtocol());
        store.connect(mailReceiverConfigurationProperties.getStoreHost(), mailReceiverConfigurationProperties.getPort(), mailReceiverConfigurationProperties.getUsername(), mailReceiverConfigurationProperties.getPassword());

        return store;
    }

    public Transport transportConnect() throws MessagingException {
        if (this.isNotEnabled()) {
            throw new MessagingException();
        }

        log.info("mail init transport connect");

        if (transportSession == null) {
            transportSession = this.createSession();
        }

        if (transport != null) {
            return transport;
        }

        // 获取连接对象
        transport = transportSession.getTransport(mailReceiverConfigurationProperties.getTransportProtocol());

        // 连接服务器
        transport.connect(mailReceiverConfigurationProperties.getTransportHost(), mailReceiverConfigurationProperties.getUsername(), mailReceiverConfigurationProperties.getPassword());

        return transport;
    }

    public IMAPFolder openInBox() throws MessagingException {
        // 获取收件箱
        Folder folder = store.getFolder(MailConstant.INBOX_FOLDER_NAME);
        if (folder == null || !folder.exists()) {
            String errorMsg = String.format("open inbox error, folder is null or not exists, host: %s, port: %s, username: %s", mailReceiverConfigurationProperties.getStoreHost(), mailReceiverConfigurationProperties.getPort(), mailReceiverConfigurationProperties.getUsername());
            log.error(errorMsg);
            throw new MessagingException(errorMsg);
        }

        // 打开收件箱，并设置读写模式,如果不需要修改,则只读模式即可 READ_ONLY
        folder.open(Folder.READ_WRITE);
        return (IMAPFolder) folder;
    }

    /**
     * 设置 session
     *
     * @author awesomecat
     * @date 2021/12/14 23:21
     */
    private Session createSession() {
        Properties properties = System.getProperties();
        properties.setProperty("mail.imap.auth", "true");
        properties.setProperty("mail.store.protocol", mailReceiverConfigurationProperties.getStoreProtocol());
        properties.setProperty("mail.transport.protocol",mailReceiverConfigurationProperties.getTransportProtocol());
        properties.setProperty("mail.imap.ssl.enable", String.valueOf(mailReceiverConfigurationProperties.getSsl()));
        properties.setProperty("mail.smtp.ssl.enable", String.valueOf(mailReceiverConfigurationProperties.getSsl()));

        return Session.getInstance(properties, null);
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

    public Session getStoreSession() {
        return storeSession;
    }

    public void setStoreSession(Session storeSession) {
        this.storeSession = storeSession;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Session getTransportSession() {
        return transportSession;
    }

    public void setTransportSession(Session transportSession) {
        this.transportSession = transportSession;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }
}
