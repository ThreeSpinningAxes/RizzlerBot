package MessageFormats;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;


public class GeneralMessage {
    public static String userMustBeConnectedToVCMessageFormat() {
       return formattedMessage("You must be connected to a voice channel for me to join");
    }

    public static String botMustBeConnectedToVCMessageFormat() {
        return formattedMessage("Bot must be connected to a voice channel");
    }

    public static String botNeedsPermissionsToConnectToVCMessageFormat() {
        return formattedMessage("I do not have permissions to join your voice channel");
    }

    public static String disconnectFromChannelMessage(Guild guild) {
        return formattedMessage("Disconnected from " + guild.getSelfMember().getVoiceState().getChannel().getName());
    }

    public static String mustBeMusicChatMessageFormat() {
        return formattedMessage("Commands must be sent through music chat");
    }

    public static String playingMessageFormat(AudioTrack track) {
        return formattedMessage("Playing: " + track.getInfo().title);
    }
    public static String currentlyPlayingMessageFormat(AudioTrack track) {
        return formattedMessage("Currently Playing: " + track.getInfo().title);
    }
    public static String trackAddedToQueueMessageFormat(AudioTrack track, int queueSize) {
        return formattedMessage("Track added to queue: " + track.getInfo().title + "\n"
        + trackPositionInQueueMessageFormat(queueSize));
    }
    public static String trackPositionInQueueMessageFormat(int queueSize) {
        return "Position in queue: " + queueSize;
    }

    public static String playlistLoadedMessageFormat(AudioPlaylist playlist) {
        return formattedMessage("Playlist loaded: " + playlist.getName() + "\nTotal number of tracks in playlist: " + playlist.getTracks().size());
    }

    public static String noMatchesMessageFormat(String trackIdentifier) {
        return formattedMessage("No matches were found for " + trackIdentifier);
    }

    public static String loadFailedMessageFormat(String exceptionMessage) {
        return formattedMessage("Could not play: " + exceptionMessage);
    }

    public static String formattedMessage(String message) {
        return "```" + message + "```";
    }

    public static String noTrackIsPlayingMessageFormat() {
        return formattedMessage("No track is currently playing");
    }

    public static String skippingTrackMessageFormat(String trackTitle) {
        return formattedMessage("Skipping track: " + trackTitle);
    }

    public static String nowPlayingTrackMessageFormat(String trackTitle) {
        return formattedMessage("Now playing: " + trackTitle);
    }

    public static String queueIsEmptyMessageFormat() {
        return formattedMessage("Queue is currently empty");
    }

    public static String queueIsResetMessageFormat() {
        return formattedMessage("Track queue has been reset");
    }


}
