package message.mvc.configs.parser;

import message.template.resource.ThymeleafTemplateResource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * mvc配置解析.
 *
 * @author sunhao(sunhao.java@gmail.com)
 * @version V1.0
 * @createTime 2015-1-24 19:32
 */
public class MVCConfigParser implements BeanDefinitionParser {
    private static final String MVC_TEMPLATE_LOCATION = "META-INF/template/mvc.xml";

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        //bean对象注册机
        BeanDefinitionRegistry registry = parserContext.getRegistry();
        dynamicLoadConfigBean(element, registry);

        return null;
    }

    private void dynamicLoadConfigBean(Element element, BeanDefinitionRegistry registry) {
        Map<String, Object> context = new HashMap<String, Object>();

        List<Element> argumentResolvers = DomUtils.getChildElementsByTagName(element, "argument-resolvers");
        List<Element> interceptors = DomUtils.getChildElementsByTagName(element, "interceptors");
        List<Element> converters = DomUtils.getChildElementsByTagName(element, "converters");

        List<String> argumentResolverList = new ArrayList<String>();
        List<String> interceptorList = new ArrayList<String>();
        List<String> convertersList = new ArrayList<String>();


        if (!CollectionUtils.isEmpty(argumentResolvers)) {
            List<Element> argumentResolverBeans = DomUtils.getChildElementsByTagName(argumentResolvers.get(0), "bean");
            for (Element ele : argumentResolverBeans) {
                argumentResolverList.add(ele.getAttribute("class"));
            }
        }

        if (!CollectionUtils.isEmpty(interceptors)) {
            List<Element> interceptorBeans = DomUtils.getChildElementsByTagName(interceptors.get(0), "bean");
            for (Element ele : interceptorBeans) {
                interceptorList.add(ele.getAttribute("class"));
            }
        }

        if (!CollectionUtils.isEmpty(argumentResolvers)) {
            List<Element> converterBeans = DomUtils.getChildElementsByTagName(converters.get(0), "bean");
            for (Element ele : converterBeans) {
                convertersList.add(ele.getAttribute("class"));
            }
        }

        context.put("argumentResolvers", argumentResolverList);
        context.put("interceptors", interceptorList);
        context.put("converters", convertersList);

        BeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(registry);
        Resource resource = new ThymeleafTemplateResource(MVC_TEMPLATE_LOCATION, context, "xml");
        beanDefinitionReader.loadBeanDefinitions(resource);
    }
}
