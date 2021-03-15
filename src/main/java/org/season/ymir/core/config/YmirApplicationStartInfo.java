package org.season.ymir.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import java.util.Properties;

/**
 * 启动日志
 *
 * @author KevinClair
 */
public class YmirApplicationStartInfo implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(YmirApplicationStartInfo.class);

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String PATH = "/META-INF/maven/org.season/ymir/pom.properties";
    private static final String BANNAR = "\n" +
            "██╗   ██╗███╗   ███╗██╗██████╗ \n" +
            "╚██╗ ██╔╝████╗ ████║██║██╔══██╗\n" +
            " ╚████╔╝ ██╔████╔██║██║██████╔╝\n" +
            "  ╚██╔╝  ██║╚██╔╝██║██║██╔══██╗\n" +
            "   ██║   ██║ ╚═╝ ██║██║██║  ██║\n" +
            "   ╚═╝   ╚═╝     ╚═╝╚═╝╚═╝  ╚═╝\n";

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(final ApplicationEnvironmentPreparedEvent event) {
        logger.info(buildBannerText());
    }

    private String buildBannerText() {
        StringBuilder bannerTextBuilder = new StringBuilder();
        bannerTextBuilder.append(LINE_SEPARATOR).append(BANNAR).append(" :: Ymir ::         (v").append(getVersion()).append(")").append(LINE_SEPARATOR);
        return bannerTextBuilder.toString();
    }

    private static String getVersion() {
        String version = null;
        try {
            Properties properties = new Properties();
            properties.load(YmirApplicationStartInfo.class.getResourceAsStream(PATH));
            version = properties.getProperty("version");
        } catch (Exception e) {
        }
        return version;
    }

}
