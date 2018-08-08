package com.aplana.sbrf.taxaccounting.web.mvc;


import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriodJournalItem;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.action.OpenCorrectionPeriodAction;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.result.ClosePeriodResult;
import com.aplana.sbrf.taxaccounting.model.result.DeletePeriodResult;
import com.aplana.sbrf.taxaccounting.model.result.OpenPeriodResult;
import com.aplana.sbrf.taxaccounting.model.result.ReopenPeriodResult;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.permissions.DepartmentReportPeriodPermissionSetter;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        binder.registerCustomEditor(OpenCorrectionPeriodAction.class, new RequestParamEditor(OpenCorrectionPeriodAction.class));
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
        // устанавливаем значение фильтра по умолчанию
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
     *
     * @param departmentId   ID подразделения
     * @param reportPeriodId ID отчетного периода
     * @return Последний отчетный период подразделения
     */
    @GetMapping(value = "/rest/departmentReportPeriod", params = "projection=fetchLast")
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
            period.setDepartmentId(item.getDepartmentId());
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
    public OpenPeriodResult open(@RequestParam DepartmentReportPeriod departmentReportPeriod) {
        return periodService.open(departmentReportPeriod, securityService.currentUserInfo());
    }

    /**
     * Закрытие периода для подразделения
     *
     * @param departmentReportPeriodId идентификатор закрываемого перода
     * @param skipHasNotAcceptedCheck пропускает проверку наличия форм в состоянии отличном от "Принято"
     * @return {@link ClosePeriodResult}
     */
    @PostMapping(value = "/actions/departmentReportPeriod/{departmentReportPeriodId}/close")
    public ClosePeriodResult close(@PathVariable Integer departmentReportPeriodId, @RequestParam boolean skipHasNotAcceptedCheck) {
        return periodService.close(departmentReportPeriodId, skipHasNotAcceptedCheck);
    }

    /**
     * Удаление периода для подразделения
     *
     * @param departmentReportPeriodId идентификатор удаляемого периода
     * @return {@link DeletePeriodResult}
     */
    @PostMapping(value = "/actions/departmentReportPeriod/delete")
    public DeletePeriodResult delete(@RequestParam Integer departmentReportPeriodId) {
        return periodService.delete(departmentReportPeriodId);
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
     * Открыть корректирующий период для подразделения
     *
     * @param action данные по корректирующему периоду
     * @return {@link OpenPeriodResult}
     */
    @PostMapping(value = "actions/departmentReportPeriod/openCorrectPeriod")
    public OpenPeriodResult openCorrectPeriod(@RequestParam OpenCorrectionPeriodAction action) {
        return periodService.openCorrectionPeriod(action);
    }

    /**
     * Переоткрывает закрытый период
     *
     * @param departmentReportPeriodId фильтр с данными о периоде и датой сдачи отчетности
     * @return {@link ReopenPeriodResult}
     */
    @PostMapping(value = "actions/departmentReportPeriod/reopen")
    public ReopenPeriodResult reopen(@RequestParam Integer departmentReportPeriodId) {
        return periodService.reopen(departmentReportPeriodId);
    }
}
