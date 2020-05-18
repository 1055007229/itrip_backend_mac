package cn.itrip.auth.service.impl;

import cn.itrip.auth.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    SimpleMailMessage message;

    @Autowired
    JavaMailSenderImpl sender;

    @Override
    public void sendMail(String to, String activationCode) {
        // 内容
        message.setText("您的邮箱" + to + "注册验证码：" + activationCode);
        // 接收人
        message.setTo(to);
        // 发送
        sender.send(message);
    }
}
