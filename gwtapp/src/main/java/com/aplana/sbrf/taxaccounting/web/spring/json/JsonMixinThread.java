package com.aplana.sbrf.taxaccounting.web.spring.json;

import org.apache.commons.lang3.Validate;

/**
 * Storage for thread-local JSON mixins.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public abstract class JsonMixinThread {
    private static final ThreadLocal<JsonMixins.JsonMixin[]> threadLocal = new ThreadLocal<>();

    /**
     * Sets the specified {@link JsonMixins.JsonMixin} annotations for the current thread.
     *
     * @param mixins the JSON mixins
     */
    public static void setMixins(JsonMixins.JsonMixin[] mixins) {
        Validate.noNullElements(mixins);
        threadLocal.set(mixins);
    }

    /**
     * Returns JSON mixins for the current thread.
     *
     * @return the current thread's JSON mixins
     */
    public static JsonMixins.JsonMixin[] getMixins() {
        return threadLocal.get();
    }

    /**
     * Removes JSON mixins for the current thread.
     */
    public static void remove() {
        threadLocal.remove();
    }
}
