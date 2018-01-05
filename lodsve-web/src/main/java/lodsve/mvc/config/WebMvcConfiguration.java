package lodsve.mvc.config;

import lodsve.core.properties.autoconfigure.annotations.EnableConfigurationProperties;
import lodsve.mvc.properties.ServerProperties;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.resource.DefaultServletHttpRequestHandler;

import java.util.Map;

/**
 * web mvc 配置,扫描包路径.
 *
 * @author sunhao(sunhao.java@gmail.com)
 * @version V1.0, 16/1/28 上午10:58
 */
@Configuration
@ComponentScan(basePackages = {"lodsve.mvc"})
@EnableConfigurationProperties(ServerProperties.class)
public class WebMvcConfiguration {
    @Bean
    public DefaultServletHttpRequestHandler defaultServletHttpRequestHandler() {
        return new DefaultServletHttpRequestHandler();
    }

    @Bean
    public SimpleUrlHandlerMapping simpleUrlHandlerMapping() {
        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        Map<String, String> urlMap = new ManagedMap<>();
        urlMap.put("/**", "defaultServletHttpRequestHandler");
        simpleUrlHandlerMapping.setUrlMap(urlMap);

        return simpleUrlHandlerMapping;
    }
}
