import BackendAudioHandling.CommandHelpers;
import BackendAudioHandling.PlayerManager;
import enums.AudioSourceManagerClasses;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import static BackendAudioHandling.CommandHelpers.*;
import static MessageFormats.GeneralMessage.*;


import java.util.List;
import java.util.Objects;


public class MusicCommands extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            return;
        }

        Guild guild = event.getGuild();

        //the chat the user typed in for this event
        TextChannel textChannel = event.getChannel().asTextChannel();
        String chatCommand = event.getMessage().getContentRaw().trim();
        if (chatCommand.equals("<join")) {
            joinCommand(guild, textChannel, event);
        } else if (chatCommand.equals("<leave")) {
            leaveCommand(guild, textChannel);
        } else if (chatCommand.startsWith("<play ")) {
            playCommand(guild, textChannel, event, chatCommand);
        } else if (chatCommand.startsWith("<playnext ")) {
            playNextCommand(guild, textChannel, event, chatCommand);
        } else if (chatCommand.equals("<pause")) {
            pauseCommand(guild, textChannel);
        } else if (chatCommand.equals("<resume")) {
            resumeCommand(guild, textChannel);
        } else if (chatCommand.equals("<currenttrack")) {
            getCurrentTrackCommand(guild, textChannel);
        } else if(chatCommand.equals("<skip")) {
            skipCurrentTrackCommand(guild, textChannel);
        } else if(chatCommand.startsWith("<skip ")) {
            skipTrackWithArgumentsCommand(guild, textChannel, chatCommand);
        } else if (chatCommand.equals("<reset")) {
            resetCommand(guild, textChannel);
        } else if (chatCommand.equals("<viewtracks")) {
            viewTracksInQueueCommand(guild, textChannel);
        } else if (chatCommand.startsWith("<playinqueue ")) {
            playSpecificSongInQueueNextCommand(guild, textChannel, chatCommand);
        } else if (chatCommand.equals("<shuffle")) {
            shuffleCommand(guild, textChannel);
        } else if (chatCommand.equals("<replay")) {
            replayCommand(guild, textChannel);
        } else if (chatCommand.startsWith("<playfromsource ")) {
            playFromSpecificSourceCommand(guild, textChannel, event, chatCommand);
        }
    }

    public boolean joinCommand(Guild guild, TextChannel textChannel, MessageReceivedEvent event) {

        if (!isMusicChannel(guild, textChannel) || !CommandHelpers.hasVoicePermissions(guild, textChannel, event) ||
                !isUserConnectedToVoiceChannel(guild, textChannel, event)) {
            return false;
        }
        connectToVoiceChannel(guild, textChannel, event);
        return true;
    }

    public void leaveCommand(Guild guild, TextChannel textChannel) {

        if (!CommandHelpers.isBotConnectedToVoiceChannel(guild, textChannel)) {
            return;
        }
        disconnectFromVoiceChannel(guild, textChannel);
    }
    public void playCommand(Guild guild, TextChannel textChannel, MessageReceivedEvent event, String chatCommand) {
        if (!joinCommand(guild, textChannel, event)) {
            return;
        }
        String[] splitCommand = chatCommand.split(" ", 2);

        if (splitCommand.length <= 1) {
            textChannel.sendMessage("No track reference provided. Please provide audio link or search query").queue();
            return;
        }

        String songurl = splitCommand[1];

        if (!isURL(songurl)) {
            songurl = "ytsearch: " + songurl;
            textChannel.sendMessage(formattedMessage("No url is provided, searching for track instead")).queue();
        }
        PlayerManager.getInstance().loadAndPlay(textChannel, songurl);
    }

    public void playNextCommand(Guild guild, TextChannel textChannel, MessageReceivedEvent event, String chatCommand) {
        if (!joinCommand(guild, textChannel, event)) {
            return;
        }
        String[] splitCommand = chatCommand.split(" ", 2);

        if (splitCommand.length <= 1) {
            textChannel.sendMessage("No track reference provided. Please provide audio link or search query").queue();
            return;
        }

        String songurl = splitCommand[1];

        if (!isURL(songurl)) {
            songurl = "ytsearch: " + songurl;
            textChannel.sendMessage(formattedMessage("No url is provided, searching for track instead")).queue();
        }

        PlayerManager.getInstance().loadAndPlayTrackNext(textChannel, songurl);
    }

    public void playFromSpecificSourceCommand(Guild guild, TextChannel textChannel, MessageReceivedEvent event, String chatCommand) {
        if (!joinCommand(guild, textChannel, event)) {
            return;
        }
        String[] splitCommand = chatCommand.split(" ", 3);
        if (splitCommand.length <= 2) {
            textChannel.sendMessage("All query parameters are not provided.\nCorrect usage: " +
                    "<playspecific [source-name] [url or search query]").queue();
            return;
        }
        String source = splitCommand[1];
        if (!isValidAudioSource(source)) {
            textChannel.sendMessage("Source " + source + " is not a valid audio source").queue();
            return;
        }
        String trackIdentifier = splitCommand[2];

        PlayerManager.getInstance().playFromSpecificSource(textChannel, trackIdentifier,
                Objects.requireNonNull(AudioSourceManagerClasses.valueOf(source.toUpperCase()).getSourceManager()));
    }

    public void viewTracksInQueueCommand(Guild guild, TextChannel textChannel) {
        if (!isBotConnectedToVoiceChannel(guild, textChannel)) {
            return;
        }
        PlayerManager.getInstance().viewTracksInQueue(textChannel);
    }

    public void playSpecificSongInQueueNextCommand(Guild guild, TextChannel textChannel, String chatCommand) {
        if (!isBotConnectedToVoiceChannel(guild, textChannel)) {
            return;
        }

        String[] splitCommand = chatCommand.split(" ", 2);

        if (splitCommand.length <= 1) {
            textChannel.sendMessage("Please specify which track in queue you want to play next" +
                    "\nCorrect usage:<playinqueue [song number in queue]").queue();
            return;
        }
        if (!isInteger(splitCommand[1])) {
            textChannel.sendMessage("Please use a number to specify the track number").queue();
            return;
        }
        if (getQueueSize(guild) == 0) {
            textChannel.sendMessage("Can't play anything from empty queue").queue();
            return;
        }
        int queueNumber = Integer.parseInt(splitCommand[1]);
        PlayerManager.getInstance().playSpecificTrackInQueueNext(textChannel, queueNumber);

    }

    public void replayCommand(Guild guild, TextChannel textChannel) {
        if (!isBotConnectedToVoiceChannel(guild, textChannel)) {
            return;
        }
        PlayerManager.getInstance().replayCurrentTrack(textChannel);
    }

    public void shuffleCommand(Guild guild, TextChannel textChannel) {
        if (!isBotConnectedToVoiceChannel(guild, textChannel)) {
            return;
        }
        PlayerManager.getInstance().shuffleQueue(textChannel);
    }

    public void pauseCommand(Guild guild, TextChannel textChannel) {
        if (!isBotConnectedToVoiceChannel(guild, textChannel)) {
            return;
        }
        PlayerManager.getInstance().stopCurrentSong(textChannel);
    }

    public void resumeCommand(Guild guild, TextChannel textChannel) {
        if (!isBotConnectedToVoiceChannel(guild, textChannel)) {
            return;
        }
        PlayerManager.getInstance().resumeCurrentSong(textChannel);
    }

    public void getCurrentTrackCommand(Guild guild, TextChannel textChannel) {
        if (!isBotConnectedToVoiceChannel(guild, textChannel)) {
            return;
        }
        PlayerManager.getInstance().getCurrentTrack(textChannel);
    }

    public void skipCurrentTrackCommand(Guild guild, TextChannel textChannel) {
        if (!isBotConnectedToVoiceChannel(guild, textChannel)) {
            return;
        }
        PlayerManager.getInstance().skipTrack(textChannel);

    }

    public void skipTrackWithArgumentsCommand(Guild guild, TextChannel textChannel, String chatCommand) {
        if (!isBotConnectedToVoiceChannel(guild, textChannel)) {
            return;
        }
        String[] splitCommand = chatCommand.split(" ", 3);
        if (splitCommand.length != 3) {
            textChannel.sendMessage("Please provide value for argument").queue();
        }

        if (splitCommand[1].equalsIgnoreCase("-n")) {
            if (!isInteger(splitCommand[2])) {
                textChannel.sendMessage("Please provide a number as argument to this command").queue();
            }
            int skipNumber = Integer.parseInt(splitCommand[2]);
            if (skipNumber == 0) {
                PlayerManager.getInstance().skipTrack(textChannel);
            }
            else {
                PlayerManager.getInstance().skipSpecificNumberOfTracksFromBeginning(Integer.parseInt(splitCommand[2]), textChannel);
            }
        }
       // else if (splitCommand[1].equalsIgnoreCase(""))

    }

    public void resetCommand(Guild guild, TextChannel textChannel) {
        if (!isBotConnectedToVoiceChannel(guild, textChannel)) {
            return;
        }
        PlayerManager.getInstance().resetPlayer(textChannel);
    }

    private void connectToVoiceChannel(Guild guild, TextChannel textChannel, MessageReceivedEvent event) {
        VoiceChannel usersVoiceChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();
        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.getConnectedChannel() == null) {
            audioManager.openAudioConnection(usersVoiceChannel);
            textChannel.sendMessage("Connected to " + usersVoiceChannel.getName() + "!").queue();
        }
    }


}




