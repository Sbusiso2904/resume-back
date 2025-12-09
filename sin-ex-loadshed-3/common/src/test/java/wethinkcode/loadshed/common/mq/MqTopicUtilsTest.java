package wethinkcode.loadshed.common.mq;

import org.junit.jupiter.api.*;
import wethinkcode.loadshed.common.mq.test.MqTestFixture;


public class MqTopicUtilsTest {

    private static MqTestFixture mqTestFixture;
    @BeforeAll
    public static void setUp() throws Exception {
        mqTestFixture = new MqTestFixture().startBroker();
    }

    @AfterAll
    public static void tearDown(){
        mqTestFixture.stopBroker();
}


    @Test
    public void testingMqTopicReceiver() throws Exception {
        MqTopicReceiver mqTopicReceiver = new MqTopicReceiver();
        MqTopicSender mqTopicSender = new MqTopicSender();
        MqTestFixture mqTestFixture  = new MqTestFixture().startBroker();
    }
}


