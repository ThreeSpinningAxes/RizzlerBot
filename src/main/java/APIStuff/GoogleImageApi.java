package APIStuff;


import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GoogleImageApi {

    private static String key = "AIzaSyBDbAJfber6IXPqQeqgXVF6KL0a7ZCWsCQ";

    private static String cx = "967860b3a8a2a4254";

    public static String getImageURL(String query) {
        String responseBody = ResponseFromGETRequestOfImageSearch(query);
        if (responseBody == null) {
            return null;
        }
        String imageLink = getRandomImageLinkFromQueryResponse(responseBody);
        if (imageLink == null) {
            return null;
        }
        return imageLink;
    }

    private static String ResponseFromGETRequestOfImageSearch(String query) {
        try {
            URI request = getURIFromString(buildRequestURL(query));

            HttpRequest imageSearchRequest = HttpRequest.newBuilder()
                    .uri(request)
                    .GET()
                    .build();
            HttpResponse<String> queryResponse = HttpClient.newHttpClient().send(imageSearchRequest, HttpResponse.BodyHandlers.ofString());
            return queryResponse.body();
        } catch (Exception e) {
            return null;
        }
    }
    private static String getRandomImageLinkFromQueryResponse(String responseBody) {
        JSONObject JSONResponse = new JSONObject(responseBody);
        JSONArray images = JSONResponse.getJSONArray("items");

        List<String> listOfImageLinks = new ArrayList<>();

        for (int i = 0; i < images.length(); i++) {
            String link = images.getJSONObject(i).getString("link");
            listOfImageLinks.add(link);
        }

        if (listOfImageLinks.size() == 0) {
            return null;
        }

        Random random = new Random();
        return listOfImageLinks.get(random.nextInt(0, listOfImageLinks.size()));
    }

    private static String buildRequestURL(String query) {
        return String.format("https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s&searchType=image", key, cx, URLEncoder.encode(query, StandardCharsets.UTF_8));
    }
    private static URI getURIFromString(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            return null;
        }
    }

}
