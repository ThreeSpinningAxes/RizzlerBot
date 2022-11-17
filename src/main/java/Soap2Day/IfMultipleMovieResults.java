package Soap2Day;

import APIStuff.Watch2GetherAPI;
import com.microsoft.playwright.Page;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

import static BackendAudioHandling.CommandHelpers.isInValidRange;
import static BackendAudioHandling.CommandHelpers.isInteger;
import static Soap2Day.Soap2DayWebScraper.getShowLink;

public class IfMultipleMovieResults extends ListenerAdapter {
        private final long channelId, authorId; // id because keeping the entity would risk cache to become outdated
        private List<List<String>> listOfMovies;

        private Page userPageForNavigation;

        boolean ifWatch2GetherRequested = false;

        public IfMultipleMovieResults(long channelID, long memberID, List<List<String>> listOfMovies, Page userPageForNavigation, Boolean ifWatch2GetherRequested) {
            this.channelId = channelID;
            this.authorId = memberID;
            this.listOfMovies = listOfMovies;
            this.userPageForNavigation = userPageForNavigation;
            this.ifWatch2GetherRequested = ifWatch2GetherRequested;
        }

        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getAuthor().isBot()) {
                return;
            }
            if (event.getChannel().asTextChannel().getIdLong() != channelId) {
                return; // ignore other channels
            }
            if (event.getAuthor().getIdLong() != authorId) {
                return; //only respond to person who initiated the call
            }

            TextChannel textChannel= event.getChannel().asTextChannel();
            String message = event.getMessage().getContentRaw();

            if (!isInteger(message)) {
                textChannel.sendMessage("Please provide a integer value to specify which movie you want to choose.").queue();
                return;
            }

            int movieIndex = Integer.parseInt(message);

            if (!isInValidRange(movieIndex, listOfMovies.size())) {
                textChannel.sendMessage("Please provide number that is within the range of total found movies.").queue();
                return;
            }
            event.getJDA().removeEventListener(this);
            textChannel.sendMessage("loading movie url...").queue();
            String movieName = listOfMovies.get(movieIndex-1).get(0);
            String moviePageURL = listOfMovies.get(movieIndex-1).get(1);
            String rawMovieURL = getShowLink(this.userPageForNavigation, moviePageURL);
            userPageForNavigation.close();

            if (ifWatch2GetherRequested) {
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
                textChannel.sendMessage("Here is the link to " + movieName + ": " + rawMovieURL).queue();
            }
        }

    }

