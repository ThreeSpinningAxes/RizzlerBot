package BackendAudioHandling;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildMusicManager {
    TrackScheduler trackScheduler;
    private final AudioPlayer audioPlayer;
    public GuildMusicManager(AudioPlayerManager audioManager) {
        audioPlayer = audioManager.createPlayer();
        trackScheduler = new TrackScheduler(audioPlayer);
        audioPlayer.addListener(trackScheduler);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(this.audioPlayer);
    }
}
