package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookDepartmentFilter;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Контроллер для доступа к справочникам
 */
@RestController
public class RefBookValuesController {
    private static final Log LOG = LogFactory.getLog(RefBookValuesController.class);

    @Autowired
    private RefBookAttachFileTypeService refBookAttachFileTypeService;

    @Autowired
    private RefBookAsnuService refBookAsnuService;

    @Autowired
    private RefBookDeclarationTypeService refBookDeclarationTypeService;

    @Autowired
    private RefBookDepartmentDataService refBookDepartmentDataService;

    @Autowired
    private PeriodService periodService;

    @Autowired
    private SecurityService securityService;

    /**
     * Привязка данных из параметров запроса
     *
     * @param binder спец. DataBinder для привязки
     */
    @InitBinder
    public void init(WebDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
        binder.registerCustomEditor(RefBookDepartmentFilter.class, new RequestParamEditor(RefBookDepartmentFilter.class));
    }

    /**
     * Получение всех значений справочника Подразделения
     *
     * @param filter       Фильтр
     * @param pagingParams Параметры пейджинга
     * @return
     */
    @GetMapping(value = "/rest/refBookValues/30")
    public JqgridPagedList<RefBookDepartment> fetchDepartments(@RequestParam RefBookDepartmentFilter filter, @RequestParam PagingParams pagingParams) {
        LOG.info("Fetch records for refbook DEPARTMENT");
        TAUser user = securityService.currentUserInfo().getUser();
        PagingResult<RefBookDepartment> departments = refBookDepartmentDataService.fetchAvailableDepartments(user, filter, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(departments, departments.getTotalCount(), pagingParams);
    }

    /**
     * Получение всех значений справочника Виды форм
     *
     * @return Значения справочника
     */
    @GetMapping(value = "/rest/refBookValues/207")
    public List<RefBookDeclarationType> fetchAllDeclarationTypes() {
        LOG.info("Fetch records for refbook DECLARATION_TYPE");
        return refBookDeclarationTypeService.fetchAllDeclarationTypes();
    }

    /**
     * Получение всех значений справочника АСНУ
     *
     * @return Значения справочника
     */
    @GetMapping(value = "/rest/refBookValues/900")
    public List<RefBookAsnu> fetchAllAsnu() {
        LOG.info("Fetch records for refbook ASNU");
        return refBookAsnuService.fetchAvailableAsnu(securityService.currentUserInfo());
    }

    /**
     * Получение всех значений справочника Категории прикрепленных файлов
     *
     * @return Значения справочника
     */
    @GetMapping(value = "/rest/refBookValues/934")
    public List<RefBookAttachFileType> fetchAllAttachFileTypes() {
        LOG.info("Fetch records for refbook ATTACH_FILE_TYPE");
        return refBookAttachFileTypeService.fetchAllAttachFileTypes();
    }

    /**
     * Получение всех Отчетных периодов
     *
     * @return Список периодов
     */
    @GetMapping(value = "/rest/refBookValues/reportPeriod")
    public List<ReportPeriod> fetchReportPeriods() {
        LOG.info("Fetch periods");
        TAUser user = securityService.currentUserInfo().getUser();
        List<ReportPeriod> periods = periodService.getPeriodsByTaxTypeAndDepartments(TaxType.NDFL, Arrays.asList(user.getDepartmentId()));
        return periods;
    }
}
