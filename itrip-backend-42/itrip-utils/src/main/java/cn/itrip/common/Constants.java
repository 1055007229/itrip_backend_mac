package cn.itrip.common;

/***
 * 常量类 放置一些常量
 */
public class Constants {

    //默认起始页
    public static final Integer DEFAULT_PAGE_NO = 1;
    //默认页大小
    public static final Integer DEFAULT_PAGE_SIZE = 10;

    //设置邮箱激活码生命周期
    public static final Integer MAIL_CODE_TIMEOUT = 5 * 60;

    //设置邮箱激活码前缀
    public static final String MAIL_CODE_PREFIX = "MailCode:";

    //设置手机激活码前缀
    public static final String PHONE_CODE_PREFIX = "PhoneCode:";

    //设置手机激活码生命周期
    public static final Integer PHONE_CODE_TIMEOUT = 5 * 60;

    //token前缀
    public static final String TOKEN_PREFIX = "token:";

    //token生命周期
    public static final Integer TOKEN_CODE_TIMEOUT = 7200000;

    //token保护期
    public static final Integer TOKEN_REPLACEMENT_PROTECTED_TIME = 3600000;

    //token续命时间
    public static final Integer TOKEN_DELAY_TIME = 120;
}
