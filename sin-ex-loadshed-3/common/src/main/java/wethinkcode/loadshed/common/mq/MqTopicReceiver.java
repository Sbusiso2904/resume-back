package wethinkcode.loadshed.common.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import wethinkcode.loadshed.common.mq.test.MqTestFixture;

import javax.jms.*;

public class MqTopicReceiver {

    private Connection connection;
    private Session session;
    private MessageConsumer consumer;

    MqTestFixture mqTestFixture;

    public MqTopicReceiver init(String topicName, MessageListener listener) {
        try{
            mqTestFixture = new MqTestFixture().startBroker();

            ActiveMQConnectionFactory connectionFactory =
                    new ActiveMQConnectionFactory("tcp://localhost:61616");

            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);

            consumer = session.createConsumer(topic);
            consumer.setMessageListener(listener);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public void close() {
        try {
            if (consumer != null) consumer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        } catch (Exception ignored) {}
    }
}
