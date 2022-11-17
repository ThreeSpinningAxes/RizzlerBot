package APIStuff;

import com.google.gson.Gson;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Watch2GetherAPI {
    static PropertiesConfiguration properties;

    static {
        try {
            properties = new PropertiesConfiguration("INFO.properties");
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public Watch2GetherAPI() throws ConfigurationException {
    }

    private static final String API_KEY = properties.getString("watch2GetherKey");

    private final static String REQUEST_URL = "https://api.w2g.tv/rooms/create.json";

    private final static String BASE_URL = "https://w2g.tv/rooms/";

    public static String sendPOSTRequest(String videoURL) throws URISyntaxException, IOException, InterruptedException {

        Watch2GetherPOSTBuilderHelper watch2GetherPOSTBuilderHelper = new Watch2GetherPOSTBuilderHelper();
        watch2GetherPOSTBuilderHelper.setW2g_api_key(API_KEY);
        watch2GetherPOSTBuilderHelper.setShare(videoURL);
        watch2GetherPOSTBuilderHelper.setBg_color("000000");
        watch2GetherPOSTBuilderHelper.setBg_opacity("50");



        Gson gson = new Gson();
        String jsonRequest = gson.toJson(watch2GetherPOSTBuilderHelper);

        HttpRequest requestRoomPOST = HttpRequest.newBuilder()
                .uri(new URI(REQUEST_URL))
                .header("Accept", "application/json")
                .headers("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> httpResponse = client.send(requestRoomPOST, HttpResponse.BodyHandlers.ofString());
        System.out.println(httpResponse.body());
        String roomURL = httpResponse.body().split(",", 2)[1].split(":")[1].split(",")[0].replace("\"", "");
        return BASE_URL + roomURL;
    }
    public static void main(String[] args) throws ConfigurationException, URISyntaxException, IOException, InterruptedException {
        Watch2GetherAPI a = new Watch2GetherAPI();
        a.sendPOSTRequest("https://www.youtube.com/watch?v=sV2t3tW_JTQ&list=RDxMgE736TC4A&index=10");
    }
}
