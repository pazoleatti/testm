package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookSimple;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.RefBookListResult;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Контроллер для работы с записями справочников
 */
@RestController
public class RefBookController {
    private final RefBookFactory refBookFactory;
    private final CommonRefBookService commonRefBookService;
    private final SecurityService securityService;

    public RefBookController(RefBookFactory refBookFactory, CommonRefBookService commonRefBookService, SecurityService securityService) {
        this.refBookFactory = refBookFactory;
        this.commonRefBookService = commonRefBookService;
        this.securityService = securityService;
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
        return refBookFactory.get(refBookId);
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
     * @return Страница списка значений справочника
     */
    @GetMapping(value = "/rest/refBookRecords/{refBookId}")
    public JqgridPagedList<Map<String, RefBookValue>> fetchRefBookRecords(@PathVariable Long refBookId,
                                                                          @RequestParam(required = false) Long recordId,
                                                                          @RequestParam(required = false) Date version,
                                                                          @RequestParam(required = false) String searchPattern,
                                                                          @RequestParam(required = false) boolean exactSearch,
                                                                          @RequestParam PagingParams pagingParams) {
        PagingResult<Map<String, RefBookValue>> records = commonRefBookService.fetchAllRecords(
                refBookId, recordId, version, searchPattern, exactSearch, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(records, records.getTotalCount(), pagingParams);
    }

    /**
     * Получение списка всех записей иерархического справочника без учета пэйджинга
     * Версии так же не учитываются, считаем, что у нас нет версионируемых иерархических справочников
     *
     * @param refBookId     Идентификатор справочника
     * @param searchPattern Строка с запросом поиска по справочнику
     * @param exactSearch   Признак того, что результат поиска должен быть с полным соответствием поисковой строке
     * @return Страница списка значений справочника
     */
    @GetMapping(value = "/rest/refBookRecords/{refBookId}", params = "projection=hier")
    public Collection<Map<String, RefBookValue>> fetchHierRefBookRecords(@PathVariable Long refBookId,
                                                                         @RequestParam(required = false) String searchPattern,
                                                                         @RequestParam(required = false) boolean exactSearch) {
        return commonRefBookService.fetchHierRecords(refBookId, searchPattern, exactSearch);
    }

    /**
     * Получение всех даных о справочниках для отображения в списке справочников
     *
     * @return список объектов содержащих данные о справочниках
     */
    @GetMapping(value = "rest/refBook")
    public PagingResult<RefBookListResult> fetchAllRefBooks() {
        return commonRefBookService.fetchAllRefBooks();
    }

    /**
     * Получение всех значений указанного справочника замапленных на определенные сущности.
     * Используется для заполнения данными выпадашек.
     *
     * @param refBookId    идентификатор справочника
     * @param columns      список столбов таблицы справочника, по которым будет выполняться фильтрация
     * @param filter       параметр фильтрации
     * @param pagingParams параметры пейджинга
     * @return значения справочника
     */
    @GetMapping(value = "/rest/refBook/{refBookId}/records")
    public <T extends RefBookSimple> JqgridPagedList<T> fetchAllRecords(@PathVariable Long refBookId, @RequestParam String[] columns, @RequestParam String filter, @RequestParam PagingParams pagingParams) {
        //TODO: добавить учитывание версии для отбора записей
        PagingResult<T> result = commonRefBookService.fetchAllRecords(refBookId, Arrays.asList(columns), filter, pagingParams);
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
     * Удаляет указанные записи правочника
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
     * Формирование отчета в XLSX с отбором записей учитывая версию, фильтрацию и сортировку
     *
     * @param refBookId     идентификатор справочника
     * @param version       версия, на которую строится отчет (для версионируемых справочников)
     * @param pagingParams  параметры сортировки для отображения записей в отчете так же как и в GUI
     * @param searchPattern Строка с запросом поиска по справочнику
     * @param exactSearch   Признак того, что результат поиска должен быть с полным соответствием поисковой строке
     * @return информация о создании отчета
     */
    @PostMapping(value = "/actions/refBook/{refBookId}/reportXlsx")
    public ActionResult exportRefBookToXlsx(@PathVariable long refBookId,
                                                    @RequestParam(required = false) Date version,
                                                    @RequestParam(required = false) PagingParams pagingParams,
                                                    @RequestParam(required = false) String searchPattern,
                                                    @RequestParam(required = false) boolean exactSearch) {
        return commonRefBookService.createReport(securityService.currentUserInfo(), refBookId, version, pagingParams,
                searchPattern, exactSearch, AsyncTaskType.EXCEL_REF_BOOK);
    }

    /**
     * Формирование отчета в CSV с отбором записей учитывая версию, фильтрацию и сортировку
     *
     * @param refBookId     идентификатор справочника
     * @param version       версия, на которую строится отчет (для версионируемых справочников)
     * @param pagingParams  параметры сортировки для отображения записей в отчете так же как и в GUI
     * @param searchPattern Строка с запросом поиска по справочнику
     * @param exactSearch   Признак того, что результат поиска должен быть с полным соответствием поисковой строке
     * @return информация о создании отчета
     */
    @PostMapping(value = "/actions/refBook/{refBookId}/reportCsv")
    public ActionResult exportRefBookToCsv(@PathVariable long refBookId,
                                                   @RequestParam(required = false) Date version,
                                                   @RequestParam(required = false) PagingParams pagingParams,
                                                   @RequestParam(required = false) String searchPattern,
                                                   @RequestParam(required = false) boolean exactSearch) {
        return commonRefBookService.createReport(securityService.currentUserInfo(), refBookId, version, pagingParams,
                searchPattern, exactSearch, AsyncTaskType.CSV_REF_BOOK);
    }
}