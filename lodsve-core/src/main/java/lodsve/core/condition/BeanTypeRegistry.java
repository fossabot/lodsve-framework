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

package lodsve.core.condition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A registry of the bean types that are contained in a
 * {@link DefaultListableBeanFactory}. Provides similar functionality to
 * {@link ListableBeanFactory#getBeanNamesForType(Class, boolean, boolean)} but is
 * optimized for use by {@link OnBeanCondition} based on the following assumptions:
 * <ul>
 * <li>Bean definitions will not change type.</li>
 * <li>Beans definitions will not be removed.</li>
 * <li>Beans will not be created in parallel.</li>
 * </ul>
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 1.2.0
 */
final class BeanTypeRegistry implements SmartInitializingSingleton {

    static final String FACTORY_BEAN_OBJECT_TYPE = "factoryBeanObjectType";
    private static final Log logger = LogFactory.getLog(BeanTypeRegistry.class);
    private static final String BEAN_NAME = BeanTypeRegistry.class.getName();

    private final DefaultListableBeanFactory beanFactory;

    private final Map<String, Class<?>> beanTypes = new HashMap<String, Class<?>>();

    private int lastBeanDefinitionCount = 0;

    private BeanTypeRegistry(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * Factory method to get the {@link BeanTypeRegistry} for a given {@link BeanFactory}.
     *
     * @param beanFactory the source bean factory
     * @return the {@link BeanTypeRegistry} for the given bean factory
     */
    static BeanTypeRegistry get(ListableBeanFactory beanFactory) {
        Assert.isInstanceOf(DefaultListableBeanFactory.class, beanFactory);
        DefaultListableBeanFactory listableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        Assert.isTrue(listableBeanFactory.isAllowEagerClassLoading(),
                "Bean factory must allow eager class loading");
        if (!listableBeanFactory.containsLocalBean(BEAN_NAME)) {
            BeanDefinition bd = new RootBeanDefinition(BeanTypeRegistry.class);
            bd.getConstructorArgumentValues().addIndexedArgumentValue(0, beanFactory);
            listableBeanFactory.registerBeanDefinition(BEAN_NAME, bd);

        }
        return listableBeanFactory.getBean(BEAN_NAME, BeanTypeRegistry.class);
    }

    /**
     * Return the names of beans matching the given type (including subclasses), judging
     * from either bean definitions or the value of {@link FactoryBean#getObjectType()} in
     * the case of {@link FactoryBean FactoryBeans}. Will include singletons but will not
     * cause early bean initialization.
     *
     * @param type the class or interface to match (must not be {@code null})
     * @return the names of beans (or objects created by FactoryBeans) matching the given
     * object type (including subclasses), or an empty set if none
     */
    Set<String> getNamesForType(Class<?> type) {
        updateTypesIfNecessary();
        Set<String> matches = new LinkedHashSet<String>();
        for (Map.Entry<String, Class<?>> entry : this.beanTypes.entrySet()) {
            if (entry.getValue() != null && type.isAssignableFrom(entry.getValue())) {
                matches.add(entry.getKey());
            }
        }
        return matches;
    }

    /**
     * Returns the names of beans annotated with the given {@code annotation}, judging
     * from either bean definitions or the value of {@link FactoryBean#getObjectType()} in
     * the case of {@link FactoryBean FactoryBeans}. Will include singletons but will not
     * cause early bean initialization.
     *
     * @param annotation the annotation to match (must not be {@code null})
     * @return the names of beans (or objects created by FactoryBeans) annoated with the
     * given annotation, or an empty set if none
     */
    Set<String> getNamesForAnnotation(Class<? extends Annotation> annotation) {
        updateTypesIfNecessary();
        Set<String> matches = new LinkedHashSet<String>();
        for (Map.Entry<String, Class<?>> entry : this.beanTypes.entrySet()) {
            if (entry.getValue() != null && AnnotationUtils
                    .findAnnotation(entry.getValue(), annotation) != null) {
                matches.add(entry.getKey());
            }
        }
        return matches;
    }

    @Override
    public void afterSingletonsInstantiated() {
        // We're done at this point, free up some memory
        this.beanTypes.clear();
        this.lastBeanDefinitionCount = 0;
    }

    private void addBeanType(String name) {
        if (this.beanFactory.containsSingleton(name)) {
            this.beanTypes.put(name, this.beanFactory.getType(name));
        } else if (!this.beanFactory.isAlias(name)) {
            addBeanTypeForNonAliasDefinition(name);
        }
    }

    private void addBeanTypeForNonAliasDefinition(String name) {
        try {
            String factoryName = BeanFactory.FACTORY_BEAN_PREFIX + name;
            RootBeanDefinition beanDefinition = (RootBeanDefinition) this.beanFactory
                    .getMergedBeanDefinition(name);
            if (!beanDefinition.isAbstract()
                    && !requiresEagerInit(beanDefinition.getFactoryBeanName())) {
                if (this.beanFactory.isFactoryBean(factoryName)) {
                    Class<?> factoryBeanGeneric = getFactoryBeanGeneric(this.beanFactory,
                            beanDefinition, name);
                    this.beanTypes.put(name, factoryBeanGeneric);
                    this.beanTypes.put(factoryName,
                            this.beanFactory.getType(factoryName));
                } else {
                    this.beanTypes.put(name, this.beanFactory.getType(name));
                }
            }
        } catch (CannotLoadBeanClassException ex) {
            // Probably contains a placeholder
            logIgnoredError("bean class loading failure for bean", name, ex);
        } catch (BeanDefinitionStoreException ex) {
            // Probably contains a placeholder
            logIgnoredError("unresolvable metadata in bean definition", name, ex);
        }
    }

    private void logIgnoredError(String message, String name, Exception ex) {
        if (BeanTypeRegistry.logger.isDebugEnabled()) {
            BeanTypeRegistry.logger.debug("Ignoring " + message + " '" + name + "'", ex);
        }
    }

    private boolean requiresEagerInit(String factoryBeanName) {
        return (factoryBeanName != null && this.beanFactory.isFactoryBean(factoryBeanName)
                && !this.beanFactory.containsSingleton(factoryBeanName));
    }

    private void updateTypesIfNecessary() {
        if (this.lastBeanDefinitionCount != this.beanFactory.getBeanDefinitionCount()) {
            Iterator<String> names = this.beanFactory.getBeanNamesIterator();
            while (names.hasNext()) {
                String name = names.next();
                if (!this.beanTypes.containsKey(name)) {
                    addBeanType(name);
                }
            }
            this.lastBeanDefinitionCount = this.beanFactory.getBeanDefinitionCount();
        }
    }

    /**
     * Attempt to guess the type that a {@link FactoryBean} will return based on the
     * generics in its method signature.
     *
     * @param beanFactory the source bean factory
     * @param definition  the bean definition
     * @param name        the name of the bean
     * @return the generic type of the {@link FactoryBean} or {@code null}
     */
    private Class<?> getFactoryBeanGeneric(ConfigurableListableBeanFactory beanFactory,
                                           BeanDefinition definition, String name) {
        try {
            return doGetFactoryBeanGeneric(beanFactory, definition, name);
        } catch (Exception ex) {
            return null;
        }
    }

    private Class<?> doGetFactoryBeanGeneric(ConfigurableListableBeanFactory beanFactory,
                                             BeanDefinition definition, String name)
            throws Exception, ClassNotFoundException, LinkageError {
        if (StringUtils.hasLength(definition.getFactoryBeanName())
                && StringUtils.hasLength(definition.getFactoryMethodName())) {
            return getConfigurationClassFactoryBeanGeneric(beanFactory, definition, name);
        }
        if (StringUtils.hasLength(definition.getBeanClassName())) {
            return getDirectFactoryBeanGeneric(beanFactory, definition, name);
        }
        return null;
    }

    private Class<?> getConfigurationClassFactoryBeanGeneric(
            ConfigurableListableBeanFactory beanFactory, BeanDefinition definition,
            String name) throws Exception {
        Method method = getFactoryMethod(beanFactory, definition);
        Class<?> generic = ResolvableType.forMethodReturnType(method)
                .as(FactoryBean.class).resolveGeneric();
        if ((generic == null || generic.equals(Object.class))
                && definition.hasAttribute(FACTORY_BEAN_OBJECT_TYPE)) {
            generic = getTypeFromAttribute(
                    definition.getAttribute(FACTORY_BEAN_OBJECT_TYPE));
        }
        return generic;
    }

    private Method getFactoryMethod(ConfigurableListableBeanFactory beanFactory,
                                    BeanDefinition definition) throws Exception {
        if (definition instanceof AnnotatedBeanDefinition) {
            MethodMetadata factoryMethodMetadata = ((AnnotatedBeanDefinition) definition)
                    .getFactoryMethodMetadata();
            if (factoryMethodMetadata instanceof StandardMethodMetadata) {
                return ((StandardMethodMetadata) factoryMethodMetadata)
                        .getIntrospectedMethod();
            }
        }
        BeanDefinition factoryDefinition = beanFactory
                .getBeanDefinition(definition.getFactoryBeanName());
        Class<?> factoryClass = ClassUtils.forName(factoryDefinition.getBeanClassName(),
                beanFactory.getBeanClassLoader());
        return getFactoryMethod(definition, factoryClass);
    }

    private Method getFactoryMethod(BeanDefinition definition, Class<?> factoryClass) {
        Method uniqueMethod = null;
        for (Method candidate : getCandidateFactoryMethods(definition, factoryClass)) {
            if (candidate.getName().equals(definition.getFactoryMethodName())) {
                if (uniqueMethod == null) {
                    uniqueMethod = candidate;
                } else if (!hasMatchingParameterTypes(candidate, uniqueMethod)) {
                    return null;
                }
            }
        }
        return uniqueMethod;
    }

    private Method[] getCandidateFactoryMethods(BeanDefinition definition,
                                                Class<?> factoryClass) {
        return shouldConsiderNonPublicMethods(definition)
                ? ReflectionUtils.getAllDeclaredMethods(factoryClass)
                : factoryClass.getMethods();
    }

    private boolean shouldConsiderNonPublicMethods(BeanDefinition definition) {
        return (definition instanceof AbstractBeanDefinition)
                && ((AbstractBeanDefinition) definition).isNonPublicAccessAllowed();
    }

    private boolean hasMatchingParameterTypes(Method candidate, Method current) {
        return Arrays.equals(candidate.getParameterTypes(), current.getParameterTypes());
    }

    private Class<?> getDirectFactoryBeanGeneric(
            ConfigurableListableBeanFactory beanFactory, BeanDefinition definition,
            String name) throws ClassNotFoundException, LinkageError {
        Class<?> factoryBeanClass = ClassUtils.forName(definition.getBeanClassName(),
                beanFactory.getBeanClassLoader());
        Class<?> generic = ResolvableType.forClass(factoryBeanClass).as(FactoryBean.class)
                .resolveGeneric();
        if ((generic == null || generic.equals(Object.class))
                && definition.hasAttribute(FACTORY_BEAN_OBJECT_TYPE)) {
            generic = getTypeFromAttribute(
                    definition.getAttribute(FACTORY_BEAN_OBJECT_TYPE));
        }
        return generic;
    }

    private Class<?> getTypeFromAttribute(Object attribute)
            throws ClassNotFoundException, LinkageError {
        if (attribute instanceof Class<?>) {
            return (Class<?>) attribute;
        }
        if (attribute instanceof String) {
            return ClassUtils.forName((String) attribute, null);
        }
        return null;
    }

}
