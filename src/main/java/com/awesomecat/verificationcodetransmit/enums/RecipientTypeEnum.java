package com.awesomecat.verificationcodetransmit.enums;

/**
 * 邮件接收类型枚举
 *
 * @author awesomecat
 */
public enum RecipientTypeEnum {
    /** 收件人 */
    TO("To"),

    /** 抄送 */
    CC("Cc"),

    /** 密件抄送 */
    BCC("Bcc");

    private final String code;

    RecipientTypeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

}
