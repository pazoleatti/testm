package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.refbook.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * Контроллер для доступа к справочникам
 */
@RestController
public class RefBookValuesController {
    private final RefBookAttachFileTypeService refBookAttachFileTypeService;

    private final RefBookAsnuService refBookAsnuService;

    private final RefBookDeclarationTypeService refBookDeclarationTypeService;

    private final RefBookDepartmentDataService refBookDepartmentDataService;

    private final PeriodService periodService;

    private final SecurityService securityService;

    private final RefBookOktmoService oktmoService;

    private final DepartmentService departmentService;

    private final RefBookFormTypeService refBookFormTypeService;

    public RefBookValuesController(RefBookAttachFileTypeService refBookAttachFileTypeService, RefBookAsnuService refBookAsnuService,
                                   RefBookDeclarationTypeService refBookDeclarationTypeService, RefBookDepartmentDataService refBookDepartmentDataService,
                                   PeriodService periodService, SecurityService securityService, DepartmentService departmentService, RefBookOktmoService oktmoService,
                                   RefBookFormTypeService refBookFormTypeService) {
        this.refBookAttachFileTypeService = refBookAttachFileTypeService;
        this.refBookAsnuService = refBookAsnuService;
        this.refBookDeclarationTypeService = refBookDeclarationTypeService;
        this.refBookDepartmentDataService = refBookDepartmentDataService;
        this.periodService = periodService;
        this.securityService = securityService;
        this.departmentService = departmentService;
        this.oktmoService = oktmoService;
        this.refBookFormTypeService = refBookFormTypeService;
    }

    /**
     * Привязка данных из параметров запроса
     *
     * @param binder спец. DataBinder для привязки
     */
    @InitBinder
    public void init(WebDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
    }

    /**
     * Получение доступных (согласно правам доступа пользователя) значений справочника с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    @GetMapping(value = "/rest/refBookValues/30", params = "projection=allDepartments")
    public JqgridPagedList<RefBookDepartment> fetchAllDepartments(@RequestParam String name, @RequestParam PagingParams pagingParams) {
        TAUser user = securityService.currentUserInfo().getUser();
        PagingResult<RefBookDepartment> departments = refBookDepartmentDataService.fetchAvailableDepartments(user, name, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(departments, departments.getTotalCount(), pagingParams);
    }

    /**
     * Получение доступных (согласно правам доступа пользователя) для бизнес-администрирования подразделений с фильтрацией по наименованию и пейджингом
     *
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    @GetMapping(value = "/rest/refBookValues/30", params = "projection=BADepartments")
    public JqgridPagedList<RefBookDepartment> fetchBADepartments(@RequestParam String name, @RequestParam PagingParams pagingParams) {
        TAUser user = securityService.currentUserInfo().getUser();
        PagingResult<RefBookDepartment> departments = refBookDepartmentDataService.fetchAvailableBADepartments(user, name, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(departments, departments.getTotalCount(), pagingParams);
    }

    /**
     * Получение подразделений, доступных (согласно правам доступа пользователя) для назначения исполнителями, с фильтрацией по наименованию и пейджингом
     *
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    @GetMapping(value = "/rest/refBookValues/30", params = "projection=destinationDepartments")
    public JqgridPagedList<RefBookDepartment> fetchDestinationDepartments(@RequestParam String name, @RequestParam PagingParams pagingParams) {
        TAUser user = securityService.currentUserInfo().getUser();
        PagingResult<RefBookDepartment> departments = refBookDepartmentDataService.fetchAvailableDestinationDepartments(user, name, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(departments, departments.getTotalCount(), pagingParams);
    }

    /**
     * Получение действующих доступных (согласно правам доступа пользователя) значений справочника, для которых открыт заданный период,
     * с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param name           Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                       наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param reportPeriodId ID отчетного периода
     * @param pagingParams   Параметры пейджинга
     * @return Страница списка значений справочника
     */
    @GetMapping(value = "/rest/refBookValues/30", params = "projection=activeDepartmentsWithOpenPeriod")
    public JqgridPagedList<RefBookDepartment> fetchActiveDepartmentsWithOpenPeriod(@RequestParam String name, @RequestParam Integer reportPeriodId, @RequestParam PagingParams pagingParams) {
        TAUser user = securityService.currentUserInfo().getUser();
        PagingResult<RefBookDepartment> departments = refBookDepartmentDataService.fetchActiveDepartmentsWithOpenPeriod(user, name, reportPeriodId, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(departments, departments.getTotalCount(), pagingParams);
    }

    /**
     * Получение действующих доступных (согласно правам доступа пользователя) значений ТБ справочника подразделений.
     *
     * @return Список значений справочника
     */

    @GetMapping(value = "/rest/refBookValues/30", params = "projection=activeAvailableTB")
    public List<RefBookDepartment> fetchActiveAvailableTB() {
        return refBookDepartmentDataService.fetchAllAvailableForPeriodManagement(securityService.currentUserInfo());
    }

    /**
     * Возвращяет подразделения, доступные для ведения периодов текущему пользователю
     */
    @GetMapping(value = "rest/refBookValues/30", params = "projection=allAvailableForPeriodManagement")
    public List<RefBookDepartment> fetchAllAvailableForPeriodManagement() {
        return refBookDepartmentDataService.fetchAllAvailableForPeriodManagement(securityService.currentUserInfo());
    }

    /**
     * Получение всех значений справочника Виды форм
     *
     * @return Значения справочника
     */
    @GetMapping(value = "/rest/refBookValues/207")
    public List<RefBookDeclarationType> fetchAllDeclarationTypes() {
        return refBookDeclarationTypeService.fetchAllDeclarationTypes();
    }

    /**
     * Получение всех значений справочника АСНУ
     *
     * @return Значения справочника
     */
    @GetMapping(value = "/rest/refBookValues/900")
    public List<RefBookAsnu> fetchAllAsnu() {
        return refBookAsnuService.fetchAvailableAsnu(securityService.currentUserInfo());
    }

    /**
     * Получение всех значений справочника Категории прикрепленных файлов
     *
     * @return Значения справочника
     */
    @GetMapping(value = "/rest/refBookValues/934")
    public List<RefBookAttachFileType> fetchAllAttachFileTypes() {
        return refBookAttachFileTypeService.fetchAllAttachFileTypes();
    }

    /**
     * Получение всех значений справочника ОКТМО
     *
     * @param name         Параметр фильтрации названию и коду
     * @param pagingParams Параметры пейджинга
     * @return Значения справочника
     */
    @GetMapping(value = "/rest/refBookValues/96")
    public JqgridPagedList<RefBookOktmo> fetchAllOktmo(@RequestParam String name, @RequestParam PagingParams pagingParams) {
        PagingResult<RefBookOktmo> result = oktmoService.fetchAll(name, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(result, result.getTotalCount(), pagingParams);
    }

    /**
     * Получение одной записи из справочника ОКТМО по ее коду
     *
     * @param code код записи
     * @return Значения справочника
     */
    @GetMapping(value = "/rest/refBookValues/oktmoByCode")
    public RefBookOktmo fetchOktmoByCode(@RequestParam String code) {
        return oktmoService.fetchByCode(code, new Date());
    }

    /**
     * Получение всех типов Отчетных периодов по имени
     *
     * @return Список периодов
     */
    @GetMapping(value = "/rest/refBookValues/reportPeriodType")
    public JqgridPagedList<ReportPeriodType> fetchReportPeriodsType(@RequestParam PagingParams pagingParams) {
        List<ReportPeriodType> result = periodService.getPeriodType();
        pagingParams.setCount(result.size());
        return JqgridPagedResourceAssembler.buildPagedList(result, result.size(), pagingParams);
    }

    /**
     * Получение типа Отчетных периодов по id
     *
     * @return Список периодов
     */
    @GetMapping(value = "/rest/refBookValues/reportPeriodTypeById")
    public ReportPeriodType fetchReportPeriodsTypeById(@RequestParam Long dictTaxPeriodId) {
        return periodService.getPeriodTypeById(dictTaxPeriodId);
    }

    /**
     * Получение всех значений справочника Виды налоговых форм (931)
     */
    @GetMapping(value = "/rest/refBookValues/931")
    public List<RefBookFormType> fetchAllFormType() {
        return refBookFormTypeService.fetchAll();
    }
}
