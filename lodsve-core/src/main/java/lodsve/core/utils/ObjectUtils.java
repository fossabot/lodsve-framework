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

package lodsve.core.utils;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.*;

/**
 * object util class
 *
 * @author <a href="mailto:sunhao.java@gmail.com">sunhao(sunhao.java@gmail.com)</a>
 * @date 2012-6-26 上午09:44:13
 */
public class ObjectUtils extends org.apache.commons.lang3.ObjectUtils {

    private ObjectUtils() {
        super();
    }

    /**
     * 判断是否为空
     *
     * @param obj
     * @return
     */
    public static boolean isEmpty(Object obj) {
        return obj == null;
    }

    /**
     * 判断是否为非空
     *
     * @param obj
     * @return
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 获取object的class
     *
     * @param obj
     * @return
     */
    public static Class<?> getType(Object obj) {
        return isEmpty(obj) ? null : obj.getClass();
    }

    /**
     * 第一个object数组是否包含第二个object数组
     *
     * @param obj1 包含的数组           为空返回false
     * @param obj2 被包含的数组          为空返回false
     * @return
     */
    public static boolean contain(Object[] obj1, Object[] obj2) {
        if (obj1 == null || obj1.length < 1) {
            return false;
        }
        if (obj2 == null || obj2.length < 1) {
            return false;
        }
        List<Object> obj1List = Arrays.asList(obj1);
        List<Object> obj2List = Arrays.asList(obj2);

        return CollectionUtils.containsAny(obj1List, obj2List);
    }

    /**
     * 判断srcObj是否包含在destArray中
     *
     * @param destArray 目标数组        为空返回false
     * @param srcObj    源对象          为空返回false
     * @return
     */
    public static boolean contains(Object[] destArray, Object srcObj) {
        return contain(destArray, new Object[]{srcObj});
    }

    /**
     * object to map
     *
     * @param obj object
     * @return map, key is field, value is object's value
     */
    public static Map<String, Object> objectToMap(Object obj) {
        try {
            if (isEmpty(obj)) {
                return Collections.emptyMap();
            }

            Field[] fields = getFields(obj.getClass());
            Map<String, Object> map = new HashMap<>(fields.length);
            for (Field f : fields) {
                Object value = getFieldValue(obj, f.getName());
                map.put(f.getName(), value);
            }

            return map;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    /**
     * 获取对象中的所有字段
     * getFields()与getDeclaredFields()区别:
     * getFields()只能访问类中声明为公有的字段,私有的字段它无法访问.
     * getDeclaredFields()能访问类中所有的字段,与public,private,protect无关，但是不包括父类的申明字段。
     *
     * @param clazz class
     * @return 所有字段
     */
    public static Field[] getFields(Class<?> clazz) {
        if (isEmpty(clazz)) {
            return new Field[0];
        }

        return clazz.getDeclaredFields();
    }

    /**
     * 根据字段名得到实例的字段值
     *
     * @param object    实例对象
     * @param fieldName 字段名称
     * @return 实例字段的值，如果没找到该字段则返回null
     */
    public static Object getFieldValue(Object object, String fieldName) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(object);
        return beanWrapper.getPropertyValue(fieldName);
    }

    /**
     * 合并obj2和obj2的值，并返回，以前一个对象为准
     *
     * @param first  第一个对象
     * @param second 第二个对象
     */
    public static Object mergerObject(Object first, Object second) throws IllegalAccessException {
        Assert.notNull(first);
        Assert.notNull(second);
        Assert.isTrue(first.getClass().equals(second.getClass()));

        Class<?> clazz = first.getClass();
        Object result = BeanUtils.instantiate(clazz);
        Field[] fields = clazz.getDeclaredFields();

        for (Field f : fields) {
            //设置字段可读
            f.setAccessible(true);

            Object value1 = f.get(first);
            Object value2 = f.get(second);

            Object value = value1;
            if (value == null) {
                value = value2;
            }

            f.set(result, value);
        }

        return result;
    }
}
