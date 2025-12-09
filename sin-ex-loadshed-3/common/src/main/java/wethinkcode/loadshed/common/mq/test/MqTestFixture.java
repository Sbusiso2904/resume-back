package wethinkcode.loadshed.common.mq.test;

import org.apache.activemq.broker.BrokerService;
import wethinkcode.loadshed.common.mq.MqTopicReceiver;
import wethinkcode.loadshed.common.mq.MqTopicSender;

public class MqTestFixture {

    private BrokerService broker;

    public MqTestFixture startBroker() throws Exception {
        try{
            broker = new BrokerService();
            broker.setPersistent(false);
            broker.setUseJmx(false);
            broker.addConnector("tcp://localhost:61616");
            broker.start();
            broker.waitUntilStarted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public void stopBroker() {
        try{
            if (broker != null) {
                broker.stop();
                broker = null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public MqTopicSender createSender() {
        try{
            MqTopicSender sender = new MqTopicSender();
            sender.init("tcp://localhost:61616");
            return sender;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public MqTopicReceiver createReceiver(String topic, javax.jms.MessageListener listener) throws Exception {
        try{
            MqTopicReceiver receiver = new MqTopicReceiver();
            receiver.init(topic, listener);
            return receiver;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
