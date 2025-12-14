package com.mall.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * 邮件发送工具类 (用于订单确认、找回密码等)
 */
public class EmailUtil {

    // ⚠️ 替换为你的邮件服务提供商信息
    private static final String HOST = "smtp.qq.com"; // 示例：QQ邮箱 SMTP
    private static final String PORT = "465"; // SSL 端口
    private static final String USERNAME = "244417287@qq.com"; // 你的邮箱账号
    private static final String PASSWORD = "oydpftbtjadtcbcj"; //

    public static void sendEmail(String toEmail, String subject, String content) {

        // 1. 配置邮件会话属性
        Properties props = new Properties();
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.ssl.enable", "true"); // 启用 SSL
        props.put("mail.smtp.auth", "true");

        // 2. 创建会话
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            // 3. 构建邮件内容
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME)); // 发件人
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail) // 收件人
            );
            message.setSubject(subject); // 邮件主题
            message.setText(content); // 邮件内容

            // 4. 发送邮件
            Transport.send(message);

            System.out.println("成功发送邮件到: " + toEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("发送邮件失败: " + e.getMessage());
        }
    }
}