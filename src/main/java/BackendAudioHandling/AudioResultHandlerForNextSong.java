package BackendAudioHandling;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.TextChannel;


import static MessageFormats.GeneralMessage.*;

public class AudioResultHandlerForNextSong extends DefaultAudioResultHandler implements AudioLoadResultHandler {
    public AudioResultHandlerForNextSong(GuildMusicManager guildMusicManager, String trackidentifier, TextChannel textChannel) {
        super(guildMusicManager, trackidentifier, textChannel);
    }

    @Override
    public void trackLoaded(AudioTrack track) {

        this.guildMusicManager.trackScheduler.queue(track);
        this.guildMusicManager.trackScheduler.putLastTrackInQueueNext();
        textChannel.sendMessage("Playing track next: " + track.getInfo().title).queue();
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {

        AudioTrack audioTrack = playlist.getTracks().get(0);
        if (playlist.isSearchResult()) {
            this.guildMusicManager.trackScheduler.queue(audioTrack);
            this.guildMusicManager.trackScheduler.putLastTrackInQueueNext();
            textChannel.sendMessage("Playing track next: " + audioTrack.getInfo().title).queue();
        }
        else {
            textChannel.sendMessage(playlistLoadedMessageFormat(playlist)).queue();
            for (AudioTrack track : playlist.getTracks()) {
                this.guildMusicManager.trackScheduler.queue(track);
            }
        }

    }

}
