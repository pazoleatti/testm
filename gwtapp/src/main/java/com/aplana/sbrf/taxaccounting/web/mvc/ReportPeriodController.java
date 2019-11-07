package com.aplana.sbrf.taxaccounting.web.mvc;


import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.result.ReportPeriodResult;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReportPeriodController {

    private final SecurityService securityService;
    private final PeriodService periodService;

    public ReportPeriodController(SecurityService securityService, PeriodService periodService) {
        this.securityService = securityService;
        this.periodService = periodService;
    }

    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(ReportPeriod.class, new RequestParamEditor(ReportPeriod.class));
    }


    /**
     * Получение открытых Отчетных периодов
     *
     * @return Список периодов
     */
    @GetMapping(value = "/rest/reportPeriod", params = "projection=opened")
    public List<ReportPeriod> fetchOpened() {
        TAUser user = securityService.currentUserInfo().getUser();
        return periodService.findAllActive(user);
    }

    /**
     * Получение всех Отчетных периодов
     *
     * @return Список периодов
     */
    @GetMapping(value = "/rest/reportPeriod", params = "projection=all")
    public List<ReportPeriod> fetchAllReportPeriods() {
        return periodService.findAll();
    }

    /**
     * Получение всех Отчетных периодов для формы 2-НДФЛ (ФЛ)
     *
     * @return Список периодов
     */
    @GetMapping(value = "/rest/reportPeriod", params = "projection=for2NdflFL")
    public List<ReportPeriod> findAllFor2NdflFL() {
        return periodService.findAllFor2NdflFL();
    }

    /**
     * Получение всех Отчетных периодов для Приложения 2
     *
     * @return Список периодов
     */
    @GetMapping(value = "/rest/reportPeriod", params = "projection=forApp2")
    public List<ReportPeriod> findAllForApp2() {
        return periodService.findAllForApp2();
    }

    /**
     * Получает периоды назначенные подразделению
     *
     * @param departmentId идентификатор подразделения
     * @return возвращает список периодов назначенных подразделению
     */
    @GetMapping(value = "/rest/reportPeriod", params = "projection=forDepartment")
    public List<ReportPeriodResult> fetchForDepartment(@RequestParam Integer departmentId) {
        return periodService.fetchActiveByDepartment(departmentId);
    }
}
