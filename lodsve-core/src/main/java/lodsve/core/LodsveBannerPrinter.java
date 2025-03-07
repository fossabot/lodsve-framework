/*
 * Copyright (C) 2019 Sun.Hao(https://www.crazy-coder.cn/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lodsve.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import lodsve.core.configuration.ApplicationProperties;
import lodsve.core.configuration.BannerConfig;
import lodsve.core.configuration.BannerMode;
import lodsve.core.io.support.LodsveResourceLoader;
import lodsve.core.properties.relaxedbind.RelaxedBindFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模仿spring-boot打印出banner.
 *
 * @author <a href="mailto:sunhao.java@gmail.com">sunhao(sunhao.java@gmail.com)</a>
 * @date 2018/1/11 下午10:13
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class LodsveBannerPrinter implements WebApplicationInitializer {
    private static final Logger logger = LoggerFactory.getLogger(LodsveBannerPrinter.class);

    private static final Banner DEFAULT_BANNER = new LodsveBanner();
    private static final String[] IMAGE_EXTENSION = {"gif", "jpg", "png"};
    private static final ResourceLoader RESOURCE_LOADER = new LodsveResourceLoader();
    /**
     * 缓存日志级别
     */
    private static final Map<String, Level> LEVEL_CACHE = new HashMap<>();

    private BannerConfig bannerConfig;

    @Override
    public void onStartup(@NonNull ServletContext servletContext) throws ServletException {
        closeLogback();

        ApplicationProperties properties = new RelaxedBindFactory.Builder<>(ApplicationProperties.class).build();
        bannerConfig = properties.getBanner();

        if (!bannerConfig.isEnable()) {
            // no banners!
            return;
        }

        if (BannerMode.OFF.equals(bannerConfig.getMode())) {
            return;
        }

        Banner banner = getBanner();

        if (BannerMode.LOGGER.equals(bannerConfig.getMode())) {
            printInLogger(banner);
            return;
        }

        printBannerInConsole(banner);

        openLogback();
    }

    private void openLogback() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        List<ch.qos.logback.classic.Logger> loggers = loggerContext.getLoggerList();
        loggers.forEach(l -> l.setLevel(LEVEL_CACHE.get(l.getName())));
    }

    private void closeLogback() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        List<ch.qos.logback.classic.Logger> loggers = loggerContext.getLoggerList();
        loggers.forEach(l -> {
            LEVEL_CACHE.put(l.getName(), l.getLevel());
            l.setLevel(Level.OFF);
        });
    }

    private void printInLogger(Banner banner) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        banner.print(bannerConfig, new PrintStream(output));
        try {
            String bannerContent = output.toString(bannerConfig.getCharset());

            logger.info(bannerContent);
        } catch (UnsupportedEncodingException e) {
            logger.warn("Failed to create String for banner", e);
        }
    }

    private void printBannerInConsole(Banner banner) {
        banner.print(bannerConfig, System.out);
    }

    private Banner getBanner() {
        Banners banners = new Banners();
        banners.addIfNotNull(getImageBanner(bannerConfig));
        banners.addIfNotNull(getTextBanner(bannerConfig));
        if (banners.hasAtLeastOneBanner()) {
            return banners;
        }
        return DEFAULT_BANNER;
    }

    private Banner getImageBanner(BannerConfig bannerConfig) {
        String location = bannerConfig.getImage().getLocation();
        if (StringUtils.hasLength(location)) {
            Resource resource = RESOURCE_LOADER.getResource(location);
            return (resource.exists() ? new ImageBanner(resource) : null);
        }

        for (String ext : IMAGE_EXTENSION) {
            Resource resource = RESOURCE_LOADER.getResource("banner." + ext);
            if (resource.exists()) {
                return new ImageBanner(resource);
            }
        }

        return null;
    }

    private Banner getTextBanner(BannerConfig bannerConfig) {
        String location = bannerConfig.getLocation();
        Resource resource = RESOURCE_LOADER.getResource(location);
        if (resource.exists()) {
            return new TextBanner(resource);
        }

        return null;
    }

    static class Banners implements Banner {
        private List<Banner> banners = new ArrayList<>(16);

        @Override
        public void print(BannerConfig config, PrintStream out) {
            for (Banner banner : banners) {
                banner.print(config, out);
            }
        }

        void addIfNotNull(Banner banner) {
            if (banner != null) {
                banners.add(banner);
            }
        }

        boolean hasAtLeastOneBanner() {
            return banners.size() > 0;
        }
    }
}
