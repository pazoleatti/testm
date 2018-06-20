package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.CreateApplication2Service;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер Приложения 2
 */
@RestController
public class CreateApplication2Controller {

    private SecurityService securityService;

    private CreateApplication2Service createApplication2Service;

    public CreateApplication2Controller(SecurityService securityService, CreateApplication2Service createApplication2Service) {
        this.securityService = securityService;
        this.createApplication2Service = createApplication2Service;
    }

    /**
     * Формирует файл Приложения 2 для декларации по налогу на прибыль
     * @param reportYear    отчетный год
     * @return  строка с uuid уведомлений о задаче
     */
    @PostMapping(value = "/actions/createApplication2")
    public String createApplication2(@RequestParam int reportYear) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return createApplication2Service.createApplication2Task(reportYear, userInfo);
    }
}
