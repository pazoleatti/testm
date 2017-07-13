package com.aplana.sbrf.taxaccounting.web.spring;

import org.springframework.http.CacheControl;

/**
 * Фабрика для создания экземпляров CacheControl
 *
 * @author pmakarov
 * @since 13.01.2016
 */
public class CacheControlFactory {

    /**
     * Создает Cache-Control:no-store, must-revalidate
     */
    public static CacheControl buildDefaultCacheControl() {
        return CacheControl.noStore().mustRevalidate();
    }
}
