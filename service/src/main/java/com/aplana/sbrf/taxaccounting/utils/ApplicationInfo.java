package com.aplana.sbrf.taxaccounting.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Получает значения конфигурационных параметров для модуля
 * @author dloshkarev
 */
@Component
public class ApplicationInfo {

    @Autowired
    @Qualifier("versionInfoProperties")
    private Properties versionInfoProperties;

    public boolean isProductionMode() {
        if (versionInfoProperties.getProperty("productionMode") == null) {
            throw new IllegalArgumentException("Property 'productionMode' must be set up!");
        }
        return versionInfoProperties.getProperty("productionMode").equals("true");
    }

    public String getVersion() {
        if (versionInfoProperties.getProperty("version") == null) {
            return "?";
        }
        return versionInfoProperties.getProperty("version");
    }

    public String getRevision() {
        if (versionInfoProperties.getProperty("revision") == null) {
            return "?";
        }
        return versionInfoProperties.getProperty("revision");
    }
}
