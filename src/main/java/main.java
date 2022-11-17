import Soap2Day.Soap2DayManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.security.auth.login.LoginException;

public class main {

    static final Logger LOG = LoggerFactory.getLogger(main.class);

    public static void main(String[] Args) throws LoginException, InterruptedException, ConfigurationException {
        PropertiesConfiguration properties = new PropertiesConfiguration("INFO.properties");
        Soap2DayManager.getInstance(); //instantiate playwright
        JDA bot = JDABuilder.createDefault(properties.getString("key"))
                .setActivity(Activity.playing("sober stream brother"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .enableCache(CacheFlag.VOICE_STATE)
                .addEventListeners(new MusicCommands(), new GeneralCommands())
                .build()
                .awaitReady();
    }
}
