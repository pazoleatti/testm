package com.aplana.sbrf.taxaccounting.web.spring.json;

public abstract class JsonFilterDescriptionThread {
    private static final ThreadLocal<JsonFilterDescription> threadLocal = new ThreadLocal<JsonFilterDescription>();

    public static void setFilterDescriptions(JsonFilterDescription filterDescription) {
        threadLocal.set(filterDescription);
    }

    public static JsonFilterDescription getFilterDescriptions() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }
}
