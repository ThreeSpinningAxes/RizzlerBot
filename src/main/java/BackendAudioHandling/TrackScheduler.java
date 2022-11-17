package BackendAudioHandling;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {

    AudioPlayer audioPlayer;
    private BlockingQueue<AudioTrack> queue;
    public TrackScheduler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.

        this.audioPlayer.startTrack(queue.poll(), false);
    }

    public void resetPlayer() {
        this.queue.clear();
        audioPlayer.destroy();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    public int getQueueSize() {
        return this.queue.size();
    }

    public void shuffleQueue(TextChannel textChannel) {
        List<AudioTrack> trackList = new ArrayList<>();
        this.queue.drainTo(trackList);
        Collections.shuffle(trackList);
        this.queue.addAll(trackList);
        textChannel.sendMessage("Shuffled queue").queue();
    }

    public void replayCurrentTrack() {
        audioPlayer.getPlayingTrack().setPosition(0);
    }

    public void getCurrentTrack(TextChannel channel) {
        if (audioPlayer.getPlayingTrack() != null) {
            channel.sendMessage("Playing: " +
                    audioPlayer.getPlayingTrack().getInfo().author + " - " + audioPlayer.getPlayingTrack().getInfo().title).queue();
        }
        else {
            channel.sendMessage("No track is currently playing").queue();
        }
    }

    public void stopTrack(TextChannel channel) {
        if (audioPlayer.getPlayingTrack() != null) {
            if (!audioPlayer.isPaused()) {
                this.audioPlayer.setPaused(true);
                channel.sendMessage("Paused " + audioPlayer.getPlayingTrack().getInfo().title).queue();
            }
            else {
                channel.sendMessage("Audio track is already paused").queue();
            }
        }
        else {
            channel.sendMessage("No track is currently playing").queue();
        }

    }
    public void playSpecificTrackInQueueNext(TextChannel channel, int queueNumber) {
        if (queueNumber <= 0 || queueNumber > queue.size()) {
            channel.sendMessage("Track number exceeds the queue size or is a invalid number").queue();
            return;
        }

        List<AudioTrack> trackList = new ArrayList<>();
        this.queue.drainTo(trackList);

        AudioTrack wantedTrack = trackList.get(queueNumber-1);
        trackList.add(0, wantedTrack);
        trackList.remove(queueNumber);
        this.queue.addAll(trackList);
        channel.sendMessage("Loaded track at position " + queueNumber + " next: " + wantedTrack.getInfo().title).queue();
    }

    public void putLastTrackInQueueNext() {

        List<AudioTrack> trackList = new ArrayList<>();
        this.queue.drainTo(trackList);

        AudioTrack wantedTrack = trackList.get(queue.size()-1);
        trackList.add(0, wantedTrack);
        trackList.remove(queue.size());
        this.queue.addAll(trackList);
    }

    public void resumeTrack(TextChannel channel) {
        if (audioPlayer.getPlayingTrack() != null) {
            if (audioPlayer.isPaused()) {
                this.audioPlayer.setPaused(false);
                channel.sendMessage("Un-paused " + audioPlayer.getPlayingTrack().getInfo().title).queue();
            }
            else {
                channel.sendMessage("Audio track is already playing").queue();
            }
        }
        else {
            channel.sendMessage("No track is currently playing").queue();
        }
    }

    public void viewTracksInQueue(TextChannel textChannel) {
        if (audioPlayer.getPlayingTrack() == null && this.getQueueSize() == 0) {
            textChannel.sendMessage("No tracks are in queue").queue();
            return;
        }
        StringBuilder trackViewBuilder = new StringBuilder("```");
        if (queue.size() > 20) {
            trackViewBuilder.append("Queue size is large (" + queue.size() + " tracks total), only showing first 20 queued tracks\n\n");
        }
        AudioTrack currentTrack = audioPlayer.getPlayingTrack();
        trackViewBuilder.append("0: " + currentTrack.getInfo().title + " <----- Currently Playing \n");
        int counter = 1;
        for (AudioTrack track : this.queue) {
            if (counter <= 20) {
                trackViewBuilder.append(counter + ": " + track.getInfo().title + "\n");
                counter+=1;
            }
            else {
                break;
            }
        }
        trackViewBuilder.append("```");
        textChannel.sendMessage(trackViewBuilder.toString()).queue();
    }


    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track) {
        if (!audioPlayer.startTrack(track, true)) {
            this.queue.offer(track);
        }
    }
}
