package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
    @GetMapping(value = "/rest/fetchCommonParams")
    public List<Configuration> fetchCommonParams(HttpServletResponse response) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return configurationService.getCommonParametr(userInfo);
    }

    /**
     * Редактирование значения общего параметра
     *
     * @param config редактируемый параметр
     */
    @PostMapping(value = "/actions/uploadCommonParams")
    public void uploadCommonParams(@RequestParam Configuration config) {
        configurationService.update(config);
    }

    /**
     * Установка значений общих параметров по умолчанию
     */
    @PostMapping(value = "/actions/changeToDefaultCommonParams")
    public void setCommonParamsDefault() {
        configurationService.setCommonParamsDefault();
    }

}
