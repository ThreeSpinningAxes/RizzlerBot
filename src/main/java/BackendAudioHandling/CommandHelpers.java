package BackendAudioHandling;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import enums.AudioSourceManagerClasses;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


import static MessageFormats.GeneralMessage.*;

import java.net.URL;

public final class CommandHelpers {

    public static boolean isURL(String link)  {
        try {
            URL url = new URL(link);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static boolean isUserConnectedToVoiceChannel(Guild guild, TextChannel textChannel, MessageReceivedEvent event) {
        if ((VoiceChannel) event.getMember().getVoiceState().getChannel() == null) {
            textChannel.sendMessage(userMustBeConnectedToVCMessageFormat()).queue();
            return false;
        }
        return true;
    }

    public static int getQueueSize(Guild guild) {
        return PlayerManager.getInstance().getMusicManager(guild).trackScheduler.getQueueSize();
    }

    public static boolean isBotConnectedToVoiceChannel(Guild guild, TextChannel textChannel) {
        VoiceChannel voiceChannelOfBot = (VoiceChannel) guild.getSelfMember().getVoiceState().getChannel();
        if (voiceChannelOfBot == null) {
            textChannel.sendMessage(botMustBeConnectedToVCMessageFormat()).queue();
            return false;
        }
        return true;
    }

    public static boolean hasVoicePermissions(Guild guild, TextChannel textChannel, MessageReceivedEvent event) {
        if (!guild.getSelfMember().hasPermission(event.getChannel().asGuildMessageChannel(), Permission.VOICE_CONNECT)) {
            textChannel.sendMessage(botNeedsPermissionsToConnectToVCMessageFormat()).queue();
            return false;
        }
        return true;
    }

    public static boolean isMusicChannel(Guild guild, TextChannel textChannel) {
        if (!textChannel.getName().equalsIgnoreCase("music")) {
            textChannel.sendMessage(mustBeMusicChatMessageFormat()).queue();
            return false;
        }
        return true;
    }
    public static boolean isInteger(String string) {
        try {
            int x = Integer.parseInt(string);
        }
        catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isInValidRange(int num, int size) {
        return num >= 1 && num <= size;
    }


    public static void disconnectFromVoiceChannel(Guild guild, TextChannel textChannel) {
        guild.getAudioManager().closeAudioConnection();
        PlayerManager.getInstance().getMusicManager(guild).trackScheduler.resetPlayer();
        textChannel.sendMessage(disconnectFromChannelMessage(guild)).queue();
    }

    public static <T extends AudioSourceManager> Class<T> getAudioSourceManagerClass(String sourceName) {
        sourceName = sourceName.toUpperCase();
        AudioSourceManagerClasses sourceClass = AudioSourceManagerClasses.valueOf(sourceName);
        return sourceClass.getSourceManager();
    }

    public static boolean isValidAudioSource(String sourceName) {
        try {
            AudioSourceManagerClasses.valueOf(sourceName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
