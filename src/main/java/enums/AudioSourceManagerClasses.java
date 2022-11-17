package enums;

import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;

public enum AudioSourceManagerClasses {
    YOUTUBE, TWITCH, SOUNDCLOUD;
    //BANDCAMP(BandcampAudioSourceManager),
    //VIMEO(VimeoAudioSourceManager),
    //BEAM(BeamAudioSourceManager),
    //GETYARN(GetyarnAudioSourceManager);

    public <T> Class<T> getSourceManager() {
        switch (this) {
            case YOUTUBE:
                return (Class<T>) YoutubeAudioSourceManager.class;

            case SOUNDCLOUD:
                return (Class<T>) SoundCloudAudioSourceManager.class;

            case TWITCH:
                return (Class<T>) TwitchStreamAudioSourceManager.class;

            default:
                return null;
        }
    }


}
