package lodsve.mongodb.core;

import com.mongodb.MongoClientURI;
import lodsve.core.properties.autoconfigure.PropertiesConfigurationFactory;
import lodsve.mongodb.config.MongoProperties;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.StringUtils;

/**
 * mongo db datasource.
 *
 * @author sunhao(sunhao.java@gmail.com)
 * @version V1.0, 16/1/21 下午6:15
 */
public class MongoDataSourceBeanDefinitionFactory {
    private static final String URL_PREFIX = "mongodb://";

    private String dataSourceName;
    private MongoProperties mongoProperties;

    public MongoDataSourceBeanDefinitionFactory(String dataSourceName) {
        this.dataSourceName = dataSourceName;

        this.mongoProperties = new PropertiesConfigurationFactory.Builder<>(MongoProperties.class).build();
    }

    public BeanDefinition build() {
        BeanDefinitionBuilder mongoURIBean = BeanDefinitionBuilder.genericBeanDefinition(MongoClientURI.class);
        mongoURIBean.addConstructorArgValue(getMongoUri());

        return mongoURIBean.getBeanDefinition();
    }

    private String getMongoUri() {
        MongoProperties.MongoConnection connection = mongoProperties.getProject().get(dataSourceName);

        String url = connection.getUrl();
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException("uri must not null");
        }
        if (!url.startsWith(URL_PREFIX)) {
            throw new IllegalArgumentException("uri needs to start with " + URL_PREFIX);
        }

        StringBuilder uriBuilder = new StringBuilder(URL_PREFIX);


        String username = connection.getUsername();
        String password = connection.getPassword();

        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            uriBuilder.append(username + ":" + password + "@");
        }

        uriBuilder.append(url.substring(URL_PREFIX.length()));

        uriBuilder.append("?maxpoolsize=");
        if (connection.getMaxpoolsize() != 0) {
            uriBuilder.append(connection.getMaxpoolsize());
        } else {
            uriBuilder.append(mongoProperties.getMaxpoolsize());
        }

        return uriBuilder.toString();
    }
}
