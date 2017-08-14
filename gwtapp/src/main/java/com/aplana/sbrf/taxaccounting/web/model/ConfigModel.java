package com.aplana.sbrf.taxaccounting.web.model;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

import java.util.Properties;

/**
 * Модель данных конфигурации приложения
 */
public class ConfigModel {
    private String gwtMode;
    private Properties versionInfoProperties;

    public ConfigModel(String gwtMode, Properties versionInfoProperties) {
        this.gwtMode = gwtMode;
        this.versionInfoProperties = versionInfoProperties;
    }

    public String getGwtMode() {
        return gwtMode;
    }

    public Properties getVersionInfoProperties() {
        return versionInfoProperties;
    }
}