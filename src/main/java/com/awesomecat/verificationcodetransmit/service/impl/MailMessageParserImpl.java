package com.awesomecat.verificationcodetransmit.service.impl;

import com.awesomecat.verificationcodetransmit.bo.MailInfo;
import com.awesomecat.verificationcodetransmit.service.MailMessageParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 信息解析器
 *
 * @author mianXian
 * @date 2021/12/14 14:52
 */
@Slf4j
@Service
public class MailMessageParserImpl implements MailMessageParser {

    @Override
    public MailInfo parseMessage(MimeMessage mimeMessage) {
        Folder folder = mimeMessage.getFolder();
        try {
            if (!folder.isOpen()) {
                return null;
            }

            MailInfo mailInfo = new MailInfo();
            mailInfo.setFrom(this.getFrom(mimeMessage));
            mailInfo.setTo(this.getAddress(this.getReceiveMailAddress(Message.RecipientType.TO, mimeMessage)));
            mailInfo.setCc(this.getAddress(this.getReceiveMailAddress(Message.RecipientType.CC, mimeMessage)));
            mailInfo.setBcc(this.getAddress(this.getReceiveMailAddress(Message.RecipientType.BCC, mimeMessage)));
            mailInfo.setSubject(MimeUtility.decodeText(mimeMessage.getSubject()));
            mailInfo.setSentDate(mimeMessage.getSentDate());
            mailInfo.setReceivedDate(mimeMessage.getReceivedDate());
            mailInfo.setBodyText(this.getBodyText(mimeMessage));
            mailInfo.setMessageId(mimeMessage.getMessageID());
            mailInfo.setBox(mimeMessage.getFolder().getName());

            return mailInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getFrom(MimeMessage mimeMessage) throws MessagingException {
        InternetAddress[] address = (InternetAddress[]) mimeMessage.getFrom();
        String from = address[0].getAddress();
        if (from == null) {
            log.warn("mail address is null!");
            return StringUtils.EMPTY;
        }

        return from;
    }

    private String getAddress(String personAddress) {
        if (StringUtils.isEmpty(personAddress)) {
            return StringUtils.EMPTY;
        }

        String regex = ".*<(.*)>.*";
        Pattern pattern = Pattern.compile(regex);
        String[] split = personAddress.split(";");

        String[] resultString = Arrays.stream(split)
                .filter(s -> pattern.matcher(s).matches())
                .map(s -> pattern.matcher(s).group(1))
                .toArray(String[]::new);

        return StringUtils.join(resultString, ";");
    }

    /**
     * 获得邮件的收件人，抄送，和密送的地址和姓名
     * <p> 格式：{@code name<address>,name<address>}
     */
    private String getReceiveMailAddress(Message.RecipientType type, MimeMessage mimeMessage) throws MessagingException, UnsupportedEncodingException {
        Assert.notNull(type, "RecipientType must not be null");
        InternetAddress[] addressList = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.TO);
        if (addressList == null) {
            return StringUtils.EMPTY;
        }

        List<String> resultList = new ArrayList<>();
        for (InternetAddress address : addressList) {
            String emailAddress = MimeUtility.decodeText(address.getAddress());
            String personal = MimeUtility.decodeText(address.getPersonal());
            resultList.add(personal + "<" + emailAddress + ">");
        }
        return StringUtils.join(resultList, ",");
    }

    private String getBodyText(MimeMessage mimeMessage) throws Exception {
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
     * <p>
     * 解析邮件
     * </p>
     *
     * <pre>
     *  主要是根据 MimeType 类型的不同执行不同的操作，一步一步的解析
     *  把得到的邮件内容保存到一个 StringBuilder 对象中
     * </pre>
     *
     * @throws MessagingException
     * @throws Exception
     */
    private void getMailContent(StringBuilder sb, Map<String, String> imgs, Part part) throws Exception {
        // 检查内容是否为纯文本
        if (part.isMimeType("text/plain")) {
            log.warn("skip text plain");
            return;
        }

        // 检查内容是否为 html
        if (part.isMimeType("text/html")) {
            sb.append(part.getContent());
            return;
        }

        // 检查内容是否含有附件
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++) {
                this.getMailContent(sb, imgs, mp.getBodyPart(i));
            }
            return;
        }

        // 检查内容是否含有嵌套消息
        if (part.isMimeType("message/rfc822")) {
            this.getMailContent(sb, imgs, (Part) part.getContent());
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
