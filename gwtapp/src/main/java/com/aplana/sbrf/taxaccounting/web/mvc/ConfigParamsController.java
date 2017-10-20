package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Контроллер для работы с конфигурационными данными приложения
 */

@RestController
public class ConfigParamsController {

    /**
     * Привязка данных из параметров запроса
     *
     * @param binder спец. DataBinder для привязки
     */
    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
        binder.registerCustomEditor(Configuration.class, new RequestParamEditor(Configuration.class));


    }

    private ConfigurationService configurationService;

    private SecurityService securityService;

    public ConfigParamsController(SecurityService securityService, ConfigurationService configurationService) {
        this.securityService = securityService;
        this.configurationService = configurationService;
    }

    /**
     * Возвращает список общих параметров
     */
    @GetMapping(value = "/rest/getCommonParams")
    public List<Configuration> getCommonParams(HttpServletResponse response) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return configurationService.getCommonParametr(userInfo);
    }

    /**
     * Редактирование значения общего параметра
     *
     * @param config редактируемый параметр
     */
    @PostMapping(value = "/actions/redactCommonParams")
    public void redactCommonParams(@RequestParam Configuration config) {
        configurationService.update(config);
    }

    /**
     * Установка значений общих параметров по умолчанию
     */
    @PostMapping(value = "/actions/defaultCommonParams")
    public void setCommonParamsDefault() {
        configurationService.setCommonParamsDefault();
    }

}
