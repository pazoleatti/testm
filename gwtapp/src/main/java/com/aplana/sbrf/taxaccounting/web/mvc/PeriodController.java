package com.aplana.sbrf.taxaccounting.web.mvc;


import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import net.sf.jasperreports.web.actions.ActionException;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;


@RestController
public class PeriodController {

    /**
     * Привязка данных из параметров запроса
     *
     * @param binder спец. DataBinder для привязки
     */
    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
        binder.registerCustomEditor(DepartmentReportPeriodFilter.class, new RequestParamEditor(DepartmentReportPeriodFilter.class));
        binder.registerCustomEditor(DataRow.class, new RequestParamEditor(DataRow.class));
        binder.registerCustomEditor(DepartmentReportPeriod.class, new RequestParamEditor(DepartmentReportPeriod.class));
        binder.registerCustomEditor(ReportPeriod.class, new RequestParamEditor(ReportPeriod.class));
        binder.registerCustomEditor(TaxPeriod.class, new RequestParamEditor(TaxPeriod.class));
        binder.registerCustomEditor(Cell.class, new RequestParamEditor(Cell.class));
    }

    @Autowired
    public PeriodController(DepartmentReportPeriodService departmentReportPeriodService, PeriodService periodService, SecurityService securityService) {
        this.departmentReportPeriodService = departmentReportPeriodService;
        this.periodService = periodService;
        this.securityService = securityService;
    }

    private DepartmentReportPeriodService departmentReportPeriodService;
    private PeriodService periodService;
    private SecurityService securityService;

    /**
     * Получение отчетных периодов с фильтрацией и пагинацией
     */
    @GetMapping(value = "/rest/reportPeriods", params = "projection=reportPeriods")
    public JqgridPagedList<DepartmentReportPeriodJournalItem> getAllPeriods(@RequestParam DepartmentReportPeriodFilter filter, @RequestParam PagingParams pagingParams){
        PagingResult<DepartmentReportPeriodJournalItem> result = departmentReportPeriodService.findAll(filter, pagingParams);

        return JqgridPagedResourceAssembler.buildPagedList(
                result,
                result.getTotalCount(),
                pagingParams);
    }

    /**
     * Открытие периода для подразделения
     * @param departmentReportPeriod открываемый период
     * @return идентификатор uuid для логов
     */
    @PostMapping(value = "/rest/reportPeriods/newPeriod")
    public String add(@RequestParam DepartmentReportPeriod departmentReportPeriod){
        return periodService.open(departmentReportPeriod);
    }

    /**
     * Редактирование периода для подразделения
     * @param departmentReportPeriod открываемый период
     * @return идентификатор uuid для логов
     */
    @PostMapping(value = "/rest/reportPeriods/updatePeriod")
    public String edit(@RequestParam DepartmentReportPeriod departmentReportPeriod){
        return periodService.editPeriod(departmentReportPeriod, securityService.currentUserInfo());
    }


    /**
     * Проверка преиода подразделения на наличие непринятых деклараций
     */
    @GetMapping(value = "/rest/reportPeriods", params = "projection=checkNotAccepted")
    public String checkHasNotAccepted(@RequestParam Long id){
        return departmentReportPeriodService.checkHasNotAccepted(id);
    }

    /**
     *  Закрытие периода для подразделения
     */
    @PostMapping(value = "/rest/reportPeriods/closePeriod")
    public String close(@RequestParam DepartmentReportPeriodFilter filter){
        return periodService.close(filter);
    }

    /**
     * Закрытие периода для подразделения
     */
    @PostMapping(value = "/rest/reportPeriods/delete")
    public String delete(@RequestParam Long[] ids){return periodService.removeReportPeriod(ids, securityService.currentUserInfo());}

    /**
     * Получение доступных для выбора отчетных периодов для открытия коррекционного периода
     */
    @GetMapping(value = "rest/reportPeriods/allowCorrectionPeriod")
    public JqgridPagedList<ReportPeriod> getCorrectPeriod(@RequestParam Integer departmentId, @RequestParam PagingParams pagingParams){
        PagingResult<ReportPeriod> result = periodService.getCorrectPeriods(TaxType.NDFL, departmentId, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(result, result.getTotalCount(), pagingParams);
    }

    /**
     * Открыть коррекционный период для подразделения
     */
    @PostMapping(value = "rest/reportPeriods/openCorrectPeriod")
    public String openCorrectPeriod(@RequestParam DepartmentReportPeriodFilter filter){
        DepartmentReportPeriod period = new DepartmentReportPeriod();
        period.setCorrectionDate(new LocalDateTime(filter.getSimpleCorrectionDate()));
        period.setReportPeriod(filter.getReportPeriod());
        period.setId(filter.getId());
        period.setDepartmentId(filter.getDepartmentId());
        return periodService.openCorrectionPeriod(period);
    }

    @PostMapping(value = "rest/reportPeriods/deadline")
    public void setDeadline(@RequestParam DepartmentReportPeriodFilter filter, @RequestParam boolean withChild) throws ActionException {
        filter.setDeadline(new LocalDateTime(filter.getSimpleCorrectionDate()));
        periodService.setDeadline(filter, withChild);
    }

}
