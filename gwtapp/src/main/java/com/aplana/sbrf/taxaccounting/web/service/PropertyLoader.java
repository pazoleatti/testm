package com.aplana.sbrf.taxaccounting.web.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Получает значения конфигурационных параметров для модуля
 * @author dloshkarev
 */
public final class PropertyLoader {

    private static final Log LOG = LogFactory.getLog(PropertyLoader.class);
    private static final String PROPERTIES_PATH = "/WEB-INF/classes/module.properties";

    private PropertyLoader() {}

    /**
     * Получает версию текущего ejb-модуля
     * @return версия
     */
    public static boolean isProductionMode() {
        try {
            InputStream stream = PropertyLoader.class.getResourceAsStream(PROPERTIES_PATH);
            if (stream != null) {
                try {
                    Properties props = new Properties();
                    props.load(stream);
                    return props.getProperty("module.isProductionMode").equals("true");
                } finally {
                    stream.close();
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }
}
