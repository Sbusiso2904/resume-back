package wethinkcode.loadshed.common.mq.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import wethinkcode.loadshed.common.mq.MqTopicSender;

import javax.jms.*;

public class NullTopicSender {

    private Connection connection;
    private Session session;
    private MessageProducer producer;

    public NullTopicSender init(String topicName) {
        try {
            ActiveMQConnectionFactory factory =
                    new ActiveMQConnectionFactory("tcp://localhost:61616");

            connection = factory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            producer = session.createProducer(topic);

            connection.start();
        }
        catch (JMSException e) {
            throw new RuntimeException(e);
        }
        return  this;
    }

    public void send(String messageText) {
        try {
            TextMessage message = session.createTextMessage(messageText);
            producer.send(message);
        }
        catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            if (producer != null) producer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        }
        catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
