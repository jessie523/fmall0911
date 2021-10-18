package com.my.fmall.payment.mq;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * author:zxy
 *
 * @create 2021-10-12 13:21
 */
public class ConsumerTest {

    public static void main(String[] args) throws JMSException {
        /*
        * 1、创建连接工厂
        * 2、创建连接
        * 3、打开连接
        * 4、创建session
        * 5、创建队列
        * 6、创建消费者
        * 7、接收(消费)消息
        * */
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.91.128:61616");
        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();
        //创建session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //创建队列
        Queue queue = session.createQueue("fmall");
        //创建consumer
        MessageConsumer consumer = session.createConsumer(queue);
        //接收消息
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {

                //如何获取消息
                if(message instanceof TextMessage){
                    try {
                        String text = ((TextMessage) message).getText();
                        System.out.println("接收消息："+text);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

    }
}
