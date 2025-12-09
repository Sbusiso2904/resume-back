package wethinkcode.stage;


import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import javax.jms.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.HttpStatus;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.*;
import wethinkcode.loadshed.common.transfer.StageDO;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I test StageService message sending.
 */
//@Disabled
@Tag( "expensive" )
public class StageServiceMQTest
{
    public static final int TEST_PORT = 7772;

    private static StageService server;

    private static ActiveMQConnectionFactory factory;

    private static Connection mqConnection;


    @BeforeAll
    public static void startInfrastructure() throws JMSException {
        startMsgQueue();
        startStageSvc();
    }

    @AfterAll
    public static void cleanup() throws JMSException {
        server.stop();
        mqConnection.close();
    }

    @BeforeEach
    public void connectMqListener(MessageListener listener) throws JMSException {
        mqConnection = factory.createConnection();
        final Session session = mqConnection.createSession( false, Session.AUTO_ACKNOWLEDGE );
        final Destination dest = session.createTopic( StageService.MQ_TOPIC_NAME );
//
        final MessageConsumer receiver = session.createConsumer( dest );
        receiver.setMessageListener( listener );

        mqConnection.start();
    }

    @AfterEach
    public void closeMqConnection() throws JMSException {
        mqConnection.close();
        mqConnection = null;
    }

    @Test
    public void sendMqEventWhenStageChanges(){
        final SynchronousQueue<StageDO> resultCatcher = new SynchronousQueue<>();
        final MessageListener mqListener = new MessageListener(){
            @Override
            public void onMessage( Message message ){
//                throw new UnsupportedOperationException( "TODO" );
                try {
                    if (message instanceof TextMessage) {
                        String json = ((TextMessage) message).getText();
                        StageDO received = new ObjectMapper().readValue(json, StageDO.class);
                        resultCatcher.put(received);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        final HttpResponse<StageDO> startStage = Unirest.get( serverUrl() + "/stage" ).asObject( StageDO.class );
        assertEquals( HttpStatus.OK, startStage.getStatus() );

        final StageDO data = startStage.getBody();
        final int newStage = data.getStage() + 1;

        final HttpResponse<JsonNode> changeStage = Unirest.post( serverUrl() + "/stage" )
            .header( "Content-Type", "application/json" )
            .body( new StageDO( newStage ))
            .asJson();
        assertEquals( HttpStatus.OK, changeStage.getStatus() );

//        fail( "TODO" );
        try {
            StageDO result = resultCatcher.poll(2, TimeUnit.SECONDS);

            System.out.println("this is the result:" + result);
            assertNotNull(result, "No MQ message was received within timeout");
            assertEquals(newStage, result.getStage());
        } catch (InterruptedException e) {
            fail("Interrupted while waiting for MQ event");
        }

        server.stop();
    }

    private static void startMsgQueue() throws JMSException {
        factory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
    }

    private static void startStageSvc(){
        server = new StageService().initialise();
        server.start( TEST_PORT );
    }

    private String serverUrl(){
        return "http://localhost:" + TEST_PORT;
    }
}
