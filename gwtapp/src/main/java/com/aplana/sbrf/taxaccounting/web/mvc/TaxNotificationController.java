package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.service.TaxNotificationService;
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

    public TaxNotificationController(TaxNotificationService taxNotificationService) {
        this.taxNotificationService = taxNotificationService;
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
        String taskLogsUuid = taxNotificationService.createAsync(departmentId, periodId, asnuIds);
        return taskLogsUuid;
    }
}
