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
import org.springframework.http.MediaType;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
public class DepartmentReportPeriodController {

    private DepartmentReportPeriodService departmentReportPeriodService;
    private PeriodService periodService;
    private SecurityService securityService;

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

    public DepartmentReportPeriodController(DepartmentReportPeriodService departmentReportPeriodService, PeriodService periodService, SecurityService securityService) {
        this.departmentReportPeriodService = departmentReportPeriodService;
        this.periodService = periodService;
        this.securityService = securityService;
    }

    /**
     * Получение отчетных периодов с фильтрацией и пагинацией
     */
    @GetMapping(value = "/rest/departmentReportPeriod")
    public List<DepartmentReportPeriodJournalItem> getAllPeriods(@RequestParam DepartmentReportPeriodFilter filter){
        List<DepartmentReportPeriodJournalItem> result = departmentReportPeriodService.findAll(filter);
        Map<Integer, DepartmentReportPeriodJournalItem> yearMap = new HashMap<>();
        for(DepartmentReportPeriodJournalItem item : result){
            if (yearMap.get(item.getYear()) == null){
                DepartmentReportPeriodJournalItem parentItem = new DepartmentReportPeriodJournalItem();
                parentItem.setYear(item.getYear());
                parentItem.setId(item.getYear());
                parentItem.setName("Календарный год " + item.getYear());
                yearMap.put(item.getYear(), parentItem);
                item.setParent(parentItem);
            }else {
                item.setParent(yearMap.get(item.getYear()));
            }
        }

        return result;
    }

    /**
     * Открытие периода для подразделения
     * @param departmentReportPeriod открываемый период
     * @return идентификатор uuid для логов
     */
    @PostMapping(value = "/actions/departmentReportPeriod/open")
    public String add(@RequestParam DepartmentReportPeriod departmentReportPeriod){
        return periodService.open(departmentReportPeriod);
    }

    /**
     * Редактирование периода для подразделения
     * @param departmentReportPeriod открываемый период
     * @return идентификатор uuid для логов
     */
    @PostMapping(value = "/rest/departmentReportPeriod/{departmentReportPeriodId}")
    public String edit(@RequestParam DepartmentReportPeriod departmentReportPeriod){
        return periodService.editPeriod(departmentReportPeriod, securityService.currentUserInfo());
    }

    /**
     * Проверка преиода подразделения на наличие непринятых деклараций
     */
    @PostMapping(value = "/rest/departmentReportPeriod/{departmentReportPeriodId}", params = "projection=checkHasNotAccepted")
    public String checkHasNotAccepted(@PathVariable Integer departmentReportPeriodId){
        return departmentReportPeriodService.checkHasNotAccepted(departmentReportPeriodId);
    }

    /**
     *  Закрытие периода для подразделения
     */
    @PostMapping(value = "/actions/departmentReportPeriod/{departmentReportPeriodId}/close")
    public String close(@PathVariable Integer departmentReportPeriodId){
        return periodService.close(departmentReportPeriodId);
    }

    /**
     * Удаление периода для подразделения
     */
    @PostMapping(value = "/actions/departmentReportPeriod/delete")
    public String delete(@RequestParam Integer id){return periodService.removeReportPeriod(id, securityService.currentUserInfo());}

    /**
     * Получение доступных для выбора отчетных периодов для открытия коррекционного периода
     */
    @GetMapping(value = "rest/departmentReportPeriod", params = "projection=closedWithoutCorrection")
    public JqgridPagedList<ReportPeriod> fetchСlosedWithoutCorrection(@RequestParam Integer departmentId, @RequestParam PagingParams pagingParams){
        PagingResult<ReportPeriod> result = periodService.getCorrectPeriods(TaxType.NDFL, departmentId, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(result, result.getTotalCount(), pagingParams);
    }

    /**
     * Проверка статуса отчетного периода для подразделения
     * @param departmentId - идентификатор подразделения
     * @param dictTaxPeriodId - идентификатор типа отчетного периода из справочника
     * @param year - год отчетного периода
     * @return статус искомого отчетного периода {@link PeriodStatusBeforeOpen}
     */
    @PostMapping(value = "rest/departmentReportPeriod/status")
    public String fetchStatus(@RequestParam Integer departmentId, @RequestParam Long dictTaxPeriodId, @RequestParam Integer year){
        return periodService.checkPeriodStatusBeforeOpen(TaxType.NDFL, year, departmentId, dictTaxPeriodId).name();
    }

    /**
     * Открыть коррекционный период для подразделения
     */
    @PostMapping(value = "actions/departmentReportPeriod/openCorrectPeriod")
    public String openCorrectPeriod(@RequestParam DepartmentReportPeriodFilter filter){
        DepartmentReportPeriod period = new DepartmentReportPeriod();
        period.setCorrectionDate(filter.getCorrectionDate());
        period.setReportPeriod(filter.getReportPeriod());
        period.setId(filter.getId());
        period.setDepartmentId(filter.getDepartmentId());
        return periodService.openCorrectionPeriod(period);
    }

    /**
     * Установка срока сдачи отчетности для периода
     */
    @PostMapping(value = "actions/departmentReportPeriod/updateDeadline", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void setDeadline(@RequestBody DepartmentReportPeriodFilter filter) throws ActionException {
        filter.setDeadline(new LocalDateTime(filter.getUtilDeadline()));
            periodService.setDeadline(filter);
    }

    /**
     * Проверка периода на наличие деклараций, находящихся на редактировании
     */
    @PostMapping(value = "rest/departmentReportPeriod/{departmentReportPeriodId}", params = "projection=checkHasBlockedDeclaration")
    public String checkHasBlockedDeclaration(@PathVariable Integer departmentReportPeriodId){
        return departmentReportPeriodService.checkHasBlockedDeclaration(departmentReportPeriodId);
    }
}
