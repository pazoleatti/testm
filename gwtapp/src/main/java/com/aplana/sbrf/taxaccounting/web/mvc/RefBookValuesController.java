package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportPeriodType;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.filter.DepartmentFilter;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttachFileType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookFormType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookOktmo;
import com.aplana.sbrf.taxaccounting.model.result.RefBookDepartmentDTO;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAsnuService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAttachFileTypeService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDeclarationTypeService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDepartmentService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookFormTypeService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookOktmoService;
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
    private final RefBookDepartmentService refBookDepartmentService;
    private final PeriodService periodService;
    private final SecurityService securityService;
    private final RefBookOktmoService oktmoService;
    private final RefBookFormTypeService refBookFormTypeService;

    public RefBookValuesController(RefBookAttachFileTypeService refBookAttachFileTypeService, RefBookAsnuService refBookAsnuService,
                                   RefBookDeclarationTypeService refBookDeclarationTypeService, RefBookDepartmentService refBookDepartmentService,
                                   PeriodService periodService, SecurityService securityService, RefBookOktmoService oktmoService,
                                   RefBookFormTypeService refBookFormTypeService) {
        this.refBookAttachFileTypeService = refBookAttachFileTypeService;
        this.refBookAsnuService = refBookAsnuService;
        this.refBookDeclarationTypeService = refBookDeclarationTypeService;
        this.refBookDepartmentService = refBookDepartmentService;
        this.periodService = periodService;
        this.securityService = securityService;
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
        binder.registerCustomEditor(DepartmentFilter.class, new RequestParamEditor(DepartmentFilter.class));
    }

    /**
     * Получение значений справочника подразделений с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    @GetMapping(value = "/rest/refBookValues/30", params = "projection=allDepartments")
    public JqgridPagedList<RefBookDepartment> findDepartments(@RequestParam String name, @RequestParam PagingParams pagingParams) {
        PagingResult<RefBookDepartment> departments = refBookDepartmentService.findDepartments(name, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(departments, departments.getTotalCount(), pagingParams);
    }

    /**
     * Получение списка всех подразделений
     *
     * @param searchPattern Строка с запросом поиска по справочнику
     * @param exactSearch   Признак того, что результат поиска должен быть с полным соответствием поисковой строке
     * @return список подразделений
     */
    @GetMapping(value = "/rest/refBookValues/30", params = "projection=allDepartmentsWithChildren")
    public List<RefBookDepartmentDTO> findAllDepartmentsWithChildren(@RequestParam(required = false) String searchPattern,
                                                                     @RequestParam(required = false) boolean exactSearch) {
        return refBookDepartmentService.findAllTBWithChildren(searchPattern, exactSearch);
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
        PagingResult<RefBookDepartment> departments = refBookDepartmentService.fetchAvailableBADepartments(user, name, pagingParams);
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
        PagingResult<RefBookDepartment> departments = refBookDepartmentService.fetchAvailableDestinationDepartments(user, name, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(departments, departments.getTotalCount(), pagingParams);
    }

    /**
     * Возвращяет страницу подразделений по фильтру
     *
     * @param filter       фильтр
     * @param name         значение поиска из select2, отдельно от фильтра, т.к. select2 иначе не умеет
     * @param pagingParams параметры пейджинга
     * @return страница подразделений
     */
    @GetMapping(value = "/rest/refBookValues/30", params = "projection=allByFilter")
    public JqgridPagedList<RefBookDepartment> findAllByFilter(@RequestParam DepartmentFilter filter, @RequestParam(required = false) String name, @RequestParam PagingParams pagingParams) {
        TAUser user = securityService.currentUserInfo().getUser();
        filter.setName(name);
        PagingResult<RefBookDepartment> departments = refBookDepartmentService.findAllByFilter(filter, pagingParams, user);
        return JqgridPagedResourceAssembler.buildPagedList(departments, departments.getTotalCount(), pagingParams);
    }

    /**
     * Получение действующих доступных (согласно правам доступа пользователя) значений ТБ справочника подразделений.
     *
     * @return Список значений справочника
     */
    @GetMapping(value = "/rest/refBookValues/30", params = "projection=activeAvailableTB")
    public List<RefBookDepartment> fetchActiveAvailableTB() {
        return refBookDepartmentService.fetchActiveAvailableTB(securityService.currentUserInfo().getUser());
    }

    /**
     * Получение ТБ, определенного через конфиуграционные параметры, использующиеся для создания отчетности "Приложение 2"
     *
     * @return Значения справочника
     */
    @GetMapping(value = "/rest/refBookValues/30", params = "projection=application2department")
    public RefBookDepartment fetchTBForApp2() {
        return refBookDepartmentService.fetchApplication2Department();
    }

    /**
     * Получение активных подразделениЙ исключая присутствующие
     * @param presentedTbIdList идентификаторы подразделений которые нужно исключить из выборки
     * @return  список подразделений
     */
    @GetMapping(value = "/rest/refBookValues/30", params = "projection=activeTBExcluding")
    public List<RefBookDepartment> findTbExcludingPresented(@RequestParam(required = false) List<Integer> presentedTbIdList) {
        return refBookDepartmentService.findTbExcludingPresented(presentedTbIdList);
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
