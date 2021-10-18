package com.my.fmall.payment.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

/**
 * author:zxy
 *
 * @create 2021-10-12 13:04
 */
public class ProducerTest {

    public static void main(String[] args) throws JMSException {
        /*
        * 1、创建连接工厂
        * 2、创建连接
        * 3、打开连接
        * 4、创建session
        * 5、创建队列
        * 6、创建消息提供者
        * 7、创建消息对象
        * 8、发送消息
        * 9、关闭
        * */
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.91.128:61616");

        Connection connection = activeMQConnectionFactory.createConnection();

        connection.start();
        // 第一个参数：是否开启事务
        // 第二个参数：表示开启/关闭事务的相应配置参数，
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //创建队列
        Queue queue = session.createQueue("fmall");
        //创建消息提供者
        MessageProducer producer = session.createProducer(queue);
        //创建消息对象
        ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
        activeMQTextMessage.setText("hello fmall");

        //发送消息
        producer.send(activeMQTextMessage);//事务没有开启，执行send(),消息就进入到队列中
        producer.close();
        connection.close();

    }
}
