package com.awesomecat.verificationcodetransmit.service.impl;

import com.awesomecat.verificationcodetransmit.bo.MailInfo;
import com.awesomecat.verificationcodetransmit.bo.PersonInfo;
import com.awesomecat.verificationcodetransmit.service.MailMessageParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 信息解析器
 *
 * @author awesomecat
 * @date 2021/12/14 14:52
 */
@Slf4j
@Service
public class MailMessageParserImpl implements MailMessageParser {

    @Override
    public MailInfo parseMessage(MimeMessage mimeMessage) {
        try {
            Folder folder = mimeMessage.getFolder();
            if (!folder.isOpen()) {
                return null;
            }

            MailInfo mailInfo = new MailInfo();
            mailInfo.setFrom(this.getFrom(mimeMessage));
            mailInfo.setTo(this.getReceiveInfoList(Message.RecipientType.TO, mimeMessage));
            mailInfo.setCc(this.getReceiveInfoList(Message.RecipientType.CC, mimeMessage));
            mailInfo.setBcc(this.getReceiveInfoList(Message.RecipientType.BCC, mimeMessage));
            mailInfo.setSubject(MimeUtility.decodeText(mimeMessage.getSubject()));
            mailInfo.setSentDate(mimeMessage.getSentDate());
            mailInfo.setReceivedDate(mimeMessage.getReceivedDate());
            mailInfo.setContent(this.getBodyText(mimeMessage));
            mailInfo.setMessageId(mimeMessage.getMessageID());
            mailInfo.setBoxName(mimeMessage.getFolder().getName());
            return mailInfo;
        } catch (MessagingException e) {
            log.error("parse message error", e);
        } catch (IOException e) {
            log.error("file operate error", e);
        }

        return null;
    }

    /**
     * 获取发送人信息
     *
     * @param mimeMessage 邮件信息
     * @return 发送人信息
     * @author awesomecat
     * @date 2021/12/14 19:06
     */
    private PersonInfo getFrom(MimeMessage mimeMessage) throws MessagingException {
        InternetAddress[] address = (InternetAddress[]) mimeMessage.getFrom();
        if (address[0].getAddress() == null) {
            log.warn("mail address is null!");
            return null;
        }

        return PersonInfo.of(address[0]);
    }

    /**
     * 获取接收人信息列表
     * <p> 用于获取收件人人、抄送人、密送人
     *
     * @param type        类型（收件人 or 抄送人 or 秘密抄送）
     * @param mimeMessage 邮件信息
     * @return 接收人信息列表
     * @author awesomecat
     * @date 2021/12/14 19:06
     */
    private List<PersonInfo> getReceiveInfoList(Message.RecipientType type, MimeMessage mimeMessage) throws MessagingException, UnsupportedEncodingException {
        Assert.notNull(type, "RecipientType must not be null");
        InternetAddress[] addressList = (InternetAddress[]) mimeMessage.getRecipients(type);
        if (addressList == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(addressList).map(PersonInfo::of).collect(Collectors.toList());
    }

    /**
     * 获取正文
     *
     * @param mimeMessage 邮件信息
     * @return 邮件正文
     * @author awesomecat
     * @date 2021/12/14 19:08
     */
    private String getBodyText(MimeMessage mimeMessage) throws MessagingException, IOException {
        StringBuilder bodyTextBuilder = new StringBuilder();
        Map<String, String> imgs = new HashMap<>();

        this.getMailContent(bodyTextBuilder, imgs, mimeMessage);
        String tmpContent = bodyTextBuilder.toString();
        // 替换原文中的图片，原始图片标签为<img src="cid:image001.jpg@01D24963.3B4B8280">
        for (Map.Entry<String, String> entry : imgs.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            String replacedText = "cid:" + key.replace("<", "").replace(">", "");
            tmpContent = tmpContent.replace(replacedText, value);
        }

        return tmpContent;
    }

    /**
     * 获取邮件内容
     *
     * @param stringBuilder 内容构造器
     * @param imgs          图片信息
     * @param part          邮件内容
     * @author mianXian
     * @date 2021/12/14 19:10
     */
    private void getMailContent(StringBuilder stringBuilder, Map<String, String> imgs, Part part) throws MessagingException, IOException {
        // 检查内容是否为纯文本
        if (part.isMimeType("text/plain")) {
            log.warn("skip text plain");
            return;
        }

        // 检查内容是否为 html
        if (part.isMimeType("text/html")) {
            stringBuilder.append(part.getContent());
            return;
        }

        // 检查内容是否含有附件
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++) {
                this.getMailContent(stringBuilder, imgs, mp.getBodyPart(i));
            }
            return;
        }

        // 检查内容是否含有嵌套消息
        if (part.isMimeType("message/rfc822")) {
            this.getMailContent(stringBuilder, imgs, (Part) part.getContent());
            return;
        }

        // 检查内容是否为内嵌图片
        if (part.isMimeType("image/*")) {
            Object content = part.getContent();
            String contentId = (part.getHeader("Content-ID"))[0];
            InputStream in = (InputStream) content;
            byte[] bArray = new byte[in.available()];
            while ((in).available() > 0) {
                int result = ((in).read(bArray));
                if (result == -1) {
                    break;
                }
            }
            in.close();
            // 文件下载开始
            int i = imgs.size();
            String fileName = "/tmp/" + i + ".jpg";
            FileOutputStream f2 = new FileOutputStream(fileName);
            f2.write(bArray);
            f2.close();
            in.close();
            imgs.put(contentId, fileName);
            // 文件下载结束
            return;
        }

        log.warn("This is an unknown type:" + part.getContentType());
    }

}
