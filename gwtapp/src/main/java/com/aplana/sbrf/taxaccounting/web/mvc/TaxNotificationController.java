package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.TaxNotificationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер для Уведомлений о неудержанном налоге.
 */
@RestController
public class TaxNotificationController {

    private final TaxNotificationService taxNotificationService;
    private final SecurityService securityService;

    public TaxNotificationController(TaxNotificationService taxNotificationService, SecurityService securityService) {
        this.taxNotificationService = taxNotificationService;
        this.securityService = securityService;
    }

    /**
     * Добавить асинхронную задачу на создание Уведомления
     *
     * @param departmentId id тербанка
     * @param periodId     id периода
     * @param asnuIds      список id АСНУ
     * @return uuid логов создания задачи
     */
    @PostMapping("/actions/createTaxNotification")
    public String createTaxNotification(@RequestParam Integer departmentId,
                                        @RequestParam Integer periodId,
                                        @RequestParam(required = false) List<Long> asnuIds) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        String taskLogsUuid = taxNotificationService.createAsync(departmentId, periodId, asnuIds, userInfo);
        return taskLogsUuid;
    }
}
