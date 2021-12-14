package com.awesomecat.emailhelp.strategy;

import com.awesomecat.emailhelp.bo.MailInfo;
import com.awesomecat.emailhelp.service.MailHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 转发验证码策略
 *
 * @author awesome
 * @date 2021/12/14 22:27
 */
@Slf4j
@Component
public class ForwardVerificationCodeStrategy implements ReceiveMailHandleStrategy {

    /** 默认接收人 */
    // TODO: 2021/12/15 后期移至配置中心或数据库
    private static final String[] RECIPIENT_STRING_ARRAY = {"", "", ""};
    /** 主题 */
    private static final String SUBJECT = "";

    @Resource
    private MailHandlerService mailHandlerService;

    @Override
    public void doHandle(MailInfo receiveMailInfo) {
        log.info("forward verification code start");
        try {
            MailInfo sendMailInfo = new MailInfo();
            // 发件人就是上一次的接收人
            sendMailInfo.setFrom(receiveMailInfo.getTo().get(0));
            sendMailInfo.setTo(this.getRecipientList());
            sendMailInfo.setSubject(SUBJECT);
            sendMailInfo.setContent(this.getVerificationCode(receiveMailInfo));
            mailHandlerService.sendEmail(sendMailInfo);
        } catch (MessagingException e) {
            log.error("forward verification code error", e);
        }
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
        // TODO: 2021/12/15 获取验证码
        return null;
    }

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

}
