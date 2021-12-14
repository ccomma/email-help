package com.awesomecat.emailhelp.task;

import com.awesomecat.emailhelp.service.MailHandlerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 监听邮箱
 * <p> 项目启动后执行
 *
 * @author awesomecat
 */
@Component
public class MailListenerCommandLineRunner implements CommandLineRunner {

    @Resource
    private MailHandlerService mailHandlerService;

    @Async
    @Override
    public void run(String... args) throws Exception {
        // 监听收件
        mailHandlerService.listen();
    }

}
