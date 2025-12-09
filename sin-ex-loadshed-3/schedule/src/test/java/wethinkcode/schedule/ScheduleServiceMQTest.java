package wethinkcode.schedule;

import org.junit.jupiter.api.Test;
import wethinkcode.loadshed.common.mq.MqTopicReceiver;
import wethinkcode.loadshed.common.mq.MqTopicSender;
import wethinkcode.loadshed.common.mq.test.MqTestFixture;
import wethinkcode.loadshed.common.mq.test.NullTopicReceiver;
import wethinkcode.stage.StageService;


public class ScheduleServiceMQTest {

    @Test
    public void TestingSchedule() {
        MqTestFixture mqTestFixture = new MqTestFixture();
        StageService stageService = new StageService();
        NullTopicReceiver nullTopicReceiver = new NullTopicReceiver();
        MqTopicSender mqTopicSender = new MqTopicSender();

    }
}
