package cn.itrip.auth.service;

public interface MailService {
    /**
     * 发邮件
     * @param to  接收的邮箱地址
     * @param activationCode   激活码
     */
    public void sendMail(String to,String activationCode);
}
