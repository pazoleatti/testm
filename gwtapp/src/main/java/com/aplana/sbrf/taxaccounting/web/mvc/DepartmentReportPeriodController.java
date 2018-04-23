package com.aplana.sbrf.taxaccounting.web.mvc;


import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.permissions.DepartmentReportPeriodPermissionSetter;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import net.sf.jasperreports.web.actions.ActionException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для работы с {@link DepartmentReportPeriod}
 */
@RestController
public class DepartmentReportPeriodController {

    private DepartmentReportPeriodService departmentReportPeriodService;
    private PeriodService periodService;
    private SecurityService securityService;
    private DepartmentReportPeriodPermissionSetter departmentReportPeriodPermissionSetter;
    private DepartmentService departmentService;

    /**
     * Привязка данных из параметров запроса
     *
     * @param binder спец. DataBinder для привязки
     */
    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
        binder.registerCustomEditor(DepartmentReportPeriodFilter.class, new RequestParamEditor(DepartmentReportPeriodFilter.class));
        binder.registerCustomEditor(DepartmentReportPeriod.class, new RequestParamEditor(DepartmentReportPeriod.class));
        binder.registerCustomEditor(ReportPeriod.class, new RequestParamEditor(ReportPeriod.class));
        binder.registerCustomEditor(TaxPeriod.class, new RequestParamEditor(TaxPeriod.class));
    }

    public DepartmentReportPeriodController(DepartmentReportPeriodService departmentReportPeriodService, PeriodService periodService, SecurityService securityService,
                                            DepartmentReportPeriodPermissionSetter departmentReportPeriodPermissionSetter, DepartmentService departmentService) {
        this.departmentReportPeriodService = departmentReportPeriodService;
        this.periodService = periodService;
        this.securityService = securityService;
        this.departmentReportPeriodPermissionSetter = departmentReportPeriodPermissionSetter;
        this.departmentService = departmentService;
    }

    /**
     * Получение отчетных периодов с фильтрацией и пагинацией
     */
    @GetMapping(value = "/rest/departmentReportPeriod")
    public List<DepartmentReportPeriodJournalItem> getAllPeriods(@RequestParam DepartmentReportPeriodFilter filter) {
        //утсанавливаем значение фильтра по-умолчанию
        if (filter.getDepartmentId() == null) {
            filter.setDepartmentId(departmentService.getBankDepartment().getId());
        }
        List<DepartmentReportPeriodJournalItem> result = departmentReportPeriodService.fetchJournalItemByFilter(filter);
        Map<Integer, DepartmentReportPeriodJournalItem> yearMap = new HashMap<>();
        // создаем дополнительный родительский объект для каждого года
        // для группировки в гриде
        for (DepartmentReportPeriodJournalItem item : result) {
            if (yearMap.get(item.getYear()) == null) {
                DepartmentReportPeriodJournalItem parentItem = new DepartmentReportPeriodJournalItem();
                parentItem.setYear(item.getYear());
                parentItem.setId(item.getYear());
                parentItem.setName("Календарный год " + item.getYear());
                yearMap.put(item.getYear(), parentItem);
                item.setParent(parentItem);
            } else {
                item.setParent(yearMap.get(item.getYear()));
            }
        }

        setDepartmentReportPeriodPagePermissions(result);

        return result;
    }

    /**
     * Получение последнего отчетного периода подразделения по ID подразделения и ID отчетного периода
     * @param departmentId ID подразделения
     * @param reportPeriodId ID отчетного периода
     * @return Последний отчетный период подразделения
     */
    @GetMapping(value = "/rest/departmentReportPeriod",  params = "projection=fetchLast")
    public DepartmentReportPeriod fetchLastPeriod(int departmentId, int reportPeriodId) {
        return departmentReportPeriodService.fetchLast(departmentId, reportPeriodId);
    }

    /**
     * Устанавливает права на каждый элемент страницы списка периодов {@link DepartmentReportPeriodJournalItem}
     *
     * @param page страница с установленными правами на элементы периодов
     */
    private void setDepartmentReportPeriodPagePermissions(List<DepartmentReportPeriodJournalItem> page) {
        for (DepartmentReportPeriodJournalItem item : page) {
            DepartmentReportPeriod period = new DepartmentReportPeriod();
            period.setIsActive(item.getIsActive());
            period.setCorrectionDate(item.getCorrectionDate());
            departmentReportPeriodPermissionSetter.setPermissions(period, null);
            item.setPermissions(period.getPermissions());
        }
    }

    /**
     * Открытие периода для подразделения
     *
     * @param departmentReportPeriod открываемый период
     * @return идентификатор uuid для логов
     */
    @PostMapping(value = "/actions/departmentReportPeriod/open")
    public String open(@RequestParam DepartmentReportPeriod departmentReportPeriod) {
        return periodService.open(departmentReportPeriod);
    }

    /**
     * Проверка преиода подразделения на наличие непринятых деклараций
     *
     * @param departmentReportPeriodId - идентификатор проверяемого периода подразделения
     * @return uuid идентификатор логера
     */
    @PostMapping(value = "/rest/departmentReportPeriod/{departmentReportPeriodId}", params = "projection=checkHasNotAccepted")
    public String checkHasNotAccepted(@PathVariable Integer departmentReportPeriodId) {
        return departmentReportPeriodService.checkHasNotAccepted(departmentReportPeriodId);
    }

    /**
     * Закрытие периода для подразделения
     *
     * @param departmentReportPeriodId идентификатор закрываемого перода с подразделением "Банк"
     * @return uuid идентификатор логера
     */
    @PostMapping(value = "/actions/departmentReportPeriod/{departmentReportPeriodId}/close")
    public String close(@PathVariable Integer departmentReportPeriodId) {
        return periodService.close(departmentReportPeriodId);
    }

    /**
     * Удаление периода для подразделения
     *
     * @param departmentReportPeriodId идентификатор удаляемого периода
     * @return uuid идентификатор логера
     */
    @PostMapping(value = "/actions/departmentReportPeriod/remove")
    public String remove(@RequestParam Integer departmentReportPeriodId) {
        return periodService.removeReportPeriod(departmentReportPeriodId, securityService.currentUserInfo());
    }

    /**
     * Получение доступных для выбора отчетных периодов для открытия коррекционного периода
     *
     * @param departmentId идентификатор подразделения, для которого производится поиск
     * @param pagingParams параметры пагинации
     * @return страница {@link ReportPeriod} или пустой список
     */
    @GetMapping(value = "rest/departmentReportPeriod", params = "projection=closedWithoutCorrection")
    public JqgridPagedList<ReportPeriod> fetchClosedWithoutCorrection(@RequestParam Integer departmentId, @RequestParam PagingParams pagingParams) {
        List<ReportPeriod> result = periodService.getCorrectPeriods(departmentId);
        pagingParams.setCount(result.size());
        return JqgridPagedResourceAssembler.buildPagedList(result, result.size(), pagingParams);
    }

    /**
     * Проверка статуса отчетного периода для подразделения
     *
     * @param departmentId    идентификатор подразделения
     * @param dictTaxPeriodId идентификатор типа отчетного периода из справочника
     * @param year            год отчетного периода
     * @return статус искомого отчетного периода {@link PeriodStatusBeforeOpen}
     */
    @PostMapping(value = "rest/departmentReportPeriod/status")
    public String fetchStatus(@RequestParam Integer departmentId, @RequestParam Long dictTaxPeriodId, @RequestParam Integer year) {
        return periodService.checkPeriodStatus(year, departmentId, dictTaxPeriodId).name();
    }

    /**
     * Проверка статуса отчетного периода для подразделения
     *
     * @param departmentReportPeriod проверяемый коррекционный период
     * @return статус искомого отчетного периода {@link PeriodStatusBeforeOpen}
     */
    @PostMapping(value = "rest/departmentReportPeriod/status", params = "projection=checkCorrectPeriod")
    public String fetchStatusCorrectPeriod(@RequestParam DepartmentReportPeriod departmentReportPeriod) {
        return periodService.checkPeriodStatusBeforeOpen(departmentReportPeriod.getReportPeriod(), departmentReportPeriod.getDepartmentId(), departmentReportPeriod.getCorrectionDate()).name();
    }


    /**
     * Открыть коррекционный период для подразделения
     *
     * @param departmentReportPeriod открываемый коррекционный период
     * @return uuid идентификатор логера
     */
    @PostMapping(value = "actions/departmentReportPeriod/openCorrectPeriod")
    public String openCorrectPeriod(@RequestParam DepartmentReportPeriod departmentReportPeriod) {
        return periodService.openCorrectionPeriod(departmentReportPeriod);
    }

    /**
     * Установка срока сдачи отчетности для периода
     *
     * @param filter фильтр с данными о периоде и датой сдачи отчетности
     * @throws ActionException если дата сдачи отчетности не указана
     */
    @PostMapping(value = "actions/departmentReportPeriod/updateDeadline")
    public void updateDeadline(@RequestParam DepartmentReportPeriodFilter filter) throws ActionException {
        periodService.updateDeadline(filter);
    }

    /**
     * Проверка периода на наличие деклараций, находящихся на редактировании
     *
     * @param departmentReportPeriodId идентификатор проверяемого периода
     * @return uuid идентификатор логера
     */
    @PostMapping(value = "rest/departmentReportPeriod/{departmentReportPeriodId}", params = "projection=checkHasBlockedDeclaration")
    public String checkHasBlockedDeclaration(@PathVariable Integer departmentReportPeriodId) {
        return departmentReportPeriodService.checkHasBlockedDeclaration(departmentReportPeriodId);
    }
}
