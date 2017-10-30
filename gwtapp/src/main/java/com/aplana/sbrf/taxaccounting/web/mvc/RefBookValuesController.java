package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttachFileType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAsnuService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAttachFileTypeService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDeclarationTypeService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDepartmentDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Контроллер для доступа к справочникам
 */
@RestController
public class RefBookValuesController {
    final private RefBookAttachFileTypeService refBookAttachFileTypeService;

    final private RefBookAsnuService refBookAsnuService;

    final private RefBookDeclarationTypeService refBookDeclarationTypeService;

    final private RefBookDepartmentDataService refBookDepartmentDataService;

    final private PeriodService periodService;

    final private SecurityService securityService;

    public RefBookValuesController(RefBookAttachFileTypeService refBookAttachFileTypeService, RefBookAsnuService refBookAsnuService,
                                   RefBookDeclarationTypeService refBookDeclarationTypeService, RefBookDepartmentDataService refBookDepartmentDataService,
                                   PeriodService periodService, SecurityService securityService) {
        this.refBookAttachFileTypeService = refBookAttachFileTypeService;
        this.refBookAsnuService = refBookAsnuService;
        this.refBookDeclarationTypeService = refBookDeclarationTypeService;
        this.refBookDepartmentDataService = refBookDepartmentDataService;
        this.periodService = periodService;
        this.securityService = securityService;
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
     * Получение доступных (согласно правам доступа пользователя) значений справочника, для которых открыт заданный период,
     * с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param name           Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                       наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param reportPeriodId ID отчетного периода
     * @param pagingParams   Параметры пейджинга
     * @return Страница списка значений справочника
     */
    @GetMapping(value = "/rest/refBookValues/30", params = "projection=departmentsWithOpenPeriod")
    public JqgridPagedList<RefBookDepartment> fetchDepartmentsWithOpenPeriod(@RequestParam String name, @RequestParam Integer reportPeriodId, @RequestParam PagingParams pagingParams) {
        TAUser user = securityService.currentUserInfo().getUser();
        PagingResult<RefBookDepartment> departments = refBookDepartmentDataService.fetchDepartmentsWithOpenPeriod(user, name, reportPeriodId, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(departments, departments.getTotalCount(), pagingParams);
    }

    /**
     * Получение доступных (согласно правам доступа пользователя) значений справочника, для которых открыт заданный период,
     * с фильтрацией по наименованию подразделения и пейджингом для создания отчётности
     *
     * @param name           Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                       наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param reportPeriodId ID отчетного периода
     * @param pagingParams   Параметры пейджинга
     * @return Страница списка значений справочника
     */

    @GetMapping(value = "/rest/refBookValues/30", params = "projection=departmentsWithOpenPeriodForReport")
    public JqgridPagedList<RefBookDepartment> fetchDepartmentsWithOpenPeriodForReport(@RequestParam String name, @RequestParam Integer reportPeriodId, @RequestParam PagingParams pagingParams) {
        TAUser user = securityService.currentUserInfo().getUser();
        PagingResult<RefBookDepartment> departments = refBookDepartmentDataService.fetchDepartmentWithOpenPeriodForReport(user, name, reportPeriodId, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(departments, departments.getTotalCount(), pagingParams);
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
     * Получение всех Отчетных периодов
     *
     * @return Список периодов
     */
    @GetMapping(value = "/rest/refBookValues/reportPeriod", params = "projection=allPeriods")
    public List<ReportPeriod> fetchAllReportPeriods() {
        TAUser user = securityService.currentUserInfo().getUser();
        return periodService.getPeriodsByTaxTypeAndDepartments(TaxType.NDFL, Collections.singletonList(user.getDepartmentId()));
    }

    /**
     * Получение открытых Отчетных периодов
     *
     * @return Список периодов
     */
    @GetMapping(value = "/rest/refBookValues/reportPeriod", params = "projection=openPeriods")
    public List<ReportPeriod> fetchOpenReportPeriods() {
        TAUser user = securityService.currentUserInfo().getUser();
        return new ArrayList<ReportPeriod>(periodService.getOpenForUser(user, TaxType.NDFL));
    }
}
