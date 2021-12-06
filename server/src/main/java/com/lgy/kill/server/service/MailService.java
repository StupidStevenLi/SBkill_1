package com.lgy.kill.server.service;


import com.lgy.kill.server.dto.MailDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

@Service
@EnableAsync
public class MailService {

    public static final Logger log = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private Environment env;

    @Autowired
    private JavaMailSender mailSender;

    /**
     * 发送简单文本文件
     */
    @Async
    public void sendSimpleEmail(final MailDto mailDto){
        try{
            SimpleMailMessage message = new SimpleMailMessage();

            //发送者
            message.setFrom(env.getProperty("mail.send.from"));
            //接收者
            message.setTo(mailDto.getTos());
            //主题
            message.setSubject(mailDto.getSubject());
            //正文
            message.setText(mailDto.getContent());

            mailSender.send(message);

            log.info("发送简单文本文件-发送成功!");
        }catch(Exception e){
            log.error("发送简单文本文件-发生异常： ",e.fillInStackTrace());
        }
    }

    /**
     * HTML
     * @param mailDto
     */
    @Async
    public void sendHTMLMail(final MailDto mailDto){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message,true,"utf-8");
            messageHelper.setFrom(env.getProperty("mail.send.from"));
            messageHelper.setTo(mailDto.getTos());
            messageHelper.setSubject(mailDto.getSubject());
            messageHelper.setText(mailDto.getContent(),true);
            mailSender.send(message);
            log.info("发送花哨邮件-发送成功!");
        }catch (Exception e){
            log.error("发送花哨邮件-发生异常： ",e.fillInStackTrace());
        }
    }
}
