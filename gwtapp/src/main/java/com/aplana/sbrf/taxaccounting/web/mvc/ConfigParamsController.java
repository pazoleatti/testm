package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//TODO Необходимо сделать ревью и рефакторинг бэкенда и фронтенда по работе с Общими параметрами. https://jira.aplana.com/browse/SBRFNDFL-3174
/**
 * Контроллер для работы с конфигурационными параметрами приложения
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
    @GetMapping(value = "/rest/commonParams/fetchCommonParams")
    public List<Configuration> fetchCommonParams() {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return configurationService.fetchCommonParameter(userInfo);
    }

    /**
     * Редактирование значения общего параметра
     *
     * @param config редактируемый параметр
     */
    @PostMapping(value = "/actions/commonParams/editCommonParams")
    public ActionResult editCommonParams(@RequestParam Configuration config) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return configurationService.update(userInfo, config);
    }

    /**
     * Установка значений общих параметров по умолчанию
     */
    @PostMapping(value = "/actions/commonParams/changeToDefaultCommonParams")
    public void setCommonParamsDefault() {
        TAUserInfo userInfo = securityService.currentUserInfo();
        configurationService.setCommonParamsDefault(userInfo);
    }

}
