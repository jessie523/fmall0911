package com.my.fmall.payment;

import com.my.fmall.config.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FmallPaymentApplicationTests {

//    @Value("${spring.activemq.broker-url}")
//    private String brokerUrl;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Test
    public void testM() throws JMSException {
        //获取连接

        Connection connection = activeMQUtil.getConnection();
        connection.start();
        //创建session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("fmall");
        //创建消息消息生产者
        MessageProducer producer = session.createProducer(queue);
        //创建消息对象
        ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
        activeMQTextMessage.setText("什么时候是个头");

        producer.send(activeMQTextMessage);

        producer.close();
        connection.close();
        session.close();


    }

}
