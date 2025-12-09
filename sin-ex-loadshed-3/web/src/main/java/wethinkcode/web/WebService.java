package wethinkcode.web;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import wethinkcode.loadshed.spikes.TopicReceiver;
import wethinkcode.places.PlaceNameService;
import wethinkcode.schedule.ScheduleService;
import wethinkcode.stage.StageService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;


/**
 * I am the front-end web server for the LightSched project.
 * <p>
 * Remember that we're not terribly interested in the web front-end part of this server, more in the way it communicates
 * and interacts with the back-end services.
 */
public class WebService
{
    public static final int DEFAULT_PORT = 8080;
    public static final String STAGE_SVC_URL = "http://localhost:" + StageService.DEFAULT_PORT;
    public static final String PLACES_SVC_URL = "http://localhost:" + PlaceNameService.DEFAULT_PORT;
    public static final String SCHEDULE_SVC_URL = "http://localhost:" + ScheduleService.DEFAULT_PORT;
    private static final String PAGES_DIR = "/html/";
    private static final String TEMPLATES_DIR = "/templates/";
    private HttpClient httpClient;
    private TopicReceiver topicReceiver = new TopicReceiver();
    private static String province;
    public String loadSheddingStage;


    public static void main( String[] args ){

        final WebService svc = new WebService().initialise();
        svc.start();
    }

    private Javalin server;

    private int servicePort;

    @VisibleForTesting
    WebService initialise(){
        // FIXME: Initialise HTTP client, MQ machinery and server from here
        new Thread(topicReceiver::run).start();
        configureHttpClient();
        server = configureHttpServer();

        return this;
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

    private void configureHttpClient(){
//        throw new UnsupportedOperationException( "TODO" );
//        To makes requests to services ...
        try{

            httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Javalin configureHttpServer(){
//        throw new UnsupportedOperationException( "TODO" );
//        to server the pages...
        JavalinThymeleaf.configure(templateEngine());
        loadSheddingStage = getStage();
        System.out.println("this is the load shedding at startup :" + loadSheddingStage);
//      refactor to use SynchronousQueue<>(); later
        return Javalin.create(config -> {
            config.addStaticFiles(PAGES_DIR, Location.CLASSPATH);})
                .get("/", context -> {
                    System.out.println("Stage at on landing : "+ loadSheddingStage);
                    Map<String, Object> viewModel = new HashMap<>(Map.of("stage", getStage()));
                    viewModel.put("provinces", getProvinces());
                    viewModel.put("stage", getStage());
                    viewModel.put("province", province);
                    viewModel.put("queryTowns", getTowns(province).get("queryNames"));
                    viewModel.put("displayTowns", getTowns(province).get("displayNames"));

                    context.render("index.html", viewModel);})
                .get("/towns", context -> {

                    province = context.queryParam("province");

                    Map<String, Object> viewModel = new HashMap<>(Map.of("towns", getTowns(province)));
                    viewModel.put("stage", topicReceiver.UPDATE_STAGE);
                    viewModel.put("province", province);
                    viewModel.put("provinces", getProvinces());

                    context.redirect("/");})
                .get("/schedule", context -> {

                    String town = context.queryParam("town").replace("%20", " ");

//                    System.out.println("testing again: "+ loadSheddingStage);
                    Map<String, Object> viewModel = new HashMap<>(Map.of("schedule", getSchedule(province, town)));
                    viewModel.put("province", province);
                    viewModel.put("provinces", getProvinces());
                    viewModel.put("town", town);
                    viewModel.put("stage", topicReceiver.UPDATE_STAGE);
                    viewModel.put("queryTowns", getTowns(province).get("queryNames"));
                    viewModel.put("displayTowns", getTowns(province).get("displayNames"));
                    viewModel.put("schedule", getSchedule(province, town));

                    context.render("schedule.html", viewModel);});
    }

    private String[] getProvinces(){
        String s = String.valueOf('"');
        try{
            HttpRequest placesRequest = HttpRequest.newBuilder()
                    .uri(URI.create(PLACES_SVC_URL.concat("/provinces"))).GET().build();
            HttpResponse<String> placesResponse = httpClient.send(placesRequest, HttpResponse.BodyHandlers.ofString());
            return placesResponse.body().replace("[","").replace("]","").replace(s, "").split(",");
        } catch (Exception e) {
            return new String[0];
        }
    }


    private String[] getSchedule(String province, String town){
        try{
            HttpRequest scheduleRequest = HttpRequest.newBuilder()
                    .uri(URI.create(SCHEDULE_SVC_URL.concat(String.format("/%s/%s", province.replace(" ", "%20"), town.replace(" ", "%20"))))).GET().build();
            HttpResponse<String> scheduleResponse = httpClient.send(scheduleRequest, HttpResponse.BodyHandlers.ofString());

            return scheduleResponse.body().replace("[","").replace("]","").split(",");
        } catch (Exception e) {
            return new String[0];
        }
    }


    private Map<String, List> getTowns(String province){
        String s = String.valueOf('"');
        try{
            HttpRequest townsRequest = HttpRequest.newBuilder()
                    .uri(URI.create(PLACES_SVC_URL.concat("/towns/".concat(province.replace(" ", "%20"))))).GET().build();
            HttpResponse<String> townsResponse = httpClient.send(townsRequest, HttpResponse.BodyHandlers.ofString());

            String[] towns = townsResponse.body().replace("[","").replace("]","")
                    .replace(s, "").replace("{","").replace("}","").split(",");
            ArrayList<String> townNames = new ArrayList<>();

            for (String town : towns){
                if (town.contains("name")){
                    String name = town.split(":")[1];
                    townNames.add(name);
                }
            }
            ArrayList<String> newTowns = new ArrayList<>();
            for (String town : townNames) {
                newTowns.add(town.replace(" ", "%20"));
            }
            return Map.of("queryNames", newTowns, "displayNames", townNames);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String getStage(){
        try{
            HttpRequest stageRequest = HttpRequest.newBuilder()
                    .uri(URI.create(STAGE_SVC_URL.concat("/stage"))).GET().build();
            HttpResponse<String> stageResponse = httpClient.send(stageRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("stage respode body: "+ stageResponse.body().replace("}","").split(":")[1]);
            return stageResponse.body().replace("}","").split(":")[1];
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "unknown";
        }
    }

//    public void routes(EndpointGroup group) {
//        server.routes(group);
//    }

    private TemplateEngine templateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix(TEMPLATES_DIR);
        templateEngine.setTemplateResolver(resolver);
        templateEngine.addDialect(new LayoutDialect());
        return templateEngine;
    }

}
