package com.lgy.kill.server.service.impl;

import com.lgy.kill.model.entity.ItemKill;
import com.lgy.kill.model.entity.ItemKillSuccess;
import com.lgy.kill.model.mapper.ItemKillMapper;
import com.lgy.kill.model.mapper.ItemKillSuccessMapper;
import com.lgy.kill.server.enums.SysConstant;
import com.lgy.kill.server.service.IKillService;
import com.lgy.kill.server.service.RabbitSenderService;
import com.lgy.kill.server.utils.RandomUtil;
import com.lgy.kill.server.utils.SnowFlake;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.joda.time.DateTime;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class KillService implements IKillService {

    private static final Logger log = LoggerFactory.getLogger(KillService.class);
    private SnowFlake snowFlake = new SnowFlake(2,3);
    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;
    @Autowired
    private ItemKillMapper itemKillMapper;
    @Autowired
    private RabbitSenderService rabbitSenderService;

    /**
     * 可发送邮件 --没优化
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItem(Integer killId, Integer userId) throws Exception {
        //TODO:判断当前用户是否已经抢购过当前商品
        if(itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
            //TODO:查询要想要秒杀的商品的详情
            ItemKill itemKill = itemKillMapper.selectById(killId);
            //TODO:
            if(itemKill != null && 1 == itemKill.getCanKill()){
                //TODO: 库存扣减1
                int res = itemKillMapper.updateKillItem(killId);
                if(res>0){
                    //TODO:扣减成功发送信息通知用户
                    System.out.println("成功扣减");
                    this.commonRecordKillSuccessInfo(itemKill,userId);
                }
                return true;
            }
        }else{
            throw new Exception("您已经抢购过该商品了!");
        }
        return false;
    }

    /**
     * 通用的方法-记录用户秒杀成功后生成的订单-并进行异步邮件消息的通知
     * @param kill
     * @param userId
     * @throws Exception
     */
    private void commonRecordKillSuccessInfo(ItemKill kill,Integer userId) throws Exception{
        //TODO:记录抢购成功后生成的秒杀订单记录
        ItemKillSuccess itemKillSuccessEntity=new ItemKillSuccess();
        String orderNo=String.valueOf(snowFlake.nextId());
        //itemKillSuccessEntity.setCode(RandomUtil.generateOrderCode());   //传统时间戳+N位随机数
        itemKillSuccessEntity.setCode(orderNo);
        itemKillSuccessEntity.setItemId(kill.getItem_id());
        itemKillSuccessEntity.setKillId(kill.getId());
        itemKillSuccessEntity.setUserId(userId.toString());
        itemKillSuccessEntity.setStatus(SysConstant.OrderStatus.SuccessNotPayed.getCode().byteValue());
        itemKillSuccessEntity.setCreateTime(DateTime.now().toDate());

        //TODO: 仿照单例模式的双重检测--mysql优化
        if(itemKillSuccessMapper.countByKillUserId(kill.getId(),userId) <= 0){
            int res = itemKillSuccessMapper.insertSelective(itemKillSuccessEntity);
            if(res>0){
                //TODO:进行异步邮件消息的通知=rabbitMQ+mail
                rabbitSenderService.sendKillSuccessEmailMsg(orderNo);
                //TODO:入死信队列，用于失效超过指定时间没有支付的订单
                rabbitSenderService.sendKillSuccessOrderExpireMsg(orderNo);
            }
        }
    }

    /**
     * MySQL优化
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV2(Integer killId, Integer userId) throws Exception {
        //TODO:判断当前用户是否已经抢购过当前商品
        if(itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
            //TODO:A.查询要想要秒杀的商品的详情
            ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
            //TODO: 判断是否可以被秒杀cankill=1
            if(itemKill != null && 1 == itemKill.getCanKill()){
                //TODO: B.库存扣减1
                int res = itemKillMapper.updateKillItemV2(killId);
                if(res>0){
                    //TODO:扣减成功发送信息通知用户
                    System.out.println("成功扣减");
                    this.commonRecordKillSuccessInfo(itemKill,userId);
                }
                return true;
            }
        }else{
            throw new Exception("您已经抢购过该商品了!");
        }
        return false;
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /**
     * Redis分布式锁优化
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV3(Integer killId, Integer userId) throws Exception {
        //TODO:判断当前用户是否已经抢购过当前商品
        if(itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
            //TODO: 借助Redis的原子操作实现分布式锁
            //TODO:第一步设置锁
            ValueOperations valueOperations = stringRedisTemplate.opsForValue();
            final String key = new StringBuffer().append(killId).append(userId).append("-RedisLock").toString();
            final String value = RandomUtil.generateOrderCode();
            //TODO:第二步获取锁
            Boolean cacheRes = valueOperations.setIfAbsent(key,value);
            //TODO: redis部署节点可能宕机
            if(cacheRes){
                stringRedisTemplate.expire(key,30, TimeUnit.SECONDS);
                try{
                    //TODO:A.查询要想要秒杀的商品的详情
                    ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
                    //TODO: 判断是否可以被秒杀cankill=1
                    if(itemKill != null && 1 == itemKill.getCanKill()){
                        //TODO: B.库存扣减1
                        int res = itemKillMapper.updateKillItemV2(killId);
                        if(res>0){
                            //TODO:扣减成功发送信息通知用户
                            System.out.println("成功扣减");
                            this.commonRecordKillSuccessInfo(itemKill,userId);
                        }
                        return true;
                    }
                }catch(Exception e){
                    throw new Exception("Redis--还没到抢购日期、已过了抢购时间或已被抢购完毕！");
                }finally {
                    //TODO:释放锁
                    if(value.equals(valueOperations.get(key).toString())){
                        stringRedisTemplate.delete(key);
                    }
                }
            }
        }else{
            throw new Exception("Redis--您已经抢购过该商品了!");
        }
        return false;
    }

    @Autowired
    private RedissonClient redissonClient;
    /**
     * Redisson--分布式锁
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV4(Integer killId, Integer userId) throws Exception {
        final String lockKey = new StringBuffer().append(killId).append(userId).append("--RedissonLock").toString();
        RLock lock = redissonClient.getLock(lockKey);
        try{
            Boolean cacheRes = lock.tryLock(30,10,TimeUnit.SECONDS);
            if(cacheRes){
                //TODO:核心业务逻辑
                if(itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
                    ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
                    if(itemKill != null && 1 == itemKill.getCanKill()){
                        //TODO: B.库存扣减1
                        int res = itemKillMapper.updateKillItemV2(killId);
                        if(res>0){
                            //TODO:扣减成功发送信息通知用户
                            System.out.println("成功扣减");
                            this.commonRecordKillSuccessInfo(itemKill,userId);
                            return true;
                        }
                    }
                }else{
                    throw new Exception("Redisson--您已经抢购过该商品了!");
                }
            }
        }catch(Exception e){
            throw new Exception("还没到抢购日期、已过了抢购时间或已被抢购完毕！");
        }finally {
            lock.unlock();
        }
//        //TODO:判断当前用户是否已经抢购过当前商品
//        if(itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
//            //TODO: 借助Redis的原子操作实现分布式锁
//            //TODO:第一步设置锁
//            ValueOperations valueOperations = stringRedisTemplate.opsForValue();
//            final String key = new StringBuffer().append(killId).append(userId).append("-RedisLock").toString();
//            final String value = RandomUtil.generateOrderCode();
//            //TODO:第二步获取锁
//            Boolean cacheRes = valueOperations.setIfAbsent(key,value);
//            //TODO: redis部署节点可能宕机
//            if(cacheRes){
//                stringRedisTemplate.expire(key,30, TimeUnit.SECONDS);
//                try{
//                    //TODO:A.查询要想要秒杀的商品的详情
//                    ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
//                    //TODO: 判断是否可以被秒杀cankill=1
//                    if(itemKill != null && 1 == itemKill.getCanKill()){
//                        //TODO: B.库存扣减1
//                        int res = itemKillMapper.updateKillItemV2(killId);
//                        if(res>0){
//                            //TODO:扣减成功发送信息通知用户
//                            System.out.println("成功扣减");
//                            this.commonRecordKillSuccessInfo(itemKill,userId);
//                        }
//                        return true;
//                    }
//                }catch(Exception e){
//                    throw new Exception("Redis--还没到抢购日期、已过了抢购时间或已被抢购完毕！");
//                }finally {
//                    //TODO:释放锁
//                    if(value.equals(valueOperations.get(key).toString())){
//                        stringRedisTemplate.delete(key);
//                    }
//                }
//            }
//        }else{
//            throw new Exception("Redisson--您已经抢购过该商品了!");
//        }
        return false;
    }

    @Autowired
    private CuratorFramework curatorFramework;
    public static final String pathPrefix = "/kill/zklock/";
    /**
     * 基于Zookeeper的分布式锁的实现
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV5(Integer killId, Integer userId) throws Exception {
        InterProcessMutex mutex = new InterProcessMutex(curatorFramework,pathPrefix+killId+userId+"-lock");
        try{
            if(mutex.acquire(10L,TimeUnit.SECONDS)){
                //TODO:核心业务逻辑
                if(itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
                    ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
                    if(itemKill != null && 1 == itemKill.getCanKill()){
                        //TODO: B.库存扣减1
                        int res = itemKillMapper.updateKillItemV2(killId);
                        if(res>0){
                            //TODO:扣减成功发送信息通知用户
                            System.out.println("成功扣减");
                            this.commonRecordKillSuccessInfo(itemKill,userId);
                            return true;
                        }
                    }
                }else{
                    throw new Exception("Redisson--您已经抢购过该商品了!");
                }
            }
        }catch(Exception e){
            throw new Exception("还没到抢购日期、已过了抢购时间或已被抢购完毕！");
        }finally {
            if(mutex!=null){
                mutex.release();
            }
        }
        return false;
    }
}
