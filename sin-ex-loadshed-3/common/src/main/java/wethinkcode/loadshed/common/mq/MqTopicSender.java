package wethinkcode.loadshed.common.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import wethinkcode.loadshed.common.mq.test.NullTopicSender;

import javax.jms.*;

public class MqTopicSender extends NullTopicSender{

    private Connection connection;
    private Session session;
    private MessageProducer producer;

    public MqTopicSender init(String topicName) {
        return  this;
    }


    public void send(String messageText) {
    }


    public void close() {

    }

}
