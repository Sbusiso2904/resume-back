package wethinkcode.schedule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import wethinkcode.loadshed.common.mq.MqTopicSender;
import wethinkcode.loadshed.common.mq.test.NullTopicReceiver;
import wethinkcode.loadshed.common.mq.test.NullTopicSender;
import wethinkcode.loadshed.common.transfer.DayDO;
import wethinkcode.loadshed.common.transfer.ScheduleDO;
import wethinkcode.loadshed.common.transfer.SlotDO;
import wethinkcode.loadshed.spikes.TopicReceiver;

/**
 * I provide a REST API providing the current loadshedding schedule for a given town (in a specific province) at a given
 * loadshedding stage.
 */
public class ScheduleService
{
    public static final int DEFAULT_STAGE = 0; // no loadshedding. Ha!

    public int stage;

    public static final int DEFAULT_PORT = 7002;


    private Javalin server;

    private int servicePort;

    private TopicReceiver topicReceiver = new TopicReceiver();

    private NullTopicReceiver nullTopicReceiver;



    public static void main( String[] args ){
        final ScheduleService svc = new ScheduleService().initialise();
        svc.start();
    }

    @VisibleForTesting
    ScheduleService initialise(){
        server = initHttpServer();
        new Thread(topicReceiver::run).start();
        return this;
    }

    @VisibleForTesting
    ScheduleService initialise( int stage, NullTopicReceiver nullTopicReceiver){
        this.nullTopicReceiver = nullTopicReceiver;
        this.stage = stage;
        server = initHttpServer();
        return  this;
    }

    public void start(){
        start( DEFAULT_PORT );
    }

    @VisibleForTesting
    void start( int networkPort ){
        servicePort = networkPort;
        run();
    }

    public void stop(){
        server.stop();
    }

    public void run(){
        server.start( servicePort );
    }

    private Javalin initHttpServer(){
        return Javalin.create()
            .get( "/{province}/{town}/{stage}", this::getSchedule )
            .get( "/{province}/{town}", this::getSchedule );
    }

    private Context getSchedule( Context ctx ){
        final String province = ctx.pathParam( "province" );
        final String townName = ctx.pathParam( "town" );
        String stageStr;
        try{
            stageStr = ctx.pathParam( "stage" );
        } catch (Exception e) {
            stageStr = String.valueOf(topicReceiver.UPDATE_STAGE);
            System.out.println("what is the stage: "+ stageStr);
        }

        if( province.isEmpty() || townName.isEmpty() || stageStr.isEmpty() ){
            ctx.status( HttpStatus.BAD_REQUEST );
            return ctx;
        }

        final int stage = Integer.parseInt( stageStr );
        if( stage < 0 || stage > 8 ){
            return ctx.status( HttpStatus.BAD_REQUEST );
        }



        final Optional<ScheduleDO> schedule = getSchedule( province, townName, Integer.parseInt(stageStr));

        ctx.status( schedule.isPresent()
            ? HttpStatus.OK
            : HttpStatus.NOT_FOUND );
        return ctx.json( schedule.orElseGet( ScheduleService::emptySchedule ) );
    }

    private Context getDefaultSchedule( Context ctx ){
//        throw new UnsupportedOperationException( "TODO" );
        final String province = ctx.pathParam( "province" );
        final String townName = ctx.pathParam( "town" );

        if( province.isEmpty() || townName.isEmpty()){
            ctx.status( HttpStatus.BAD_REQUEST );
            return ctx;
        }


        final Optional<ScheduleDO> schedule = getSchedule( province, townName, DEFAULT_STAGE);

        ctx.status( schedule.isPresent()
                ? HttpStatus.OK
                : HttpStatus.NOT_FOUND );
        return ctx.json( schedule.orElseGet( ScheduleService::emptySchedule ) );

    }

    // There *must* be a better way than this...
    Optional<ScheduleDO> getSchedule( String province, String town, int stage ){
        return province.equalsIgnoreCase( "Mars" )
            ? Optional.empty()
            : Optional.of( mockSchedule() );
    }

    private static ScheduleDO mockSchedule(){
        final List<SlotDO> slots = List.of(
            new SlotDO( LocalTime.of( 2, 0 ), LocalTime.of( 4, 0 ) ),
            new SlotDO( LocalTime.of( 10, 0 ), LocalTime.of( 12, 0 ) ),
            new SlotDO( LocalTime.of( 18, 0 ), LocalTime.of( 20, 0 ) )
        );
        final List<DayDO> days = List.of(
            new DayDO( slots ),
            new DayDO( slots ),
            new DayDO( slots ),
            new DayDO( slots )
        );
        return new ScheduleDO( days );
    }

    private static ScheduleDO emptySchedule(){
        final List<SlotDO> slots = Collections.emptyList();
        final List<DayDO> days = Collections.emptyList();
        return new ScheduleDO( days );
    }
}
