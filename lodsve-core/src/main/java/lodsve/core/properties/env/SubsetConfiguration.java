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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * A subset of another configuration. The new Configuration object contains
 * every key from the parent Configuration that starts with prefix. The prefix
 * is removed from the keys in the subset.
 * </p>
 * <p>
 * It is usually not necessary to use this class directly. Instead the
 * {@link Configuration#subset(String)} method should be used, which will return
 * a correctly initialized instance.
 * </p>
 *
 * @author Emmanuel Bourg
 * @version $Id: SubsetConfiguration.java 1210202 2011-12-04 20:30:46Z oheger $
 */
class SubsetConfiguration extends AbstractConfiguration {
    /**
     * The parent configuration.
     */
    protected Configuration parent;

    /**
     * The prefix used to select the properties.
     */
    protected String prefix;

    /**
     * The prefix delimiter
     */
    protected String delimiter;

    /**
     * Create a subset of the specified configuration
     *
     * @param parent The parent configuration
     * @param prefix The prefix used to select the properties
     */
    public SubsetConfiguration(Configuration parent, String prefix) {
        this.parent = parent;
        this.prefix = prefix;
    }

    /**
     * Create a subset of the specified configuration
     *
     * @param parent    The parent configuration
     * @param prefix    The prefix used to select the properties
     * @param delimiter The prefix delimiter
     */
    public SubsetConfiguration(Configuration parent, String prefix,
                               String delimiter) {
        this.parent = parent;
        this.prefix = prefix;
        this.delimiter = delimiter;
    }

    /**
     * Return the key in the parent configuration associated to the specified
     * key in this subset.
     *
     * @param key The key in the subset.
     * @return the key as to be used by the parent
     */
    protected String getParentKey(String key) {
        if ("".equals(key) || key == null) {
            return prefix;
        } else {
            return delimiter == null ? prefix + key : prefix + delimiter + key;
        }
    }

    /**
     * Return the key in the subset configuration associated to the specified
     * key in the parent configuration.
     *
     * @param key The key in the parent configuration.
     * @return the key in the context of this subset configuration
     */
    protected String getChildKey(String key) {
        if (!key.startsWith(prefix)) {
            throw new IllegalArgumentException("The parent key '" + key
                    + "' is not in the subset.");
        } else {
            String modifiedKey = null;
            if (key.length() == prefix.length()) {
                modifiedKey = "";
            } else {
                int i = prefix.length()
                        + (delimiter != null ? delimiter.length() : 0);
                modifiedKey = key.substring(i);
            }

            return modifiedKey;
        }
    }

    /**
     * Return the parent configuration for this subset.
     *
     * @return the parent configuration
     */
    public Configuration getParent() {
        return parent;
    }

    /**
     * Return the prefix used to select the properties in the parent
     * configuration.
     *
     * @return the prefix used by this subset
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set the prefix used to select the properties in the parent configuration.
     *
     * @param prefix the prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Configuration subset(String prefix) {
        return parent.subset(getParentKey(prefix));
    }

    public boolean isEmpty() {
        return getKeys().isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return parent.containsKey(getParentKey(key));
    }

    @Override
    public Object getProperty(String key) {
        return parent.getProperty(getParentKey(key));
    }

    @Override
    public Set<String> getKeys(String prefix) {
        return getSubKeys(parent.getKeys(getParentKey(prefix)));
    }

    @Override
    public Map<String, Object> getStore() {
        return subset(prefix).getStore();
    }

    @Override
    public Set<String> getKeys() {
        return getSubKeys(parent.getKeys(prefix));
    }

    private Set<String> getSubKeys(Set<String> keys) {
        Set<String> subKeys = new HashSet<String>();
        for (String key : keys) {
            subKeys.add(getChildKey(key));
        }

        return subKeys;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Change the behavior of the parent configuration if it supports this
     * feature.
     */
    @Override
    public void setThrowExceptionOnMissing(boolean throwExceptionOnMissing) {
        if (parent instanceof AbstractConfiguration) {
            ((AbstractConfiguration) parent)
                    .setThrowExceptionOnMissing(throwExceptionOnMissing);
        } else {
            super.setThrowExceptionOnMissing(throwExceptionOnMissing);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * The subset inherits this feature from its parent if it supports this
     * feature.
     */
    @Override
    public boolean isThrowExceptionOnMissing() {
        if (parent instanceof AbstractConfiguration) {
            return ((AbstractConfiguration) parent).isThrowExceptionOnMissing();
        } else {
            return super.isThrowExceptionOnMissing();
        }
    }

    /**
     * Returns the list delimiter. This property will be fetched from the parent
     * configuration if supported.
     *
     * @return the list delimiter
     * @since 1.4
     */
    @Override
    public char getListDelimiter() {
        return (parent instanceof AbstractConfiguration) ? ((AbstractConfiguration) parent)
                .getListDelimiter() : super.getListDelimiter();
    }

    /**
     * Sets the list delimiter. If the parent configuration supports this
     * feature, the delimiter will be set at the parent.
     *
     * @param delim the new list delimiter
     * @since 1.4
     */
    @Override
    public void setListDelimiter(char delim) {
        if (parent instanceof AbstractConfiguration) {
            ((AbstractConfiguration) parent).setListDelimiter(delim);
        } else {
            super.setListDelimiter(delim);
        }
    }

    /**
     * Returns a flag whether string properties should be checked for list
     * delimiter characters. This implementation ensures that this flag is kept
     * in sync with the parent configuration if this object supports this
     * feature.
     *
     * @return the delimiter parsing disabled flag
     * @since 1.4
     */
    @Override
    public boolean isDelimiterParsingDisabled() {
        return (parent instanceof AbstractConfiguration) ? ((AbstractConfiguration) parent)
                .isDelimiterParsingDisabled() : super
                .isDelimiterParsingDisabled();
    }

    /**
     * Sets a flag whether list parsing is disabled. This implementation will
     * also set the flag at the parent configuration if this object supports
     * this feature.
     *
     * @param delimiterParsingDisabled the delimiter parsing disabled flag
     * @since 1.4
     */
    @Override
    public void setDelimiterParsingDisabled(boolean delimiterParsingDisabled) {
        if (parent instanceof AbstractConfiguration) {
            ((AbstractConfiguration) parent)
                    .setDelimiterParsingDisabled(delimiterParsingDisabled);
        } else {
            super.setDelimiterParsingDisabled(delimiterParsingDisabled);
        }
    }
}
