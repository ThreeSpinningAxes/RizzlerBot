package BackendAudioHandling;

import BackendAudioHandling.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.TextChannel;
import static MessageFormats.GeneralMessage.*;

public class DefaultAudioResultHandler implements AudioLoadResultHandler {

    GuildMusicManager guildMusicManager;
    TextChannel textChannel;

    String trackidentifier;

    public DefaultAudioResultHandler(GuildMusicManager guildMusicManager, String trackidentifier,
                                     TextChannel textChannel) {
        this.guildMusicManager = guildMusicManager;
        this.trackidentifier = trackidentifier;
        this.textChannel = textChannel;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        this.guildMusicManager.trackScheduler.queue(track);
        if (this.guildMusicManager.trackScheduler.getQueueSize() > 0) {
            textChannel.sendMessage(trackAddedToQueueMessageFormat(track,
                    this.guildMusicManager.trackScheduler.getQueueSize())).queue();
        } else {
            textChannel.sendMessage(playingMessageFormat(track)).queue();
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {

        if (playlist.isSearchResult()) {
            AudioTrack track = playlist.getTracks().get(0);
            if (this.guildMusicManager.trackScheduler.getQueueSize() > 0) {
                textChannel.sendMessage(trackAddedToQueueMessageFormat(track,
                        this.guildMusicManager.trackScheduler.getQueueSize())).queue();
            }
            else {
                textChannel.sendMessage(playingMessageFormat(track)).queue();
            }
            this.guildMusicManager.trackScheduler.queue(track);
        }
        else {
            textChannel.sendMessage(playlistLoadedMessageFormat(playlist)).queue();
            for (AudioTrack track : playlist.getTracks()) {
                this.guildMusicManager.trackScheduler.queue(track);
            }
        }
    }

    @Override
    public void noMatches() {
        // Notify the user that we've got nothing
        textChannel.sendMessage(noMatchesMessageFormat(trackidentifier)).queue();
    }

    @Override
    public void loadFailed(FriendlyException throwable) {
        textChannel.sendMessage(loadFailedMessageFormat(throwable.getMessage())).queue();
    }
}
