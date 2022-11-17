package BackendAudioHandling;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import static MessageFormats.GeneralMessage.*;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {

    private static PlayerManager instance;
    private Map<Long, GuildMusicManager> guildMusicManagers;
    private AudioPlayerManager audioPlayerManager;

    public PlayerManager(){
        this.guildMusicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);

    }

    public AudioPlayerManager getAudioPlayerManager() {
        return this.audioPlayerManager;
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.guildMusicManagers.computeIfAbsent(guild.getIdLong(), guildId ->
        {
            GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public void stopCurrentSong(TextChannel textChannel) {
        GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());
        musicManager.trackScheduler.stopTrack(textChannel);
    }

    public void getCurrentTrack(TextChannel textChannel) {
        GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());
        musicManager.trackScheduler.getCurrentTrack(textChannel);
    }

    public void resumeCurrentSong(TextChannel textChannel) {
        GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());
        musicManager.trackScheduler.resumeTrack(textChannel);
    }

    public void skipTrack(TextChannel textChannel) {
        GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());
        AudioPlayer audioPlayer = musicManager.trackScheduler.audioPlayer;
        if (audioPlayer.getPlayingTrack() != null) {
            textChannel.sendMessage(skippingTrackMessageFormat(getTitle(audioPlayer))).queue();
        }
        musicManager.trackScheduler.nextTrack();
        if (audioPlayer.getPlayingTrack() != null) {
            textChannel.sendMessage(nowPlayingTrackMessageFormat(getTitle(audioPlayer))).queue();
        }
        else {
            textChannel.sendMessage(queueIsEmptyMessageFormat()).queue();
        }

    }

    public void skipSpecificNumberOfTracksFromBeginning(int number, TextChannel textChannel) {
        GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());
        int queueSize = musicManager.trackScheduler.getQueueSize();
        if (queueSize < number-1) {
            textChannel.sendMessage("Cannot skip " + number + " number of tracks since the queue size (" + queueSize + ") is smaller then" +
                    "skip number").queue();
            return;
        }
        for (int i = 0; i < number; i++) {
            musicManager.trackScheduler.nextTrack();
        }
        AudioPlayer audioPlayer = musicManager.trackScheduler.audioPlayer;
        textChannel.sendMessage(nowPlayingTrackMessageFormat(audioPlayer.getPlayingTrack().getInfo().title)).queue();
    }

    public void viewTracksInQueue(TextChannel textChannel) {
        GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());
        musicManager.trackScheduler.viewTracksInQueue(textChannel);
    }

    public void playSpecificTrackInQueueNext(TextChannel textChannel, int queueNumber) {
        GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());
        musicManager.trackScheduler.playSpecificTrackInQueueNext(textChannel, queueNumber);
    }

    public void resetPlayer(TextChannel textChannel) {
        GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());
        musicManager.trackScheduler.resetPlayer();
        textChannel.sendMessage(queueIsResetMessageFormat()).queue();
    }

    public void shuffleQueue(TextChannel textChannel) {
        GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());
        if (musicManager.trackScheduler.getQueueSize() < 2) {
            textChannel.sendMessage("Queue size needs to be greater than 2 to shuffle").queue();
            return;
        }
        musicManager.trackScheduler.shuffleQueue(textChannel);
    }

    public void replayCurrentTrack(TextChannel textChannel) {
        GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());
        if (musicManager.trackScheduler.audioPlayer.getPlayingTrack() == null) {
            textChannel.sendMessage(noTrackIsPlayingMessageFormat()).queue();
            return;
        }
        musicManager.trackScheduler.replayCurrentTrack();
    }

    public void loadAndPlay(TextChannel textChannel, String trackIdentifier) {
        GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());

        this.audioPlayerManager.loadItemOrdered(musicManager, trackIdentifier, 
                new DefaultAudioResultHandler(musicManager, trackIdentifier, textChannel));
    }

    public void loadAndPlayTrackNext(TextChannel textChannel, String trackIdentifier) {
        GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());

        if (musicManager.trackScheduler.audioPlayer.getPlayingTrack() == null) {
            this.audioPlayerManager.loadItemOrdered(musicManager, trackIdentifier,
                    new DefaultAudioResultHandler(musicManager, trackIdentifier, textChannel));
        } else {
            this.audioPlayerManager.loadItemOrdered(musicManager, trackIdentifier,
                    new AudioResultHandlerForNextSong(musicManager, trackIdentifier, textChannel));
        }
    }

    public <T> void playFromSpecificSource(TextChannel textChannel, String trackIdentifier, Class<AudioSourceManager> source) {
        GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());
        //AudioItem audioItem = this.audioPlayerManager.source(source).loadItem((AudioPlayerManager) musicManager,
               // new AudioReference(trackIdentifier, (String)null));

        if (source.equals(YoutubeAudioSourceManager.class)) {
            trackIdentifier = "ytsearch: " + trackIdentifier;
            loadAndPlay(textChannel, trackIdentifier);
        }
        else if (source.equals(SoundCloudAudioSourceManager.class)) {
            trackIdentifier = "scsearch: " + trackIdentifier;
            loadAndPlay(textChannel, trackIdentifier);
        }
    }

    public String getTitle(AudioPlayer audioPlayer) {
        return audioPlayer.getPlayingTrack().getInfo().title;
    }

    public String getTitle(AudioTrack track) {
        return track.getInfo().title;
    }

    public static synchronized PlayerManager getInstance() {

        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }

}
