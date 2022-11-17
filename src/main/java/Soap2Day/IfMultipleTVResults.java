package Soap2Day;

import APIStuff.Watch2GetherAPI;
import com.microsoft.playwright.Page;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

import static BackendAudioHandling.CommandHelpers.isInValidRange;
import static BackendAudioHandling.CommandHelpers.isInteger;
import static MessageFormats.GeneralMessage.formattedMessage;
import static Soap2Day.Soap2DayWebScraper.*;

public class IfMultipleTVResults extends ListenerAdapter {
    private final long channelId, authorId; // id because keeping the entity would risk cache to become outdated

    private Page userPageForNavitation;

    private List<List<String>> listOfTVs;
    private List<List<List<String>>> listOfSeasonEpisodesOfChosenShow = null;

    private List<List<String>> chosenSeason = null;

    boolean ifWatch2GetherRequested = false;


    public IfMultipleTVResults(long channelId, long memberId, List<List<String>> listOfTVs, Page userPageForNavitation, boolean ifWatch2GetherRequested) {
        this.channelId = channelId;
        this.authorId = memberId;
        this.listOfTVs = listOfTVs;
        this.userPageForNavitation = userPageForNavitation;
        this.ifWatch2GetherRequested = ifWatch2GetherRequested;
    }

    public void setListOfSeasonEpisodesOfChosenShow(List<List<List<String>>> listOfSeasonEpisodesOfChosenShow) {
        this.listOfSeasonEpisodesOfChosenShow = listOfSeasonEpisodesOfChosenShow;
    }

    public void setChosenSeason(List<List<String>> chosenSeason) {
         this.chosenSeason = chosenSeason;
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (!isEventValidCheck(event)) {
            return;
        }

        TextChannel textChannel= event.getChannel().asTextChannel();
        String message = event.getMessage().getContentRaw();

        //if null, then we are asking user which show they want to choose
        if (listOfSeasonEpisodesOfChosenShow == null) {
            chooseTV(message, textChannel, userPageForNavitation);
            return;
        }
        else if (chosenSeason == null) {
            chooseSeason(message, textChannel);
            return;
        }
        else {
            chooseEpisode(message, textChannel, userPageForNavitation, event);
            event.getJDA().removeEventListener(this);
        }
    }

    private void chooseTV(String message, MessageChannel channel, Page page) {
        if (!isInteger(message)) {
            channel.sendMessage("Please provide a integer value to specify which show you want to choose.").queue();
            return;
        }
        int tvIndex = Integer.parseInt(message);
        if (!isInValidRange(tvIndex, listOfTVs.size())) {
            channel.sendMessage("Please provide number that is within the range of total found TV's.").queue();
            return;
        }

        String tvSeasonEpisodePageURL = listOfTVs.get(tvIndex-1).get(1);
        userPageForNavitation = loadPage(tvSeasonEpisodePageURL, userPageForNavitation);
        this.listOfSeasonEpisodesOfChosenShow = getListOfSeasonEpisodes(page);

        channel.sendMessage(listOfSeasonsToString(listOfSeasonEpisodesOfChosenShow)).queue();
        this.listOfSeasonEpisodesOfChosenShow = getListOfSeasonEpisodes(userPageForNavitation);
        channel.sendMessage("Please enter which season you want to watch.").queue();
    }

    private void chooseSeason(String message, MessageChannel channel) {
        if (!isInteger(message)) {
            channel.sendMessage("Please provide a integer value to specify which season you want to choose.").queue();
            return;
        }
        int seasonIndex = Integer.parseInt(message);
        if (!isInValidRange(seasonIndex,this.listOfSeasonEpisodesOfChosenShow.size())) {
            channel.sendMessage(formattedMessage("Please provide number that is within the range of the total seasons for this show")).queue();
            return;
        }
        this.chosenSeason = listOfSeasonEpisodesOfChosenShow.get(seasonIndex-1);
        channel.sendMessage(formattedMessage(listOfEpisodesToString(chosenSeason, String.valueOf(seasonIndex))
        + "\n\nPlease enter which episode you would like to watch.")).queue();

    }

    private void chooseEpisode(String message, MessageChannel channel, Page page, MessageReceivedEvent event) {
        if (!isInteger(message)) {
            channel.sendMessage("Please provide a integer value to specify which episode you want to choose.").queue();
            return;
        }
        int episodeIndex = Integer.parseInt(message);
        if (!isInValidRange(episodeIndex, this.chosenSeason.size())) {
            channel.sendMessage("Please provide number that is within the range of the total episodes for this season.").queue();
            return;
        }

        String episodeName = chosenSeason.get(episodeIndex-1).get(0);
        String episodePageURL = chosenSeason.get(episodeIndex-1).get(1);
        String episodeURL = getShowLink(userPageForNavitation, episodePageURL);

        if (ifWatch2GetherRequested) {
            try {
                String roomLink = Watch2GetherAPI.sendPOSTRequest(episodeURL);
                channel.sendMessage("Here is the watch2gether link to watch '" + episodeName + "': " + roomLink).queue();
            }
            catch (Exception e)
            {
                channel.sendMessage("An error occurred fetching the watch2gether room. Here is the raw link for the episode instead: " + episodeURL).queue();
            }
        }
        else {
            channel.sendMessage("Here is the link to '" + episodeName+ "': " + episodeURL).queue();
        }
        event.getJDA().removeEventListener(this);
    }


    private boolean isEventValidCheck(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return false;
        }
        if (event.getChannel().getIdLong() != channelId) {
            return false; // ignore other channels
        }
        if (event.getAuthor().getIdLong() != authorId) {
            return false; //only respond to person who initiated the call
        }
        return true;
    }


}
