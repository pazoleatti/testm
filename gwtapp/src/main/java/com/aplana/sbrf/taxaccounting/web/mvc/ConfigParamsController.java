package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared.GetCommonParameterResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для работы с конфигурационными данными приложения
 */

@RestController
public class ConfigParamsController {

    private SecurityService securityService;

    private ConfigurationService configurationService;

    public ConfigParamsController(SecurityService securityService, ConfigurationService configurationService) {
        this.configurationService = configurationService;
        this.securityService = securityService;
    }

    /**
     * Привязка данных из параметров запроса
     *
     * @param binder спец. DataBinder для привязки
     */
    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(GetCommonParameterResult.class, new RequestParamEditor(GetCommonParameterResult.class));
        binder.registerCustomEditor(Configuration.class, new RequestParamEditor(Configuration.class));

    }

    /**
     * Возвращает общие параметры для пользователя
     * @return общие параметры

     @GetMapping(value = "/rest/getCommonParams")
     public List<Configuration> getCommonParameterResult() {
     securityService.currentUserInfo();
     List<Configuration> result = configurationService.getConfig(securityService.currentUserInfo());
     return result;
     }
     */
}
