package com.aplana.sbrf.taxaccounting.web.model;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

import java.util.Properties;

/**
 * Модель данных конфигурации приложения
 */
public class ConfigModel {
    private String gwtMode;
    private TAUserInfo taUserInfo;
    private Properties versionInfoProperties;
    private String department;

    public ConfigModel(String gwtMode, TAUserInfo taUserInfo, Properties versionInfoProperties, String department) {
        this.gwtMode = gwtMode;
        this.taUserInfo = taUserInfo;
        this.versionInfoProperties = versionInfoProperties;
        this.department = department;
    }

    public String getGwtMode() {
        return gwtMode;
    }

    public TAUserInfo getTaUserInfo() {
        return taUserInfo;
    }

    public Properties getVersionInfoProperties() {
        return versionInfoProperties;
    }

    public String getDepartment() {
        return department;
    }
}