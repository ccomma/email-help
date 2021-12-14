package com.awesomecat.verificationcodetransmit.receiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 监听邮箱
 * <p> 项目启动后执行
 *
 * @author awesomecat
 */
@Component
public class MailListenerCommandLineRunner implements CommandLineRunner {

    @Autowired
    private MailListener mailListener;

    @Override
    public void run(String... args) throws Exception {
        // 异步监听邮箱
        mailListener.asyncListener();
    }

}
