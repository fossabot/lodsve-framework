package lodsve.core.autoconfigure;

import lodsve.core.autoconfigure.annotations.ConfigurationProperties;
import lodsve.core.autoconfigure.annotations.Required;
import lodsve.core.properties.Env;
import lodsve.core.properties.configuration.Configuration;
import lodsve.core.properties.configuration.ConfigurationLoader;
import lodsve.core.properties.configuration.PropertiesConfiguration;
import lodsve.core.properties.core.ParamsHome;
import lodsve.core.utils.GenericUtils;
import lodsve.core.utils.PropertyPlaceholderHelper;
import lodsve.core.utils.StringUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.Assert;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 自动装配生成器.
 *
 * @author sunhao(sunhao.java@gmail.com)
 * @version V1.0, 2016-1-26 14:17
 */
public class AutoConfigurationBuilder {
    private static final Logger logger = LoggerFactory.getLogger(AutoConfigurationBuilder.class);

    private static final List<? extends Class<? extends Serializable>> SIMPLE_CLASS = Arrays.asList(Boolean.class, boolean.class, Long.class, long.class,
            Integer.class, int.class, String.class, Double.class, double.class);
    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    private static final Map<Class<?>, Object> CLASS_OBJECT_MAPPING = new HashMap<>(16);

    private AutoConfigurationBuilder() {
    }

    @SuppressWarnings("unchecked")
    private <T> T generateConfigurationBean(Class<T> clazz, ConfigurationProperties annotation) {
        Configuration configuration = loadProp(annotation.locations());
        T object = (T) CLASS_OBJECT_MAPPING.get(clazz);
        if (object == null) {
            object = generateObject(annotation.prefix(), clazz, configuration);
            CLASS_OBJECT_MAPPING.put(clazz, object);
        }

        return object;
    }

    private <T> T generateObject(String prefix, Class<T> clazz, Configuration configuration) {
        T object = BeanUtils.instantiate(clazz);
        BeanWrapper beanWrapper = new BeanWrapperImpl(object);

        PropertyDescriptor[] descriptors = beanWrapper.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : descriptors) {
            if (descriptor.getWriteMethod() == null) {
                continue;
            }

            String name = descriptor.getName();
            Class<?> type = descriptor.getPropertyType();
            Method method = descriptor.getReadMethod();
            TypeDescriptor typeDescriptor = beanWrapper.getPropertyTypeDescriptor(name);
            Required required = typeDescriptor.getAnnotation(Required.class);

            String key = prefix + "." + name;
            Object value = getValue(type, key, method, configuration);

            if (value == null) {
                value = getValue(type, prefix + "." + getCamelName(name), method, configuration);
            }

            if (value != null) {
                beanWrapper.setPropertyValue(name, value);
            } else if (required != null) {
                throw new RuntimeException(String.format("property [%s]'s value can't be null!please check your config!", name));
            }
        }

        return object;
    }

    private Object getValue(Class<?> type, String key, Method method, Configuration configuration) {
        Object value;

        if (isSimpleType(type)) {
            value = getValueForSimpleType(key, type, configuration);
        } else if (Map.class.equals(type)) {
            value = getValueForMap(key, method, configuration);
        } else {
            value = generateObject(key, type, configuration);
        }

        return value;
    }

    private Configuration loadProp(String... configLocations) {
        if (ArrayUtils.isEmpty(configLocations)) {
            return new PropertiesConfiguration(ConfigurationLoader.getConfigProperties());
        }

        Properties prop = new Properties();
        for (String location : configLocations) {
            location = PropertyPlaceholderHelper.replacePlaceholder(location, true, Env.getAllConfigs());

            Resource resource = this.resourceLoader.getResource(location);
            if (!resource.exists()) {
                continue;
            }

            try {
                PropertiesLoaderUtils.fillProperties(prop, new EncodedResource(resource, "UTF-8"));
            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error(String.format("fill properties with file '%s' error!", resource.getFilename()));
                }
            }
        }

        // 获取覆盖的值
        ParamsHome.getInstance().coveredWithExtResource(prop);
        return new PropertiesConfiguration(prop);
    }

    private boolean isSimpleType(Class<?> type) {
        return SIMPLE_CLASS.contains(type);
    }

    private Object getValueForSimpleType(String key, Class<?> type, Configuration configuration) {
        try {
            if (Boolean.class.equals(type) || boolean.class.equals(type)) {
                return configuration.getBoolean(key);
            } else if (Long.class.equals(type) || long.class.equals(type)) {
                return configuration.getLong(key);
            } else if (Integer.class.equals(type) || int.class.equals(type)) {
                return configuration.getInt(key);
            } else if (String.class.equals(type)) {
                return configuration.getString(key);
            } else if (Double.class.equals(type) || double.class.equals(type)) {
                return configuration.getDouble(key);
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    private Map<String, Object> getValueForMap(String prefix, Method method, Configuration configuration) {
        if (!Map.class.equals(method.getReturnType()) || !String.class.equals(GenericUtils.getGenericParameter0(method))) {
            return null;
        }

        Map<String, Object> map = new HashMap<>(16);
        Class<?> secondGenericClazz = GenericUtils.getGenericParameter(method, 1);
        Set<String> keys = configuration.subset(prefix).getKeys();
        for (String key : keys) {
            String[] temp = StringUtils.split(key, ".");
            if (temp.length < 2) {
                continue;
            }

            String keyInMap = temp[0];
            Object object = generateObject(prefix + "." + keyInMap, secondGenericClazz, configuration);
            if (object != null) {
                map.put(keyInMap, object);
            }
        }

        return map;
    }

    private String getCamelName(String name) {
        Assert.hasText(name);

        StringBuilder result = new StringBuilder();
        if (name != null && name.length() > 0) {
            for (int i = 0; i < name.length(); i++) {
                String tmp = name.substring(i, i + 1);
                //判断截获的字符是否是大写，大写字母的toUpperCase()还是大写的
                if (tmp.equals(tmp.toUpperCase())) {
                    //此字符是大写的
                    result.append("-").append(tmp.toLowerCase());
                } else {
                    result.append(tmp);
                }
            }
        }

        return result.toString();
    }

    public static class Builder<T> {
        private AutoConfigurationBuilder builder = new AutoConfigurationBuilder();
        private Class<T> clazz;
        private ConfigurationProperties annotation;

        public Builder<T> setClazz(Class<T> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder<T> setAnnotation(ConfigurationProperties annotation) {
            this.annotation = annotation;
            return this;
        }

        public T build() {
            // check
            if (clazz == null) {
                throw new IllegalArgumentException("clazz is required!");
            }

            if (annotation == null) {
                throw new IllegalArgumentException("annotation is required!");
            }

            return builder.generateConfigurationBean(clazz, annotation);
        }
    }
}
