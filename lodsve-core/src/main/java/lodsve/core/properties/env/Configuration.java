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

package lodsve.core.properties.env;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * <p>
 * The main Configuration interface.
 * </p>
 * <p>
 * This interface allows accessing and manipulating a configuration object. The
 * major part of the methods defined in this interface deals with accessing
 * properties of various data types. There is a generic {@code getProperty()}
 * method, which returns the value of the queried property in its raw data type.
 * Other getter methods try to convert this raw data type into a specific data
 * type. If this fails, a {@code ConversionException} will be thrown.
 * </p>
 * <p>
 * For most of the property getter methods an overloaded version exists that
 * allows to specify a default value, which will be returned if the queried
 * property cannot be found in the configuration. The behavior of the methods
 * that do not take a default value in case of a missing property is not defined
 * by this interface and depends on a concrete implementation. E.g. the
 * {@link AbstractConfiguration} class, which is the base class of most
 * configuration implementations provided by this package, per default returns
 * <b>null</b> if a property is not found, but provides the
 * {@link AbstractConfiguration#setThrowExceptionOnMissing(boolean)
 * setThrowExceptionOnMissing()} method, with which it can be configured to
 * throw a {@code NoSuchElementException} exception in that case. (Note that
 * getter methods for primitive types in {@code AbstractConfiguration} always
 * throw an exception for missing properties because there is no way of
 * overloading the return value.)
 * </p>
 * <p>
 * With the {@code addProperty()} and {@code setProperty()} methods new
 * properties can be added to a configuration or the values of properties can be
 * changed. With {@code clearProperty()} a property can be removed. Other
 * methods allow to iterate over the contained properties or to create a subset
 * configuration.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id: Configuration.java 1534064 2013-10-21 08:44:33Z henning $
 */
public interface Configuration {

    /**
     * Return a decorator Configuration containing every key from the current
     * Configuration that starts with the specified prefix. The prefix is
     * removed from the keys in the subset. For example, if the configuration
     * contains the following properties:
     * <p>
     * <pre>
     *    prefix.number = 1
     *    prefix.string = Apache
     *    prefixed.foo = bar
     *    prefix = Jakarta</pre>
     * <p>
     * the Configuration returned by {@code subset("prefix")} will contain
     * the properties:
     * <p>
     * <pre>
     *    number = 1
     *    string = Apache
     *    = Jakarta</pre>
     * <p>
     * (The key for the value "Jakarta" is an empty string)
     * <p>
     * Since the subset is a decorator and not a modified copy of the initial
     * Configuration, any change made to the subset is available to the
     * Configuration, and reciprocally.
     *
     * @param prefix The prefix used to select the properties.
     * @return a subset configuration
     * @see SubsetConfiguration
     */
    Configuration subset(String prefix);

    /**
     * Check if the configuration contains the specified key.
     *
     * @param key the key whose presence in this configuration is to be tested
     * @return {@code true} if the configuration contains a value for this key,
     * {@code false} otherwise
     */
    boolean containsKey(String key);

    /**
     * Gets a property from the configuration. This is the most basic get method
     * for retrieving values of properties. In a typical implementation of the
     * {@code Configuration} interface the other get methods (that return
     * specific data types) will internally make use of this method. On this
     * level variable substitution is not yet performed. The returned object is
     * an internal representation of the property value for the passed in key.
     * It is owned by the {@code Configuration} object. So a caller should not
     * modify this object. It cannot be guaranteed that this object will stay
     * constant over time (i.e. further update operations on the configuration
     * may change its internal state).
     *
     * @param key property to retrieve
     * @return the value to which this configuration maps the specified key, or
     * null if the configuration contains no mapping for this key.
     */
    Object getProperty(String key);

    /**
     * Get the set of the keys contained in the configuration that match the
     * specified prefix. For instance, if the configuration contains the
     * following keys:<br>
     * {@code db.user, db.pwd, db.url, window.xpos, window.ypos},<br>
     * an invocation of {@code getKeys("db");}<br>
     * will return the keys below:<br>
     * {@code db.user, db.pwd, db.url}.<br>
     * Note that the prefix itself is included in the result set if there is a
     * matching key. The exact behavior - how the prefix is actually
     * interpreted - depends on a concrete implementation.
     *
     * @param prefix The prefix to test against.
     * @return An Iterator of keys that match the prefix.
     * @see #getKeys()
     */
    Set<String> getKeys(String prefix);

    /**
     * Get the set of the keys contained in the configuration. The returned
     * iterator can be used to obtain all defined keys. Note that the exact
     * behavior of the iterator's {@code remove()} method is specific to
     * a concrete implementation. It <em>may</em> remove the corresponding
     * property from the configuration, but this is not guaranteed. In any case
     * it is no replacement for calling
     * {@link #clearProperty(String)} for this property. So it is
     * highly recommended to avoid using the iterator's {@code remove()}
     * method.
     *
     * @return An Iterator.
     */
    Set<String> getKeys();

    /**
     * Get a list of properties associated with the given configuration key.
     * This method expects the given key to have an arbitrary number of String
     * values, each of which is of the form {code key=value}. These strings are
     * split at the equals sign, and the key parts will become keys of the
     * returned {@code Properties} object, the value parts become values.
     *
     * @param key The configuration key.
     * @return The associated properties if key is found.
     * @throws ConversionException      is thrown if the key maps to an object that is not a
     *                                  String/List.
     * @throws IllegalArgumentException if one of the tokens is malformed (does not contain an equals
     *                                  sign).
     */
    Properties getProperties(String key);

    /**
     * Get a boolean associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated boolean.
     * @throws ConversionException is thrown if the key maps to an object that is not a Boolean.
     */
    boolean getBoolean(String key);

    /**
     * Get a boolean associated with the given configuration key. If the key
     * doesn't map to an existing object, the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated boolean.
     * @throws ConversionException is thrown if the key maps to an object that is not a Boolean.
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Get a {@link Boolean} associated with the given configuration key.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated boolean if key is found and has valid format,
     * default value otherwise.
     * @throws ConversionException is thrown if the key maps to an object that is not a Boolean.
     */
    Boolean getBoolean(String key, Boolean defaultValue);

    /**
     * Get a byte associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated byte.
     * @throws ConversionException is thrown if the key maps to an object that is not a Byte.
     */
    byte getByte(String key);

    /**
     * Get a byte associated with the given configuration key. If the key
     * doesn't map to an existing object, the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated byte.
     * @throws ConversionException is thrown if the key maps to an object that is not a Byte.
     */
    byte getByte(String key, byte defaultValue);

    /**
     * Get a {@link Byte} associated with the given configuration key.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated byte if key is found and has valid format, default
     * value otherwise.
     * @throws ConversionException is thrown if the key maps to an object that is not a Byte.
     */
    Byte getByte(String key, Byte defaultValue);

    /**
     * Get a double associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated double.
     * @throws ConversionException is thrown if the key maps to an object that is not a Double.
     */
    double getDouble(String key);

    /**
     * Get a double associated with the given configuration key. If the key
     * doesn't map to an existing object, the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated double.
     * @throws ConversionException is thrown if the key maps to an object that is not a Double.
     */
    double getDouble(String key, double defaultValue);

    /**
     * Get a {@link Double} associated with the given configuration key.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated double if key is found and has valid format,
     * default value otherwise.
     * @throws ConversionException is thrown if the key maps to an object that is not a Double.
     */
    Double getDouble(String key, Double defaultValue);

    /**
     * Get a float associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated float.
     * @throws ConversionException is thrown if the key maps to an object that is not a Float.
     */
    float getFloat(String key);

    /**
     * Get a float associated with the given configuration key. If the key
     * doesn't map to an existing object, the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated float.
     * @throws ConversionException is thrown if the key maps to an object that is not a Float.
     */
    float getFloat(String key, float defaultValue);

    /**
     * Get a {@link Float} associated with the given configuration key. If the
     * key doesn't map to an existing object, the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated float if key is found and has valid format,
     * default value otherwise.
     * @throws ConversionException is thrown if the key maps to an object that is not a Float.
     */
    Float getFloat(String key, Float defaultValue);

    /**
     * Get a int associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated int.
     * @throws ConversionException is thrown if the key maps to an object that is not a Integer.
     */
    int getInt(String key);

    /**
     * Get a int associated with the given configuration key. If the key doesn't
     * map to an existing object, the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated int.
     * @throws ConversionException is thrown if the key maps to an object that is not a Integer.
     */
    int getInt(String key, int defaultValue);

    /**
     * Get an {@link Integer} associated with the given configuration key. If
     * the key doesn't map to an existing object, the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated int if key is found and has valid format, default
     * value otherwise.
     * @throws ConversionException is thrown if the key maps to an object that is not a Integer.
     */
    Integer getInteger(String key, Integer defaultValue);

    /**
     * Get a long associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated long.
     * @throws ConversionException is thrown if the key maps to an object that is not a Long.
     */
    long getLong(String key);

    /**
     * Get a long associated with the given configuration key. If the key
     * doesn't map to an existing object, the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated long.
     * @throws ConversionException is thrown if the key maps to an object that is not a Long.
     */
    long getLong(String key, long defaultValue);

    /**
     * Get a {@link Long} associated with the given configuration key. If the
     * key doesn't map to an existing object, the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated long if key is found and has valid format, default
     * value otherwise.
     * @throws ConversionException is thrown if the key maps to an object that is not a Long.
     */
    Long getLong(String key, Long defaultValue);

    /**
     * Get a short associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated short.
     * @throws ConversionException is thrown if the key maps to an object that is not a Short.
     */
    short getShort(String key);

    /**
     * Get a short associated with the given configuration key.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated short.
     * @throws ConversionException is thrown if the key maps to an object that is not a Short.
     */
    short getShort(String key, short defaultValue);

    /**
     * Get a {@link Short} associated with the given configuration key. If the
     * key doesn't map to an existing object, the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated short if key is found and has valid format,
     * default value otherwise.
     * @throws ConversionException is thrown if the key maps to an object that is not a Short.
     */
    Short getShort(String key, Short defaultValue);

    /**
     * Get a {@link java.math.BigDecimal} associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated BigDecimal if key is found and has valid format
     */
    BigDecimal getBigDecimal(String key);

    /**
     * Get a {@link java.math.BigDecimal} associated with the given configuration key. If
     * the key doesn't map to an existing object, the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated BigDecimal if key is found and has valid format,
     * default value otherwise.
     */
    BigDecimal getBigDecimal(String key, BigDecimal defaultValue);

    /**
     * Get a {@link java.math.BigInteger} associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated BigInteger if key is found and has valid format
     */
    BigInteger getBigInteger(String key);

    /**
     * Get a {@link java.math.BigInteger} associated with the given configuration key. If
     * the key doesn't map to an existing object, the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated BigInteger if key is found and has valid format,
     * default value otherwise.
     */
    BigInteger getBigInteger(String key, BigInteger defaultValue);

    /**
     * Get a string associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated string.
     * @throws ConversionException is thrown if the key maps to an object that is not a String.
     */
    String getString(String key);

    /**
     * Get a string associated with the given configuration key. If the key
     * doesn't map to an existing object, the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated string if key is found and has valid format,
     * default value otherwise.
     * @throws ConversionException is thrown if the key maps to an object that is not a String.
     */
    String getString(String key, String defaultValue);

    /**
     * Get an array of strings associated with the given configuration key. If
     * the key doesn't map to an existing object an empty array is returned
     *
     * @param key The configuration key.
     * @return The associated string array if key is found.
     * @throws ConversionException is thrown if the key maps to an object that is not a
     *                             String/List of Strings.
     */
    String[] getStringArray(String key);

    /**
     * Get a List of strings associated with the given configuration key. If the
     * key doesn't map to an existing object an empty List is returned.
     *
     * @param key The configuration key.
     * @return The associated List.
     * @throws ConversionException is thrown if the key maps to an object that is not a List.
     */
    List<Object> getList(String key);

    /**
     * Get a List of strings associated with the given configuration key. If the
     * key doesn't map to an existing object, the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated List of strings.
     * @throws ConversionException is thrown if the key maps to an object that is not a List.
     */
    List<Object> getList(String key, List<?> defaultValue);

    Map<String, Object> getStore();
}
