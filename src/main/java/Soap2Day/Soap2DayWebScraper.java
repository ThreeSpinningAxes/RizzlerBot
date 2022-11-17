package Soap2Day;


import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Soap2DayWebScraper {
    BrowserContext browserContext;

    private static final String BASE_URL = "https://soap2day.to";
    private final static String BASE_SEARCH_URL = "https://soap2day.to/search/keyword/";
    private final static String URL_TO_GET_COOKIES = "https://soap2day.to/enter.html";

    private Soap2DayWebScraper(BrowserContext browserContext) {
        this.browserContext = browserContext;
    }

    public static Soap2DayWebScraper createDefault(BrowserContext browserContext) {
        Soap2DayWebScraper soap2DayWebScraper = new Soap2DayWebScraper(browserContext);
        if (!soap2DayWebScraper.setCookies()) {
            return null;
        }
        return soap2DayWebScraper;
    }

    public void closeBrowserContext() {
        this.browserContext.close();
    }

    private boolean setCookies() {
        Page page = browserContext.newPage();
        Response pageResponse = page.navigate(URL_TO_GET_COOKIES, new Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD));
        if (pageResponse.ok()) {
            Response completeCaptcha = page.waitForNavigation(new Page.WaitForNavigationOptions().setTimeout(15000), () -> {
                page.click(".btn-success:not([disabled])");
                this.browserContext.addCookies(page.context().cookies());
            });
            page.close();
            if (completeCaptcha.ok()) { return true; }
            else { return false; }

        }
        else { return false; }
    }

    public static String listMoviesToString(List<List<String>> listOfMovies) {
        StringBuilder movieList = new StringBuilder("LIST OF MOVIES:\n\n");
        if (listOfMovies.size() > 20) {
            movieList.append(listOfMovies.size() + " total related movies were found. Only showing first 20\n");
        }
        movieList.append(listMedia(listOfMovies));
        movieList.append("\n Please select which movie you want from the list or perform a research");
        return movieList.toString();
    }

    public static String getShowLink(Page page, String showURL) {
        try {
            Response response = page.navigate(showURL, new Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD)
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(15000));
            if (!response.ok()) {
                return null;
            }
            return page.locator("#player > div.jw-wrapper.jw-reset > div.jw-media.jw-reset > video").getAttribute("src");
        }
        catch (Exception e) {
            return null;
        }
    }

    private String getTVLink(int TVIndex, List<List<String>> listOfTVs) {
        return listOfTVs.get(TVIndex).get(1);
    }

    public static String listTVsToString(List<List<String>> listOfTVs) {
        StringBuilder TVList = new StringBuilder("LIST OF TVs:\n\n");
        if (listOfTVs.size() > 20) {
            TVList.append(listOfTVs.size() + " total related TV shows were found. Only showing first 20\n");
        }
        TVList.append(listMedia(listOfTVs));
        TVList.append("\n Please select which movie you want from the list or perform a research");
        return TVList.toString();
    }

    private static String listMedia(List<List<String>> listOfMedia) {
        int numOfShows = listOfMedia.size();
        StringBuilder showList = new StringBuilder();
        for (int i = 0; i < numOfShows; i++) {
            if (i < 20) {
                showList.append("(" + (i + 1) + ") " + listOfMedia.get(i).get(0) + "\n");
            }
        }
        return showList.toString();
    }

    public Page loadSearchResultsPage(String mediaName) {
        return loadSearchResultsPage(mediaName, null);
    }
    public Page loadSearchResultsPage(String mediaName, Page page) {
        if (page == null) {
            page = this.getNewPage();
        }
        String searchUrl = BASE_SEARCH_URL + URLEncoder.encode(mediaName, StandardCharsets.UTF_8).replace("+", "%20");
        Response pageResponse = page.navigate(searchUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD).setTimeout(15000));
        if (pageResponse.ok()) {
            return page;
        } else {
            return null;
        }
    }


    public static Page loadPage(String url, Page page) {
        Response pageResponse = page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD).setTimeout(15000));
        if (pageResponse.ok()) {
            return page;
        } else {
            return null;
        }
    }

    public static List<List<List<String>>> getListOfSeasonEpisodes(Page page) {
        Locator seasons = page.locator("body > div > div:nth-child(3) > div > div.col-sm-8 > div:nth-child(1) > div > div > div > div.col-sm-12.col-lg-12 > div");
        int seasonCount = seasons.count();
        List<List<List<String>>> listOfSeasonEpisodes = new ArrayList<>();
        for (int i = 0; i < seasonCount; i++) {
            Locator episodes = seasons.nth(i).locator("div > div");
            int episodeCount = episodes.count();
            List<List<String>> seasonsEpisodes = new ArrayList<>();
            for (int j = 0; j < episodeCount; j++) {
                List<String> nameURL = new ArrayList<>();
                String episodeName = episodes.nth(j).locator("a").textContent();
                episodeName = episodeName.substring(episodeName.indexOf(".") + 1);
                String episodeURL = episodes.nth(j).locator("a").getAttribute("href");

                nameURL.add(episodeName);
                nameURL.add(BASE_URL + episodeURL);
                seasonsEpisodes.add(0, nameURL);
            }
            listOfSeasonEpisodes.add(0,seasonsEpisodes);
        }
        return listOfSeasonEpisodes;
    }

    public static String listOfSeasonsToString(List<List<List<String>>> listOfSeasonEpisodes) {
        final String spacing = "                                   ";
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < listOfSeasonEpisodes.size(); i++) {

            String seasonString = "Season (" + (i + 1) + ")";
            seasonString += spacing.substring(0, spacing.length() - seasonString.length());
            if (i % 3 == 0 && i != 0) {
                string.append('\n');
            }
            string.append(seasonString);
        }
        return string.toString();
    }

    public static List<String> listTVSeasonEpisodeToString(List<List<List<String>>> listOfSeasonsEpisodes) {
        final String spacing = "                                   ";
        List<String> listOfSeasonsAndTheirEpisodesToString = new ArrayList<>();
        for (int i = 0; i < listOfSeasonsEpisodes.size(); i++) {
            StringBuilder seasonEpisodeList = new StringBuilder("Season " + (i + 1) + ":\n");
            List<List<String>> episodes = listOfSeasonsEpisodes.get(i);
            for (int j = 0; j < episodes.size(); j++) {
                String episodeString = "(" + (j + 1) + ") " + episodes.get(j).get(0);
                try {
                    episodeString += spacing.substring(0, spacing.length() - episodeString.length());
                }
                catch (IndexOutOfBoundsException e) {
                    seasonEpisodeList.append('\n');
                }
                seasonEpisodeList.append(episodeString);
                if (j % 3 == 0 && i != 0) {
                    seasonEpisodeList.append('\n');
                }
            }
            listOfSeasonsAndTheirEpisodesToString.add(seasonEpisodeList.toString());
        }
        return listOfSeasonsAndTheirEpisodesToString;
    }

    public static String listOfEpisodesToString(List<List<String>> episodes, String season) {
        StringBuilder listOfEpisodesStringBuilder = new StringBuilder("Season " + season + ":\n");
        for (int j = 0; j < episodes.size(); j++) {
            String episodeString = "(" + (j + 1) + ") " + episodes.get(j).get(0) + '\n';
            listOfEpisodesStringBuilder.append(episodeString);
        }
        return listOfEpisodesStringBuilder.toString();
    }

    private Page getNewPage() {
        return browserContext.newPage();
    }

    /*
     * Gets list of movies and its url
     */
    public List<List<String>> getListOfMovies(Page page) {
        Locator moviesLocator = page.locator("body > div > div:nth-child(3) > div > div.col-sm-8.col-lg-8.col-xs-12 > div:nth-child(1) > div.panel-body > div > div > div > div > div:nth-child(1) > div");
        int numberOfMovies = moviesLocator.count();
        List<List<String>> listOfMovies = new ArrayList<>();
        for (int i = 0; i < numberOfMovies; i++) {
            Locator locator = moviesLocator.nth(i).locator("h5 > a");
            List<String> movieNameURL = new ArrayList<>();
            movieNameURL.add(locator.textContent());
            movieNameURL.add(BASE_URL + locator.getAttribute("href"));
            listOfMovies.add(movieNameURL);
        }
        return listOfMovies;
    }

    public List<List<String>> getListOfTVs(Page page) {
        Locator TVsLocator = page.locator("body > div > div:nth-child(3) > div > div.col-sm-8.col-lg-8.col-xs-12 > div:nth-child(2) > div.panel-body > div > div > div > div > div:nth-child(1) > div");
        int numberOfTVs = TVsLocator.count();
        List<List<String>> listOfTVs = new ArrayList<>();
        for (int i = 0; i < numberOfTVs; i++) {
            List<String> tvNameURL = new ArrayList<>();
            Locator locator = TVsLocator.nth(i).locator("h5 > a").first();
            tvNameURL.add(locator.textContent());
            tvNameURL.add(BASE_URL + locator.getAttribute("href"));
            listOfTVs.add(tvNameURL);
        }
        return listOfTVs;
    }

    public static void main(String[] args) {

        Playwright playwright = Playwright.create();
        Browser browser = playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(true).setSlowMo(150).setTimeout(1500));
        BrowserContext browserContext = browser.newContext();
        browserContext.setDefaultTimeout(10000);


        Soap2DayWebScraper api = Soap2DayWebScraper.createDefault(browserContext);
        Page searchResults = api.loadSearchResultsPage("toy story");
         /*
        List<List<String>> listOfTVs = api.getListOfTVs(searchResults);
        System.out.println(listTVsToString(listOfTVs));
        Page seasonEpisodesPage = api.loadTVSeriesPage(listOfTVs.get(19).get(1), searchResults);
        List<List<List<String>>> a = api.getListOfSeasonEpisodes(seasonEpisodesPage);
        System.out.println(listTVSeasonEpisodeToString(a));
        api.closeBrowserContext();
        playwright.close();
        */

        List<List<String>> listOfMovies = api.getListOfMovies(searchResults);
        System.out.println(listMoviesToString(listOfMovies));
        System.out.println(getShowLink(searchResults, listOfMovies.get(1).get(1)));
        playwright.close();

    }

}
