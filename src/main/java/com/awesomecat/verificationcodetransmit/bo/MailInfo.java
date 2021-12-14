package com.awesomecat.verificationcodetransmit.bo;

import com.awesomecat.verificationcodetransmit.enums.RecipientTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.mail.Folder;
import javax.mail.Header;
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
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 邮件内容解析
 *
 * @author awesomecat
 */
@Slf4j
@Getter
@Setter
@ToString
public class MailInfo {

    private MimeMessage mimeMessage;
    /**
     * 邮件内容
     */
    private String bodyText = "";
    /**
     * 发件人
     */
    private String from;
    private String to;
    private String cc;
    private String bcc;
    private String subject;
    private Date sentDate;
    private Date receivedDate;
    private String messageId;
    private String box;

    public static MailInfo of(Message mimeMessage) {
        MailInfo mailInfo = new MailInfo();
        mailInfo.mimeMessage = (MimeMessage) mimeMessage;
        mailInfo.parseMessage();
        return mailInfo;
    }

    /**
     * 解析邮件的内容
     */
    // TODO: 2021/12/14 移至解析类
    private void parseMessage() {
        Folder folder = this.mimeMessage.getFolder();
        try {
            if (!folder.isOpen()) {
                return;
            }

            this.from = this.genFrom();
            this.to = address(getMailAddress(RecipientTypeEnum.TO));
            this.cc = address(getMailAddress(RecipientTypeEnum.CC));
            this.bcc = address(getMailAddress(RecipientTypeEnum.BCC));
            this.subject = MimeUtility.decodeText(mimeMessage.getSubject());
            this.sentDate = mimeMessage.getSentDate();
            this.receivedDate = mimeMessage.getReceivedDate();

            // 邮件内容解析开始>>>>>>>存放邮件内容的StringBuffer对象
            StringBuffer bodyTextBuf = new StringBuffer();
            Map<String, String> imgs = new HashMap<>();
            getMailContent(bodyTextBuf, imgs, this.mimeMessage);
            String tmpContent = bodyTextBuf.toString();
            // 替换原文中的图片，原始图片标签为<img src="cid:image001.jpg@01D24963.3B4B8280">
            for (Map.Entry<String, String> entry : imgs.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                String replacedText = "cid:" + key.replace("<", "").replace(">", "");
                tmpContent = tmpContent.replace(replacedText, value);
            }
            bodyText = tmpContent;
            // <<<<<<<<邮件内容解析结束
            this.messageId = mimeMessage.getMessageID();
            this.box = mimeMessage.getFolder().getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String address(String personaddress) {
        if (StringUtils.isEmpty(personaddress)) {
            return "";
        }
        String regex = ".*<(.*)>.*";
        Pattern p = Pattern.compile(regex);
        List<String> list = new ArrayList<>();
        for (String pa : personaddress.split(";")) {
            Matcher matcher = p.matcher(pa);
            if (matcher.matches()) {
                String addr = matcher.group(1);
                list.add(addr);
            }
        }
        return StringUtils.join(list.toArray(new String[0]), ";");
    }

    private String genFrom() throws MessagingException {
        InternetAddress[] address = (InternetAddress[]) mimeMessage.getFrom();
        String addressFrom = address[0].getAddress();
        if (addressFrom == null) {
            addressFrom = "";
            log.warn("发送人邮箱地址为空!");
        }
        return addressFrom;
    }

    /**
     * 　*　获得发件人的地址和姓名
     */
    public String getFrom() {
        return from;
    }

    /**
     * 收件人
     *
     * @return
     */
    public String getTo() {
        return this.to;
    }

    /**
     * 抄送人
     *
     * @return
     */
    public String getCc() {
        return this.cc;
    }

    /**
     * 私密抄送
     *
     * @return
     */
    public String getBcc() {
        return this.bcc;
    }

    /**
     * <p>
     * 获得邮件的收件人，抄送，和密送的地址和姓名，根据所传递的参数的不同
     * </p>
     *
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    private String getMailAddress(RecipientTypeEnum type) throws MessagingException, UnsupportedEncodingException {
        String mailAddr = "";
        InternetAddress[] address = null;
        switch (type) {
            case TO:
                address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.TO);
                break;
            case CC:
                address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.CC);
                break;
            case BCC:
                address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.BCC);
                break;
        }
        if (address != null) {
            String[] addrs = new String[address.length];
            int i = 0;
            for (InternetAddress addr : address) {
                String emailAddr = MimeUtility.decodeText(addr.getAddress());
                String personal = MimeUtility.decodeText(addr.getPersonal());
                addrs[i++] = personal + "<" + emailAddr + ">";
            }
            mailAddr = StringUtils.join(addrs, ",");
        }
        return mailAddr;
    }

    /**
     * 获得邮件主题
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * 邮件发送日期
     *
     * @return
     */
    public Date getSentDate() {
        return this.sentDate;
    }

    /**
     * 邮件接收日期
     *
     * @return
     */
    public Date getReceivedDate() {
        return this.receivedDate;
    }

    /**
     * 获得邮件正文内容
     *
     * @return
     */
    public String getBodyText() {
        return bodyText;
    }

    /**
     * <p>
     * 解析邮件
     * </p>
     *
     * <pre>
     *  主要是根据MimeType类型的不同执行不同的操作，一步一步的解析
     *  把得到的邮件内容保存到一个StringBuffer对象中
     * </pre>
     *
     * @throws MessagingException
     * @throws Exception
     */
    public static void getMailContent(StringBuffer sb, Map<String, String> imgs, Part p) throws Exception {
        // 检查内容是否为纯文本
        if (p.isMimeType("text/plain")) {
            log.warn("skip text plain");
        }
        // 检查内容是否为html
        else if (p.isMimeType("text/html")) {
            sb.append(p.getContent());
        }
        // 检查内容是否含有附件
        else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++) {
                getMailContent(sb, imgs, mp.getBodyPart(i));
            }
        }
        // 检查内容是否含有嵌套消息
        else if (p.isMimeType("message/rfc822")) {
            getMailContent(sb, imgs, (Part) p.getContent());
        }
        // 检查内容是否为内嵌图片
        else if (p.isMimeType("image/*")) {
            Object content = p.getContent();
            String contentId = (p.getHeader("Content-ID"))[0];
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
        } else {
            log.warn("This is an unknown type:" + p.getContentType());
        }
    }

    /**
     * 　获得此邮件的Message-ID
     */
    public String getMessageId() {
        return this.messageId;
    }

    public String getBox() {
        return this.box;
    }

    public void printHeaders() {
        try {
            Enumeration<Header> allHeaders = mimeMessage.getAllHeaders();
            while (allHeaders.hasMoreElements()) {
                Header header = allHeaders.nextElement();
                System.out.println(header.getName() + "-->" + header.getValue());
            }

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}
