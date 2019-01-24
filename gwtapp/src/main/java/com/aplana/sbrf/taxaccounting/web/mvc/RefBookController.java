package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookCountry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookShortInfo;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookSimple;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookTaxpayerState;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAsnuService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookCountryService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDocTypeService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookTaxpayerStateService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для работы с записями справочников
 */
@RestController
public class RefBookController {
    private final CommonRefBookService commonRefBookService;
    private final SecurityService securityService;
    private final RefBookAsnuService refBookAsnuService;
    private final RefBookDocTypeService refBookDocTypeService;
    private final RefBookCountryService refBookCountryService;
    private final RefBookTaxpayerStateService refBookTaxpayerStateService;

    public RefBookController(CommonRefBookService commonRefBookService, SecurityService securityService,
                             RefBookAsnuService refBookAsnuService, RefBookDocTypeService refBookDocTypeService,
                             RefBookCountryService refBookCountryService, RefBookTaxpayerStateService refBookTaxpayerStateService) {
        this.commonRefBookService = commonRefBookService;
        this.securityService = securityService;
        this.refBookAsnuService = refBookAsnuService;
        this.refBookDocTypeService = refBookDocTypeService;
        this.refBookCountryService = refBookCountryService;
        this.refBookTaxpayerStateService = refBookTaxpayerStateService;
    }

    /**
     * Привязка данных из параметров запроса
     *
     * @param binder спец. DataBinder для привязки
     */
    @InitBinder
    public void init(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
    }

    /**
     * Получение данных о справочнике
     *
     * @param refBookId Идентификатор справочника
     * @return Страница списка значений справочника
     */
    @GetMapping(value = "/rest/refBook/{refBookId}")
    public RefBook fetchRefBook(@PathVariable Long refBookId) {
        return commonRefBookService.get(refBookId);
    }

    /**
     * Получение списка записей справочника
     *
     * @param refBookId     Идентификатор справочника
     * @param recordId      Идентификатор группы версий записи справочника
     * @param version       Дата актуальности для выборки записей справочника
     * @param pagingParams  Параметры пейджинга
     * @param searchPattern Строка с запросом поиска по справочнику
     * @param exactSearch   Признак того, что результат поиска должен быть с полным соответствием поисковой строке
     * @param extraParams   дополнительные параметры для фильтрации записей
     * @return Страница списка значений справочника
     */
    @GetMapping(value = "/rest/refBookRecords/{refBookId}")
    public JqgridPagedList<Map<String, RefBookValue>> fetchRefBookRecords(@PathVariable Long refBookId,
                                                                          @RequestParam(required = false) Long recordId,
                                                                          @RequestParam(required = false) Date version,
                                                                          @RequestParam(required = false) String searchPattern,
                                                                          @RequestParam(required = false) boolean exactSearch,
                                                                          @RequestParam PagingParams pagingParams,
                                                                          @RequestBody(required = false) Map<String, String> extraParams) {
        RefBookAttribute sortAttribute = StringUtils.isNotEmpty(pagingParams.getProperty()) ?
                commonRefBookService.getAttributeByAlias(refBookId, pagingParams.getProperty()) : null;
        PagingResult<Map<String, RefBookValue>> records = commonRefBookService.fetchAllRecords(
                refBookId, recordId, version, searchPattern, exactSearch, extraParams, pagingParams, sortAttribute, pagingParams.getDirection());
        return JqgridPagedResourceAssembler.buildPagedList(records, records.getTotalCount(), pagingParams);
    }

    /**
     * Получение всех даных о справочниках для отображения в списке справочников
     *
     * @param name         строка поиска по имени справочника
     * @param pagingParams параметры сортировки и пагинации
     */
    @GetMapping(value = "rest/refBook")
    public List<RefBookShortInfo> findAll(@RequestParam(name = "filter", required = false) String name,
                                          @RequestParam(required = false) PagingParams pagingParams) {
        return commonRefBookService.findAllShortInfo(name, pagingParams);
    }

    /**
     * Получение всех значений указанного справочника замапленных на определенные сущности.
     * Используется для заполнения данными выпадашек.
     *
     * @param refBookId     идентификатор справочника
     * @param columns       список столбов таблицы справочника, по которым будет выполняться фильтрация
     * @param searchPattern шаблон поиска по полям справочника
     * @param filter        параметр фильтрации
     * @param pagingParams  параметры пейджинга
     * @return значения справочника
     */
    @GetMapping(value = "/rest/refBook/{refBookId}/records")
    public <T extends RefBookSimple> JqgridPagedList<T> fetchAllRecords(@PathVariable Long refBookId, @RequestParam(required = false) String[] columns,
                                                                        @RequestParam(required = false) String searchPattern, @RequestParam(required = false) String filter,
                                                                        @RequestParam(required = false) PagingParams pagingParams) {
        PagingResult<T> result = commonRefBookService.fetchAllRecords(refBookId, columns != null ? Arrays.asList(columns) : null, searchPattern, filter, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(result, result.getTotalCount(), pagingParams);
    }

    /**
     * Получение одного значения указанного справочника
     *
     * @param refBookId идентификатор справочника
     * @param recordId  идентификатор записи
     * @return значение записи справочника
     */
    @GetMapping(value = "/rest/refBook/{refBookId}/record/{recordId}")
    public <T extends RefBookSimple> T fetchRecordById(@PathVariable Long refBookId, @PathVariable Long recordId) {
        return commonRefBookService.fetchRecord(refBookId, recordId);
    }

    /**
     * Сохраняет изменения в записи справочника
     *
     * @param refBookId идентификатор справочника
     * @param recordId  идентификатор записи справочника
     * @param record    данные записи в структуре аттрибут-значение
     * @return результат сохранения
     */
    @PostMapping(value = "/actions/refBook/{refBookId}/editRecord/{recordId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ActionResult editRecord(@PathVariable Long refBookId, @PathVariable Long recordId, @RequestBody Map<String, RefBookValue> record) {
        return commonRefBookService.editRecord(securityService.currentUserInfo(), refBookId, recordId, record);
    }

    /**
     * Создает новую запись справочника
     *
     * @param refBookId идентификатор справочника
     * @param record    данные записи в структуре аттрибут-значение
     * @return результат сохранения
     */
    @PostMapping(value = "/actions/refBook/{refBookId}/createRecord", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ActionResult createRecord(@PathVariable Long refBookId, @RequestBody Map<String, RefBookValue> record) {
        return commonRefBookService.createRecord(securityService.currentUserInfo(), refBookId, record);
    }

    /**
     * Удаляет все версии указанных записей справочника
     *
     * @param refBookId идентификатор справочника
     * @param recordIds идентификаторы записей для удаления
     * @return результат удаления
     */
    @PostMapping(value = "/actions/refBook/{refBookId}/deleteRecords", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ActionResult deleteRecords(@PathVariable Long refBookId, @RequestBody List<Long> recordIds) {
        return commonRefBookService.deleteRecords(securityService.currentUserInfo(), refBookId, recordIds);
    }

    /**
     * Удаляет указанные записи (версии) справочника
     *
     * @param refBookId идентификатор справочника
     * @param recordIds идентификаторы записей для удаления
     * @return результат удаления
     */
    @PostMapping(value = "/actions/refBook/{refBookId}/deleteVersions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ActionResult deleteVersions(@PathVariable Long refBookId, @RequestBody List<Long> recordIds) {
        return commonRefBookService.deleteVersions(securityService.currentUserInfo(), refBookId, recordIds);
    }

    /**
     * Формирование отчета в XLSX с отбором записей учитывая версию, фильтрацию и сортировку
     *
     * @param refBookId     идентификатор справочника
     * @param version       версия, на которую строится отчет (для версионируемых справочников)
     * @param pagingParams  параметры сортировки для отображения записей в отчете так же как и в GUI
     * @param searchPattern Строка с запросом поиска по справочнику
     * @param exactSearch   Признак того, что результат поиска должен быть с полным соответствием поисковой строке
     * @param extraParams   дополнительные параметры для фильтрации записей
     * @return информация о создании отчета
     */
    @PostMapping(value = "/actions/refBook/{refBookId}/reportXlsx")
    public ActionResult exportRefBookToXlsx(@PathVariable long refBookId,
                                            @RequestParam(required = false) Date version,
                                            @RequestParam(required = false) PagingParams pagingParams,
                                            @RequestParam(required = false) String searchPattern,
                                            @RequestParam(required = false) boolean exactSearch,
                                            @RequestBody(required = false) Map<String, String> extraParams) {
        return commonRefBookService.createReport(securityService.currentUserInfo(), refBookId, version, pagingParams,
                searchPattern, exactSearch, extraParams, AsyncTaskType.EXCEL_REF_BOOK);
    }

    /**
     * Формирование отчета в CSV с отбором записей учитывая версию, фильтрацию и сортировку
     *
     * @param refBookId     идентификатор справочника
     * @param version       версия, на которую строится отчет (для версионируемых справочников)
     * @param pagingParams  параметры сортировки для отображения записей в отчете так же как и в GUI
     * @param searchPattern Строка с запросом поиска по справочнику
     * @param exactSearch   Признак того, что результат поиска должен быть с полным соответствием поисковой строке
     * @param extraParams   дополнительные параметры для фильтрации записей
     * @return информация о создании отчета
     */
    @PostMapping(value = "/actions/refBook/{refBookId}/reportCsv")
    public ActionResult exportRefBookToCsv(@PathVariable long refBookId,
                                           @RequestParam(required = false) Date version,
                                           @RequestParam(required = false) PagingParams pagingParams,
                                           @RequestParam(required = false) String searchPattern,
                                           @RequestParam(required = false) boolean exactSearch,
                                           @RequestBody(required = false) Map<String, String> extraParams) {
        return commonRefBookService.createReport(securityService.currentUserInfo(), refBookId, version, pagingParams,
                searchPattern, exactSearch, extraParams, AsyncTaskType.CSV_REF_BOOK);
    }

    /**
     * Найти все действующие записи справочника Статусы налогоплательщика
     *
     * @return список записей справочника Статусы налогоплательщика
     */
    @GetMapping(value = "/rest/refBook/903", params = "projection=findAllActive")
    public List<RefBookTaxpayerState> findAllActiveTaxPayerState() {
        return refBookTaxpayerStateService.findAllActive();
    }

    /**
     * Найти все записи справочника АСНУ
     *
     * @return список записей справочника АСНУ
     */
    @GetMapping(value = "/rest/refBook/900", params = "projection=findAllActive")
    public List<RefBookAsnu> findAllRefBookAsnu() {
        return refBookAsnuService.findAll();
    }

    /**
     * Найти все действующие записи справочника ОКСМ
     *
     * @return список записей справочника ОКСМ
     */
    @GetMapping(value = "/rest/refBook/10", params = "projection=findAllActive")
    public List<RefBookCountry> findAllActiveCountry() {
        return refBookCountryService.findAllActive();
    }

    /**
     * Найти все действующие записи справочника Коды документов
     *
     * @return список записей справочника Коды документов
     */
    @GetMapping(value = "/rest/refBook/360", params = "projection=findAllActive")
    public List<RefBookDocType> findAllActiveDocType() {
        return refBookDocTypeService.findAllActive();
    }

    /**
     * Создать идентификатор версии справочника
     * @return созданный идентификатор
     */
    @GetMapping(value = "/actions/getNextRefBookRecordId")
    public Long getNextRefBookRecordId() {
        return commonRefBookService.createNextRefBookRecordId();
    }
}