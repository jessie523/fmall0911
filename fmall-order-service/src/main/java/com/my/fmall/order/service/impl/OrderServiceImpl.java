package com.my.fmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.my.fmall.bean.OrderDetail;
import com.my.fmall.bean.OrderInfo;
import com.my.fmall.config.ActiveMQUtil;
import com.my.fmall.config.RedisUtil;
import com.my.fmall.enums.ProcessStatus;
import com.my.fmall.order.mapper.OrderDetailMapper;
import com.my.fmall.order.mapper.OrderInfoMapper;
import com.my.fmall.util.HttpClientUtil;
import com.my.fmall0911.service.OrderService;
import com.my.fmall0911.service.IPaymentService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

/**
 * author:zxy
 *
 * @create 2021-10-08 14:03
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Reference
    private IPaymentService paymentService;

    @Transactional
    @Override
    public String saveOrder(OrderInfo orderInfo) {

        //设置创建时间
        orderInfo.setCreateTime(new Date());
        //设置过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        //生成第三方支付编号
        String outTradeNo = "my"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfoMapper.insertSelective(orderInfo);

        //订单详情
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        for (OrderDetail orderDetail : orderDetailList) {
             orderDetail.setOrderId(orderInfo.getId());
             orderDetailMapper.insertSelective(orderDetail);
        }

        //为了跳转到支付页面使用。会根据订单id进行支付
        String orderId = orderInfo.getId();

        return orderId;
    }

    /**
     * 生成流水号
     * 防止重复提交
     * @return
     */
    @Override
    public String getTradeNo(String userId) {
        Jedis jedis = redisUtil.getJedis();
        //生成key
        String tradeNoKey =  "user:"+userId+":tradeCode";
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeNoKey,10*60,tradeCode);
        jedis.close();


        return tradeCode;
    }

    /**
     * 验证流水号
     * @return
     */
    @Override
    public boolean checkTradeNo(String userId,String tradeCodeNo){
        Jedis jedis = redisUtil.getJedis();
        String tradeKey = "user:"+userId+":tradeCode";
        String tradeCode = jedis.get(tradeKey);

        if(StringUtils.isNotEmpty(tradeCode) && tradeCode.equals(tradeCodeNo)){
            return true;
        }

        return false;

    }

    /**
     * 删除流水号
     * @param userId
     */
    @Override
    public void delTradeNo(String userId){
        Jedis jedis = redisUtil.getJedis();
        String tradeKey = "user:"+userId+":tradeCode";

        jedis.del(tradeKey);

        jedis.close();

    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {

        String url = "http://www.gware.com/hasStock?skuId="+skuId+"&num="+skuNum;

        String res = HttpClientUtil.doGet(url);
        if("1".equals(res)){
            return false;
        }else{
            return false;
        }
    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);

        //将orderDetail放入OrderInfo
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);

        orderInfo.setOrderDetailList(orderDetailMapper.select(orderDetail));

        return orderInfo;
    }

    @Override
    public void updateOrderStatus(String orderId, ProcessStatus paid) {
        // update orderInfo set processStatus = paid , ordersStatus = paid where id = orderId;
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(paid);
        orderInfo.setOrderStatus(paid.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    /**
     * 减少库存
     * @param orderId
     */
    @Override
    public void sendOrderStatus(String orderId) {
        //创建消息工厂
        Connection connection = activeMQUtil.getConnection();
        String orderInfoJson = initWareOrder(orderId);
        try {
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建队列
            Queue queue = session.createQueue("ORDER_RESULT_QUEUE");
            //创建消息提供者
            MessageProducer producer = session.createProducer(queue);
            //创建消息对象
            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
            //orderInfo组成json字符串
            textMessage.setText(orderInfoJson);

            producer.send(textMessage);
            session.commit();

            //关闭
            connection.close();
            producer.close();
            session.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
    
    private String initWareOrder(String orderId) {
        
        //根据orderId查询orderInfo
        OrderInfo orderInfo = getOrderInfo(orderId);
        //将orderInfo中有用的信息保存到map中
        Map<String,Object> map = initWareOrder(orderInfo);
        // 将map 转换为json  字符串！
        return JSON.toJSONString(map);
    }

    private Map<String, Object> initWareOrder(OrderInfo orderInfo) {

        HashMap<String, Object> map = new HashMap<>();
        // 给map 的key 赋值！
        map.put("orderId",orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody","测试用例");
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");
        map.put("wareId",orderInfo.getWareId()); // 仓库Id

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        ArrayList<Object> arrayList = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetailList) {
            HashMap<String, Object> map1 = new HashMap<>();

            map1.put("skuId",orderDetail.getSkuId());
            map1.put("skuNum",orderDetail.getSkuNum());
            map1.put("skuName",orderDetail.getSkuName());
            arrayList.add(map1);
        }
        map.put("details",arrayList);
        return map;
    }

    /**
     * 查询所有过期订单（当前时间> 过期时间 and 当前订单状态为未支付）
     * @return
     */
    @Override
    public List<OrderInfo> getExpiredOrderList() {

        Example example = new Example(OrderInfo.class);
        example.createCriteria().andLessThan("expireTime",new Date()).andEqualTo("processStatus",ProcessStatus.UNPAID);

        List<OrderInfo> orderInfoList = orderInfoMapper.selectByExample(example);
        return orderInfoList;
    }

    @Async//多线程实现异步并发
    @Override
    public void execExpiredOrder(OrderInfo orderInfo) {

        //将订单状态改为关闭
        updateOrderStatus(orderInfo.getId(),ProcessStatus.CLOSED);

        //关闭paymentInfo
        paymentService.closePayment(orderInfo.getId());
    }
}
