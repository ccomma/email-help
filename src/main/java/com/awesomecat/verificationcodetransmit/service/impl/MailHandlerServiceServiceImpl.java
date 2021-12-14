package com.awesomecat.verificationcodetransmit.service.impl;

import com.awesomecat.verificationcodetransmit.bo.MailInfo;
import com.awesomecat.verificationcodetransmit.service.MailHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 业务处理的方法
 *
 * @author awesomecat
 */
@Slf4j
@Service
public class MailHandlerServiceServiceImpl implements MailHandlerService {

    @Override
    public boolean doHandler(MailInfo mailInfo) {
        log.info("you have a message:[{}]", mailInfo.toString());
        return true;
    }
}
