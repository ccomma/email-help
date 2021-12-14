package com.awesomecat.emailhelp.task;

import com.awesomecat.emailhelp.service.MailHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.MessagingException;

/**
 * 邮件任务
 *
 * @author awesomecat
 * @date 2021/12/14 11:23
 */
@Slf4j
@Component
public class MailTask {

    @Resource
    private MailHandlerService mailHandlerService;

    /**
     * 心跳：每 10 分钟一次
     */
    @Scheduled(cron = "${mail.receiver.cron}")
    public void noop() throws MessagingException {
        mailHandlerService.tiktok();
    }

}
