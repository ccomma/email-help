package com.awesomecat.verificationcodetransmit.bo;

import lombok.Data;
import lombok.ToString;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import java.io.UnsupportedEncodingException;

/**
 * 身份信息
 *
 * @author awesomecat
 * @date 2021/12/14 18:25
 */
@Data
@ToString
public class PersonInfo {
    /** 名称 */
    private String name;

    /** 邮箱地址 */
    private String address;

    public PersonInfo() {
        // pojo
    }

    public static PersonInfo of(InternetAddress internetAddress) {
        PersonInfo personInfo = new PersonInfo();
        try {
            personInfo.setName(MimeUtility.decodeText(internetAddress.getPersonal()));
            personInfo.setAddress(MimeUtility.decodeText(internetAddress.getAddress()));
            return personInfo;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

}
