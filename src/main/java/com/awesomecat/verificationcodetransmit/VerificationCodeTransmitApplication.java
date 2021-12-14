package com.awesomecat.verificationcodetransmit;

import com.awesomecat.verificationcodetransmit.config.MailReceiverConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(MailReceiverConfigurationProperties.class)
@SpringBootApplication
public class VerificationCodeTransmitApplication {

    public static void main(String[] args) {
        SpringApplication.run(VerificationCodeTransmitApplication.class, args);
    }

}
