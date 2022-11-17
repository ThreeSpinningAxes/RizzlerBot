import APIStuff.GoogleImageApi;
import Soap2Day.Soap2DayManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static BackendAudioHandling.CommandHelpers.isInteger;
import static MessageFormats.GeneralMessage.formattedMessage;

public class GeneralCommands extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            return;
        }

        Guild guild = event.getGuild();
        TextChannel textChannel = event.getChannel().asTextChannel();
        String chatCommand = event.getMessage().getContentRaw();

        if (chatCommand.startsWith("<image")) {
            getImage(textChannel, chatCommand);
        } else if (chatCommand.startsWith("<watchmovie")) {
            searchMovie(textChannel, chatCommand, guild, event);
        } else if (chatCommand.startsWith("<watchshow")) {
            searchShow(textChannel, chatCommand, guild, event);
        } else if (chatCommand.equals("<clearchat") || chatCommand.startsWith("<clearchat ")) {
            deleteChatMessagesCommand(guild, textChannel, chatCommand, event);
        }

    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        Soap2DayManager.getInstance().cleanUp();
    }

    private void getImage(TextChannel textChannel, String chatCommand) {
        String[] splitCommand = chatCommand.split(" ", 2);
        if (splitCommand.length <= 1) {
            textChannel.sendMessage(formattedMessage("Please provide image search text" +
                    "\nCorrect usage:<image [description of image you want]")).queue();
            return;
        }
        String searchContext = splitCommand[1];
        String imageLink = GoogleImageApi.getImageURL(searchContext);
        if (imageLink == null) {
            textChannel.sendMessage(formattedMessage("Could not find a valid image from the first 10 images in the search")).queue();
            return;
        }
        textChannel.sendMessage(imageLink).queue();
        textChannel.sendMessage(formattedMessage("Here is a image of " + searchContext)).queue();
    }

    private void searchMovie(TextChannel textChannel, String chatCommand, Guild guild, MessageReceivedEvent event) {
        String[] splitCommand = chatCommand.split(" ", 2);
        if (splitCommand.length <= 1) {
            textChannel.sendMessage(formattedMessage("Please provide what movie you want to search" +
                    "\nCorrect usage:<watchmovie [options] [name of movie]")).queue();
            return;
        }
        String command = splitCommand[1];
        String movie;
        List<String> commandOptions = getCommandArgs(command);
        if (commandOptions.size() != 0) {
            movie = getSearchQuery(command);
        }
        else {
            movie = command;
        }

        textChannel.sendMessage(formattedMessage("Performing search...")).queue();
        Soap2DayManager.getInstance()
                .getSoap2DayGuildManager(guild)
                .getMovie(movie, event.getAuthor(), textChannel, event, commandOptions);
    }

    private void searchShow(TextChannel textChannel, String chatCommand, Guild guild, MessageReceivedEvent event) {
        String[] splitCommand = chatCommand.split(" ", 2);
        if (splitCommand.length <= 1) {
            textChannel.sendMessage(formattedMessage("Please provide what show you want to search" +
                    "\nCorrect usage:<watchshow [options] [name of show]")).queue();
            return;
        }
        String command = splitCommand[1];
        String show;
        List<String> commandOptions = getCommandArgs(command);
        if (commandOptions.size() != 0) {
            show = getSearchQuery(command);
        }
        else {
            show = command;
        }

        textChannel.sendMessage(formattedMessage("Performing search...")).queue();
        Soap2DayManager.getInstance()
                .getSoap2DayGuildManager(guild)
                .getTVShow(show, event.getAuthor(), textChannel, event, commandOptions);
    }

    public void deleteChatMessagesCommand(Guild guild, TextChannel textChannel, String chatCommand, MessageReceivedEvent event) {
        if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }
        String[] splitCommand = chatCommand.split(" ", 2);
        if (splitCommand.length <= 1) {
            clearMessagesInTextChannel(textChannel, 3);
            return;
        }
        if (!isInteger(splitCommand[1])) {
            textChannel.sendMessage("Argument must be a integer\n Usage: <clear [number of messages to delete]").queue();
            return;
        }
        clearMessagesInTextChannel(textChannel, Integer.parseInt(splitCommand[1]));

    }

    private void clearMessagesInTextChannel(TextChannel textChannel, int numberOfMessages) {
        List<Message> messages = textChannel.getHistory().retrievePast(numberOfMessages + 1).complete();
        textChannel.deleteMessages(messages).queue();
        //textChannel.deleteMessages(textChannel.getHistory().retrievePast().complete()).queue(); ;
    }

    private List<String> getCommandArgs(String commandString) {
        List<String> allMatches = new ArrayList<String>();
        Matcher m = Pattern.compile("-[^ ]*\\b")
                .matcher(commandString);
        while (m.find()) {
            String match = m.group();
            if (!match.equals(StringUtils.EMPTY)) {
                allMatches.add(match);
            }
        }
        return allMatches;
    }

    private String getSearchQuery(String commandString) {
        List<String> allMatches = new ArrayList<String>();
        Matcher m = Pattern.compile("([^-]*$)")
                .matcher(commandString);
        while (m.find()) {
            String match = m.group();
            if (!match.equals(StringUtils.EMPTY)) {
                allMatches.add(match);
            }
        }
        for (String a : allMatches) {
            if (a.length() != 0) {
                return a.split(" ", 2)[1];
            }
        }
        return StringUtils.EMPTY;
    }


}
