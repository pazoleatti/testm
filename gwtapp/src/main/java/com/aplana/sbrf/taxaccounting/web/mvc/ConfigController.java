package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.web.model.ConfigModel;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Properties;

/**
 * Контроллер для работы с конфигурационными данными приложения
 */
@RestController
public class ConfigController {
    private Properties versionInfoProperties;

    public ConfigController(Properties versionInfoProperties) {
        this.versionInfoProperties = versionInfoProperties;
    }

    /**
     * Получения конфигурационных данных приложения
     *
     * @return конфигурационные данные
     */
    @GetMapping(value = "/rest/config")
    public ConfigModel fetchConfig() {
        String gwtMode = PropertyLoader.isProductionMode() ? "" : "?gwt.codesvr=127.0.0.1:9997";

        return new ConfigModel(gwtMode, versionInfoProperties);
    }

}