package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TaxType;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Контроллер для доступа к справочникам
 *
 * @author dloshkarev
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

    @GetMapping(value = "/rest/refBookValues/30")
    public List<RefBookDepartment> fetchAllDepartments() {
        LOG.info("Fetch records for refbook DEPARTMENT");
        return refBookDepartmentDataService.fetchDepartments();
    }

    @GetMapping(value = "/rest/refBookValues/207")
    public List<RefBookDeclarationType> fetchAllDeclarationTypes() {
        LOG.info("Fetch records for refbook DECLARATION_TYPE");
        return refBookDeclarationTypeService.fetchAllDeclarationTypes();
    }

    @GetMapping(value = "/rest/refBookValues/900")
    public List<RefBookAsnu> fetchAllAsnu() {
        LOG.info("Fetch records for refbook ASNU");
        return refBookAsnuService.fetchAllAsnu();
    }

    @GetMapping(value = "/rest/refBookValues/934")
    public List<RefBookAttachFileType> fetchAllAttachFileTypes() {
        LOG.info("Fetch records for refbook ATTACH_FILE_TYPE");
        return refBookAttachFileTypeService.fetchAllAttachFileTypes();
    }

    @GetMapping(value = "/rest/refBookValues/reportPeriod")
    public List<ReportPeriod> fetchReportPeriods() {
        LOG.info("Fetch periods");
        TAUser user = securityService.currentUserInfo().getUser();
        return new ArrayList<ReportPeriod>(periodService.getOpenForUser(user, TaxType.NDFL));
    }

    /*private static final String DATE_VERSION_FORMAT = "dd-MM-yyyy";

    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private RefBookDtoBuilder dtoBuilder;


    @GetMapping(value = "/rest/refbook/{refBookId}")
    public PagingResult<RefBookBase> fetchAll(@PathVariable Long refBookId,
                                           @RequestParam String version,
                                           @RequestParam(required = false) String paging,
                                           @RequestParam(required = false) String filter) {
        LOG.info(String.format("Fetch records for refbook = %s, version = %s, paging = %s, filter = %s", refBookId, version, paging, filter));
        Date versionDate;
        try {
            versionDate = new SimpleDateFormat(DATE_VERSION_FORMAT).parse(version);
        } catch (ParseException e) {
            throw new ServiceException("Cannot parse version as Date. Format should be: " + DATE_VERSION_FORMAT, e);
        }
        RefBookDataProvider dataProvider = refBookFactory.getDataProvider(refBookId);
        PagingResult<Map<String, RefBookValue>> records = dataProvider.getRecords(versionDate, getPagingParams(paging), filter, null);

        List<RefBookBase> result = new ArrayList<RefBookBase>();
        for (Map<String, RefBookValue> record : records) {
            result.add(dtoBuilder.getRefBookDTO(refBookId, record, null));
        }
        return new PagingResult<RefBookBase>(result);
    }*/

    /**
     * <p>Создание диапазона на основе данных параметров фильтрации. Если среди параметров есть параметр с
     * именем 'paging', то диапазон формируется со значениями, указанными
     * в данном параметре, иначе диапазон инициализируется значениями по умолчанию.</p>
     * <p>
     * <p>Значение параметра представляет собой строку вида "10;20", где 10 - начало диапазона, а
     * 20 - конец диапазона.</p>
     *
     * @param paging строка вида "10;20". Может быть NULL
     * @return диапазон, может быть NULL, если не найден соответствующий параметр
     * @throws IllegalArgumentException если значение параметра содержит более двух строковых элементов, разделенных ';'
     *                                  или они не могут быть преобразованы в числа
     */
    /*private PagingParams getPagingParams(String paging) {
        if (paging != null) {
            StringTokenizer values = new StringTokenizer(paging, PagingParams.PAGING_PARAM_SEPARATOR);
            // Должно быть минимум два параметра
            if (values.countTokens() != 2) {
                throw new IllegalArgumentException("Paging param count must be 2: \"from\" & \"to\"");
            }
            // Значения параметров должны быть числами
            try {
                return new PagingParams(Integer.valueOf(values.nextToken()), Integer.valueOf(values.nextToken()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Paging param must be the integer type", e);
            }
        }
        return null;
    }*/
}
