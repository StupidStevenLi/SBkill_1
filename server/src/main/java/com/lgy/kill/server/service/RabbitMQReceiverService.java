package com.lgy.kill.server.service;

import com.lgy.kill.model.dto.KillSuccessUserInfo;
import com.lgy.kill.model.entity.ItemKillSuccess;
import com.lgy.kill.model.mapper.ItemKillSuccessMapper;
import com.lgy.kill.server.dto.MailDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQReceiverService {
    public static final Logger log = LoggerFactory.getLogger(RabbitMQReceiverService.class);

    @Autowired
    private Environment env;

    @Autowired
    private MailService mailService;

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    /**
     * 秒杀异步邮件通知-接收消息
     * 单一消费者模式
     */
    @RabbitListener(queues = {"${mq.success.email.queue}"},containerFactory = "singleListenerContainer")
    public void consumeEmailMsg(KillSuccessUserInfo info){
        try{
            log.info("秒杀异步邮件通知-接收消息:{}",info);
            //TODO: 真正的发送邮件
            final String content = String.format(env.getProperty("mail.kill.item.success.content"),info.getItemName(),info.getCode());
            MailDto mailDto = new MailDto(env.getProperty("mail.kill.item.success.subject"),content,new String[]{info.getEmail()});
            mailService.sendHTMLMail(mailDto);
        }catch(Exception e){
            log.error("秒杀异步邮件通知-接收消息-发生异常",e.fillInStackTrace());
        }
    }

    /**
     * 用户秒杀成功后超时未支付-监听
     * @param info
     */
    @RabbitListener(queues = {"${mq.success.email.kill.dead.real.queue}"},containerFactory = "singleListenerContainer")
    public void consumeExpireOrder(KillSuccessUserInfo info){
        try{
            log.info("用户秒杀成功后超时未支付-监听接收:{}",info);

            if(info != null){
                ItemKillSuccess itemKillSuccessEntity = itemKillSuccessMapper.selectByPrimaryKey(info.getCode());
                //status == 0成功(未付款)状态下，进行失效订单的操作
                if(itemKillSuccessEntity != null && itemKillSuccessEntity.getStatus().intValue() == 0){
                    itemKillSuccessMapper.expireOrder(info.getCode());

                    MailDto mailDto = new MailDto(env.getProperty("mail.kill.item.success.subject"),"订单超时失效",new String[]{info.getEmail()});
                    mailService.sendHTMLMail(mailDto);
                }
            }
        }catch(Exception e){
            log.error("用户秒杀成功后超时未支付-监听接收息-发生异常",e.fillInStackTrace());
        }
    }
}
