package com.awesomecat.emailhelp.strategy;

import com.awesomecat.emailhelp.bo.MailInfo;
import com.awesomecat.emailhelp.service.MailHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 转发验证码策略
 *
 * @author awesomecat
 * @date 2021/12/14 22:27
 */
@Slf4j
@Component
public class ForwardVerificationCodeStrategy implements ReceiveMailHandleStrategy {

    /** 默认接收人 */
    // TODO: 2021/12/15 后期移至配置中心或数据库
    private static final String[] RECIPIENT_STRING_ARRAY = {"782453374@qq.com", "229526881@qq.com", "1060686387@qq.com"};
    /** 发送主题 */
    private static final String SUBJECT = "bilibili 验证码转发";
    /** bilibili 地址 */
    private static final String BILIBILI_ADDRESS = "verify@service.bilibili.com";
    /** bilibili 标识 */
    private static final String BILIBILI_SIGN = "哔哩哔哩";
    /** 验证码标识 */
    private static final String VERIFICATION_CODE_SIGN = "验证码";
    /** 验证码正则 */
    private static final Pattern VERIFICATION_CODE_PATTERN = Pattern.compile("<strong>(\\d{6})</strong>");

    @Resource
    private MailHandlerService mailHandlerService;

    @Override
    public void doHandle(MailInfo receiveMailInfo) {
        // bilibili 验证
        if (!isBilibiliVerificationCode(receiveMailInfo)) {
            return;
        }

        log.info("forward verification code start");
        try {
            MailInfo sendMailInfo = new MailInfo();
            // 发件人就是上一次的接收人
            sendMailInfo.setFrom(receiveMailInfo.getTo().get(0));
            sendMailInfo.setTo(this.getRecipientList());
            String verificationCode = this.getVerificationCode(receiveMailInfo);
            sendMailInfo.setContent(verificationCode + " 悠着点，不要乱登！");
            sendMailInfo.setSubject(SUBJECT + "：" + verificationCode);
            mailHandlerService.sendEmail(sendMailInfo);
        } catch (MessagingException e) {
            log.error("forward verification code error", e);
        }
    }

    /**
     * 判断是否是哔哩哔哩登录验证邮件
     *
     * @param receiveMailInfo 邮件信息
     * @return 是否是哔哩哔哩登录验证邮件
     * @author awesomecat
     * @date 2021/12/15 13:03
     */
    private boolean isBilibiliVerificationCode(MailInfo receiveMailInfo) {
        // 不包含验证码，不发送邮件
        if (!receiveMailInfo.getContent().contains(VERIFICATION_CODE_SIGN)) {
            return false;
        }

        // 发件人验证
        InternetAddress from = (InternetAddress) receiveMailInfo.getFrom();
        if (BILIBILI_ADDRESS.equals(from.getAddress()) || BILIBILI_SIGN.equals(from.getPersonal())) {
            return true;
        }

        // 主题验证
        return receiveMailInfo.getSubject().contains(BILIBILI_SIGN);
    }

    /**
     * 获取收件人列表
     *
     * @return 收件人列表
     * @author awesomecat
     * @date 2021/12/15 13:04
     */
    public List<Address> getRecipientList() {
        List<Address> resultList = new ArrayList<>();
        for (String recipient : RECIPIENT_STRING_ARRAY) {
            try {
                Address address = new InternetAddress(recipient);
                resultList.add(address);
            } catch (AddressException e) {
                e.printStackTrace();
            }
        }

        return resultList;
    }

    /**
     * 获取验证码
     *
     * @param receiveMailInfo 接收到的验证码邮件
     * @return 验证码
     * @author awesomecat
     * @date 2021/12/15 0:45
     */
    private String getVerificationCode(MailInfo receiveMailInfo) {
        Matcher matcher = VERIFICATION_CODE_PATTERN.matcher(receiveMailInfo.getContent());
        if (matcher.find()) {
            return matcher.group(1);
        }

        return StringUtils.EMPTY;
    }

}
