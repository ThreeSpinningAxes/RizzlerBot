package APIStuff;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Dalle2Api {

    private final String API_KEY = "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";

    private final String BASE_URL = "https://api.openai.com/v1/images/generations";

    private final String DEFAULT_RESOLUTION = "512x512";

    public String queryAIArt(String prompt) {
        return "";
    }

    private String sendPOSTRequest(String prompt, int numberOfImages) throws URISyntaxException, IOException, InterruptedException {

        Dalle2ImageQuery query = new Dalle2ImageQuery();
        query.setPrompt(prompt);
        query.setN(numberOfImages);
        query.setSize(DEFAULT_RESOLUTION);

        Gson gson = new Gson();
        String jsonRequest = gson.toJson(query);

        HttpRequest requestImagePOST = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL))
                .header("authorization", "bearer " +API_KEY)
                .headers("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        System.out.println(jsonRequest);
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> httpResponse = client.send(requestImagePOST, HttpResponse.BodyHandlers.ofString());
        System.out.println(httpResponse.body());
        return "";
    }

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        Dalle2Api dalle2Api = new Dalle2Api();
        dalle2Api.sendPOSTRequest("cute cool dog", 1);
    }



}
