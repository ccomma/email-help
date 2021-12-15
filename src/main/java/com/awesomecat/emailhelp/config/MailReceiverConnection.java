package com.awesomecat.emailhelp.config;

import com.awesomecat.emailhelp.constants.MailConstant;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import java.util.HashMap;
import java.util.Properties;

/**
 * 邮箱接收配置
 *
 * @author awesomecat
 * @date 2021/12/14 11:32
 */
@Slf4j
@Component
public class MailReceiverConnection {

    /** 收件 Session */
    private Session storeSession;
    /** 收件连接 */
    private Store store;
    /** 发件 Session */
    private Session transportSession;
    /** 发件连接 */
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

        if (storeSession == null) {
            storeSession = this.createSession();
        }

        if (store != null) {
            return store;
        }

        log.info("mail init store connect");

        store = storeSession.getStore(mailReceiverConfigurationProperties.getStoreProtocol());
        store.connect(mailReceiverConfigurationProperties.getStoreHost(), mailReceiverConfigurationProperties.getImapPort(), mailReceiverConfigurationProperties.getUsername(), mailReceiverConfigurationProperties.getPassword());

        // 163 邮箱需要添加 id 进行认证
        if (MailConstant.IMAP_SERVER_163.equals(mailReceiverConfigurationProperties.getStoreHost())) {
            // 带上 IMAP ID 信息，由 key 和 value 组成，例如 name，version，vendor，support-email 等
            HashMap<String, String> imapIdMap = new HashMap<>(8);
            imapIdMap.put("name", "myname");
            imapIdMap.put("version", "1.0.0");
            imapIdMap.put("vendor", "myclient");
            imapIdMap.put("support-email", "testmail@test.com");
            ((IMAPStore) store).id(imapIdMap);
        }

        return store;
    }

    /**
     * 发件连接
     */
    public Transport transportConnect() throws MessagingException {
        if (this.isNotEnabled()) {
            throw new MessagingException();
        }

        if (transportSession == null) {
            transportSession = this.createSession();
        }

        if (transport != null) {
            return transport;
        }

        log.info("mail init transport connect");

        // 获取连接对象
        transport = transportSession.getTransport(mailReceiverConfigurationProperties.getTransportProtocol());

        // 连接服务器
        transport.connect(mailReceiverConfigurationProperties.getTransportHost(), mailReceiverConfigurationProperties.getUsername(), mailReceiverConfigurationProperties.getPassword());

        return transport;
    }

    /**
     * 打开收件箱
     *
     * @return 收件箱
     * @author awesomecat
     * @date 2021/12/15 13:14
     */
    public IMAPFolder openInBox() throws MessagingException {
        // 获取收件箱
        Folder folder = store.getFolder(MailConstant.INBOX_FOLDER_NAME);
        if (folder == null || !folder.exists()) {
            String errorMsg = String.format("open inbox error, folder is null or not exists, host: %s, port: %s, username: %s", mailReceiverConfigurationProperties.getStoreHost(), mailReceiverConfigurationProperties.getImapPort(), mailReceiverConfigurationProperties.getUsername());
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
        properties.setProperty("mail.transport.protocol", mailReceiverConfigurationProperties.getTransportProtocol());
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
        if (Boolean.TRUE.equals(mailReceiverConfigurationProperties.getRunNode())) {
            return false;
        }

        // 支持多节点判断，优先 runMode = true，runNodeKey 为兼容已有系统中已有的配置
        String runNodeKeyEnv = System.getenv(mailReceiverConfigurationProperties.getRunNodeKey());
        return !mailReceiverConfigurationProperties.getRunNodeValue().equals(runNodeKeyEnv != null ? runNodeKeyEnv : "-1");
    }

    /**
     * 是否支持 idle 模式
     */
    public boolean isSupportIdle() throws MessagingException {
        return ((IMAPStore) store).hasCapability("IDLE");
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
