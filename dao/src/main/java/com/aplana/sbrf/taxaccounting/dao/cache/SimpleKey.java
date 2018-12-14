package com.aplana.sbrf.taxaccounting.dao.cache;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Простой ключ используемый в {@link CacheKeyGenerator}.
 */
@SuppressWarnings("serial")
public class SimpleKey implements Serializable {

    private final Object[] params;
    private final int hashCode;

    public SimpleKey(String className, String methodName) {
        this.params = new Object[]{className, methodName};
        this.hashCode = Arrays.deepHashCode(this.params);
    }

    public SimpleKey(String className, String methodName, Object... elements) {
        Assert.notNull(elements, "Elements must not be null");
        this.params = new Object[elements.length + 2];
        this.params[0] = className;
        this.params[1] = methodName;
        System.arraycopy(elements, 0, this.params, 2, elements.length);
        this.hashCode = Arrays.deepHashCode(this.params);
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj || (obj instanceof SimpleKey
                && Arrays.deepEquals(this.params, ((SimpleKey) obj).params)));
    }

    @Override
    public final int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + StringUtils.arrayToCommaDelimitedString(this.params) + "]";
    }
}
