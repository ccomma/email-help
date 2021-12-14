package com.awesomecat.verificationcodetransmit.service;

import com.awesomecat.verificationcodetransmit.bo.MailInfo;

/**
 * 收到邮件后统一回调此接口，业务方如需使用，实现此接口即可
 *
 * @author awesomecat
 */
public interface MailHandlerService {

    /**
     * 业务处理方法
     * <p>
     * 1、根据业务需要处理邮箱内容
     * 2、建议发件人入库，便于后续分析
     *
     * @param mailInfo
     * @return 返回处理状态, true 处理完成,邮件标记为已读
     */
    boolean doHandler(MailInfo mailInfo);
}
