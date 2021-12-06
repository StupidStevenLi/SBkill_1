package com.lgy.kill.server.service;

import com.lgy.kill.model.dto.KillSuccessUserInfo;
import com.lgy.kill.model.mapper.ItemKillSuccessMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class RabbitSenderService {
    public static final Logger log = LoggerFactory.getLogger(RabbitSenderService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    public void sendKillSuccessEmailMsg(String orderNo){
        log.info("秒杀成功异步发送邮件通知消息-准备发送消息：{}",orderNo);

        try{
            if(!orderNo.isBlank()){
                KillSuccessUserInfo killSuccessUserInfo = itemKillSuccessMapper.selectByCode(orderNo);
                log.info("killinfo");
                if(killSuccessUserInfo != null){
                    log.info("killinfo!=null");
                    //TODO:rabbitmq发送消息的逻辑
                    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                    rabbitTemplate.setExchange(env.getProperty("mq.success.email.exchange"));
                    rabbitTemplate.setRoutingKey(env.getProperty("mq.success.email.routing.key"));
                    //TODO:将info充当消息发送至队列
                    rabbitTemplate.convertAndSend(killSuccessUserInfo, new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            MessageProperties messageProperties = message.getMessageProperties();
                            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            messageProperties.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME,KillSuccessUserInfo.class);
                            System.out.println("convert");
                            return message;
                        }
                    });
                }
            }
        }catch (Exception e){
            log.error("秒杀成功异步发送邮件通知消息-发生异常，消息为：{}",orderNo,e.fillInStackTrace());
        }
    }


    /**
     * 秒杀成功后生成抢购订单-发送消息入死信队列，等待着一定时间失效超时未支付的订单
     * @param orderNo
     */
    public void sendKillSuccessOrderExpireMsg(String orderNo){
        log.info("秒杀成功异步发送邮件通知消息-准备发送消息：{}",orderNo);

        try{
            if(!orderNo.isBlank()){
                KillSuccessUserInfo killSuccessUserInfo = itemKillSuccessMapper.selectByCode(orderNo);
                log.info("killinfo");
                if(killSuccessUserInfo != null){
                    log.info("killinfo!=null");
                    //TODO:rabbitmq发送消息的逻辑
                    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                    rabbitTemplate.setExchange(env.getProperty("mq.success.email.kill.dead.prod.exchange"));
                    rabbitTemplate.setRoutingKey(env.getProperty("mq.success.email.kill.dead.prod.routing.key"));
                    //TODO:将info充当消息发送至队列
                    rabbitTemplate.convertAndSend(killSuccessUserInfo, new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            MessageProperties messageProperties = message.getMessageProperties();
                            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            messageProperties.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME,KillSuccessUserInfo.class);
                            System.out.println("convert");

                            //TODO: 动态设置TTL 10s
                            messageProperties.setExpiration(env.getProperty("mq.success.email.kill.expire"));
                            return message;
                        }
                    });
                }
            }
        }catch (Exception e){
            log.error("秒杀成功异步发送邮件通知消息-发生异常，消息为：{}",orderNo,e.fillInStackTrace());
        }
    }
}
