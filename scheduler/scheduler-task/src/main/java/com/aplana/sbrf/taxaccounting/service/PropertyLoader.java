package com.aplana.sbrf.taxaccounting.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Получает значения конфигурационных параметров для модуля
 * @author dloshkarev
 */
public class PropertyLoader {
    private static final Log LOG = LogFactory.getLog(PropertyLoader.class);
    private static final String PROPERTIES_PATH = "/module.properties";

    /**
     * Получает версию текущего ejb-модуля
     * @return версия
     */
    public static String getVersion() {
        try {
            InputStream stream = PropertyLoader.class.getResourceAsStream(PROPERTIES_PATH);
            if (stream != null) {
                try {
                    Properties props = new Properties();
                    props.load(stream);
                    return props.getProperty("module.version");
                } finally {
                    stream.close();
                }
            }
        } catch (Throwable e) {
            LOG.error(e);
        }
        return "";
    }
}
