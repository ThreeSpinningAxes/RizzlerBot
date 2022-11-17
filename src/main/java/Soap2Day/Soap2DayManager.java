package Soap2Day;

import com.microsoft.playwright.*;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;

public class Soap2DayManager {

    Playwright playwright;

    Browser browser;

    Map<Long, Soap2DayGuildManager> soap2DayGuildManagerMap = new HashMap<>();
    private static Soap2DayManager instance;

    private Soap2DayManager() {
        playwright = Playwright.create();
        browser = playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(true).setTimeout(1500));
    }

    public Soap2DayGuildManager getSoap2DayGuildManager(Guild guild) {
        if (soap2DayGuildManagerMap.get(guild) == null) {
            BrowserContext browserContext = browser.newContext();
            browserContext.setDefaultTimeout(10000);
            Soap2DayGuildManager soap2DayGuildManager = new Soap2DayGuildManager(guild, browserContext);
            soap2DayGuildManagerMap.put(guild.getIdLong(), soap2DayGuildManager);
            return soap2DayGuildManager;
        }
        return soap2DayGuildManagerMap.get(guild.getIdLong());
    }

    public synchronized static Soap2DayManager getInstance() {
        if (instance == null) {
            instance = new Soap2DayManager();
            return instance;
        }
        return instance;
    }

    public void cleanUp() {
        this.browser.close();
        this.playwright.close();
    }

}
