package com.aplana.sbrf.taxaccounting.web.model;

import com.aplana.sbrf.taxaccounting.core.api.ServerInfo;

import java.util.Properties;

/**
 * Модель данных конфигурации приложения
 */
public class ConfigModel {
    private String gwtMode;
    private Properties versionInfoProperties;
    private ServerInfo serverInfo;

    public ConfigModel(String gwtMode, Properties versionInfoProperties, ServerInfo serverInfo) {
        this.gwtMode = gwtMode;
        this.versionInfoProperties = versionInfoProperties;
        this.serverInfo = serverInfo;
    }

    public String getGwtMode() {
        return gwtMode;
    }

    public Properties getVersionInfoProperties() {
        return versionInfoProperties;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }
}