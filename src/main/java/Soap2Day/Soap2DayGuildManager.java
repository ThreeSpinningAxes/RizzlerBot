package Soap2Day;

import APIStuff.Watch2GetherAPI;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static MessageFormats.GeneralMessage.formattedMessage;
import static Soap2Day.Soap2DayWebScraper.*;

public class Soap2DayGuildManager {

    Guild guild;
    BrowserContext browserContext;
    Soap2DayWebScraper soap2DayWebScraper;
    private Map<Long, Page> userPages = new HashMap<>();

    public Soap2DayGuildManager(Guild guild, BrowserContext browserContext) {
        this.guild = guild;
        this.browserContext = browserContext;
        this.soap2DayWebScraper = Soap2DayWebScraper.createDefault(browserContext);
    }
    private Page getPageForUserWhoRequestedSearch(long memberID) {
        Page page = userPages.get(memberID);
        if (page == null) {
            Page membersPage = browserContext.newPage();
            userPages.put(memberID, membersPage);
            return membersPage;
        }
        return page;
    }

    public void getMovie(String query, User user, TextChannel textChannel, MessageReceivedEvent event,List<String> arguments) {
        boolean watch2GetherRequested = false;

        if (arguments.contains("-w")) {
            watch2GetherRequested = true;
        }

        Page userPageForNavigation = getPageForUserWhoRequestedSearch(user.getIdLong());
        userPageForNavigation = soap2DayWebScraper.loadSearchResultsPage(query, userPageForNavigation);
        List<List<String>> listOfMoviesFound = soap2DayWebScraper.getListOfMovies(userPageForNavigation);
        if (listOfMoviesFound.size() == 0) {
            textChannel.sendMessage(formattedMessage("No results found for '" + query + "'\n Try searching again.")).queue();
            userPageForNavigation.close();
            return;
        }
        if (listOfMoviesFound.size() == 1) {
            String movieName = listOfMoviesFound.get(0).get(0);
            String moviePageURL = listOfMoviesFound.get(0).get(1);
            String rawMovieURL = getShowLink(userPageForNavigation, moviePageURL);
            if (watch2GetherRequested) {
                try {
                    String roomLink = Watch2GetherAPI.sendPOSTRequest(rawMovieURL);
                    textChannel.sendMessage("Here is the watch2gether link to watch" + movieName + ": " + roomLink).queue();
                }
                catch (Exception e)
                {
                    textChannel.sendMessage("An error occurred fetching the watch2gether room. Here is the raw link for the movie instead: " + rawMovieURL).queue();
                }
            }
            else {
                textChannel.sendMessage("Found one result. Here is the url for '" + movieName + "': " + rawMovieURL).queue();
                userPageForNavigation.close();
                return;
            }
        }
        else {
            textChannel.sendMessage(formattedMessage(listMoviesToString(listOfMoviesFound))).queue();
            IfMultipleMovieResults eventListenerToAskUserWhichMovieIndex = new IfMultipleMovieResults(textChannel.getIdLong(), user.getIdLong(), listOfMoviesFound, userPageForNavigation, watch2GetherRequested);
            event.getJDA().addEventListener(eventListenerToAskUserWhichMovieIndex);
            removeEventListenerAfterTimeout(textChannel, event, 20000, eventListenerToAskUserWhichMovieIndex, userPageForNavigation);
        }
    }

    public void getTVShow(String query, User user, TextChannel textChannel, MessageReceivedEvent event, List<String> arguments) {
        boolean watch2GetherRequested = false;

        if (arguments.contains("-w")) {
            watch2GetherRequested = true;
        }

        Page userPageForNavigation = getPageForUserWhoRequestedSearch(user.getIdLong());
        userPageForNavigation = soap2DayWebScraper.loadSearchResultsPage(query, userPageForNavigation);
        List<List<String>> listOfTVs = soap2DayWebScraper.getListOfTVs(userPageForNavigation);
        if (listOfTVs.size() == 0) {
            textChannel.sendMessage(formattedMessage("No results found for '" + query + "'\n Try searching again.")).queue();
            userPageForNavigation.close();
            return;
        }
        if (listOfTVs.size() == 1) {
            String tvPageURL = listOfTVs.get(0).get(1);
            userPageForNavigation = soap2DayWebScraper.loadPage(tvPageURL, userPageForNavigation);
            List<List<List<String>>> listOfSeasonEpisodesOfTVShow = getListOfSeasonEpisodes(userPageForNavigation);

            textChannel.sendMessage(formattedMessage("Found one result: '" + listOfTVs.get(0).get(0) + "'\n\n"
                    + listOfSeasonsToString(listOfSeasonEpisodesOfTVShow) +
                    "\n\nPlease choose which season you want to watch.")).queue();

            IfMultipleTVResults eventListenerTVSearch = new IfMultipleTVResults(textChannel.getIdLong(),user.getIdLong(), listOfTVs, userPageForNavigation, watch2GetherRequested);
            eventListenerTVSearch.setListOfSeasonEpisodesOfChosenShow(listOfSeasonEpisodesOfTVShow);
            event.getJDA().addEventListener(eventListenerTVSearch);
            removeEventListenerAfterTimeout(textChannel, event, 60000, eventListenerTVSearch, userPageForNavigation);
            return;
        }

        textChannel.sendMessage(formattedMessage(listTVsToString(listOfTVs))).queue();
        IfMultipleTVResults eventListenerTVSearch = new IfMultipleTVResults(textChannel.getIdLong(), user.getIdLong(), listOfTVs, userPageForNavigation, watch2GetherRequested);
        event.getJDA().addEventListener(eventListenerTVSearch);
        removeEventListenerAfterTimeout(textChannel, event, 120000, eventListenerTVSearch, userPageForNavigation);
    }

    private void removeEventListenerAfterTimeout(TextChannel textChannel, MessageReceivedEvent event, int delay, ListenerAdapter eventHandler, Page page) {
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        try {
                            if (event.getJDA().getRegisteredListeners().contains(eventHandler)) {
                                event.getJDA().removeEventListener(eventHandler);
                                textChannel.sendMessage("Did not receive a response, timing out.").queue();
                            }
                        }
                        catch (IllegalArgumentException e) {
                            return;
                        }
                        page.close();
                    }
                },
                delay
        );
    }

}





