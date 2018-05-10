package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.RefBookListResult;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.apache.commons.lang3.StringUtils;
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
     * @param refBookId    Идентификатор справочника
     * @param recordId     Идентификатор группы версий записи справочника
     * @param version      Дата актуальности для выборки записей справочника
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    @GetMapping(value = "/rest/refBookRecords/{refBookId}")
    public JqgridPagedList<Map<String, RefBookValue>> fetchRefBookRecords(@PathVariable Long refBookId,
                                                                          @RequestParam(required = false) Long recordId,
                                                                          @RequestParam(required = false) Date version,
                                                                          @RequestParam PagingParams pagingParams) {
        RefBookDataProvider provider = refBookFactory.getDataProvider(refBookId);
        RefBookAttribute sortAttribute = StringUtils.isNotEmpty(pagingParams.getProperty()) ?
                refBookFactory.getAttributeByAlias(refBookId, pagingParams.getProperty()) : null;
        PagingResult<Map<String, RefBookValue>> records;
        if (recordId == null) {
            // Отбираем все записи справочника
            records = provider.getRecordsWithVersionInfo(version,
                    pagingParams, null, sortAttribute, pagingParams.getDirection().toLowerCase().equals("asc"));
        } else {
            // Отбираем все версии записи правочника
            records = provider.getRecordVersionsByRecordId(recordId, pagingParams, null, sortAttribute);
        }
        return JqgridPagedResourceAssembler.buildPagedList(records, records.getTotalCount(), pagingParams);
    }

    /**
     * Получение списка всех записей иерархического справочника без учета пэйджинга
     * Версии так же не учитываются, считаем, что у нас нет версионируемых иерархических справочников
     *
     * @param refBookId Идентификатор справочника
     * @return Страница списка значений справочника
     */
    @SuppressWarnings("unchecked")
    @GetMapping(value = "/rest/refBookRecords/{refBookId}", params = "projection=hier")
    public Collection<Map<String, RefBookValue>> fetchHierRefBookRecords(@PathVariable Long refBookId) {
        RefBookDataProvider provider = refBookFactory.getDataProvider(refBookId);
        List<Map<String, RefBookValue>> records = provider.getRecords(null, null, null, null, true);
        Map<Number, Map<String, RefBookValue>> recordsById = new HashMap<>();
        List<Map<String, RefBookValue>> result = new ArrayList<>();

        // Группируем по id
        for (Map<String, RefBookValue> record : records) {
            recordsById.put(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue(), record);
        }
        // Собираем дочерние подразделения внутри родительских
        for (Map<String, RefBookValue> record : recordsById.values()) {
            Number parentId = record.get(RefBook.RECORD_PARENT_ID_ALIAS).getReferenceValue();
            if (parentId != null) {
                Map<String, RefBookValue> parent = recordsById.get(parentId);
                if (parent.containsKey(RefBook.RECORD_CHILDREN_ALIAS)) {
                    parent.get(RefBook.RECORD_CHILDREN_ALIAS).getCollectionValue().add(record);
                } else {
                    parent.put(RefBook.RECORD_CHILDREN_ALIAS, new RefBookValue(RefBookAttributeType.COLLECTION, new ArrayList()));
                }
                if (parentId.intValue() == Department.ROOT_DEPARTMENT_ID) {
                    // Отбираем только ТБ - все остальные подразделения будут уже как дочерние
                    result.add(record);
                }
            }
        }
        return result;
    }

    /**
     * Получение всех даных о справочниках для отображения в списке справочников
     *
     * @param pagingParams параметры пейджинга
     * @return список объектов содержащих данные о справочниках
     */
    @GetMapping(value = "rest/refBookList")
    public JqgridPagedList<RefBookListResult> fetchAllRefbooks(@RequestParam PagingParams pagingParams) {
        PagingResult<RefBookListResult> result = commonRefBookService.fetchAllRefBooks();
        return JqgridPagedResourceAssembler.buildPagedList(result, result.size(), pagingParams);
    }

    /**
     * Получение всех значений указанного справочника замапленных на определенные сущности
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
     * Получение количества версий для записи справочника
     *
     * @param refBookId идентификатор справочника
     * @param recordId  идентификатор записи
     * @return значение записи справочника
     */
    @GetMapping(value = "/actions/refBook/{refBookId}/recordVersionCount/{recordId}")
    public int getRecordVersionCount(@PathVariable Long refBookId, @PathVariable Long recordId) {
        return commonRefBookService.getRecordVersionCount(refBookId, recordId);
    }

    /**
     * Формирование отчета в XLSX
     *
     * @param refBookId    идентификатор справочника
     * @param version      версия, на которую строится отчет (для версионируемых справочников)
     * @param pagingParams параметры сортировки для отображения записей в отчете так же как и в GUI
     * @return информация о создании отчета
     */
    @PostMapping(value = "/actions/refBook/{refBookId}/reportXlsx")
    public ActionResult createDeclarationReportXlsx(@PathVariable long refBookId,
                                                    @RequestParam(required = false) Date version,
                                                    @RequestParam(required = false) PagingParams pagingParams) {
        return commonRefBookService.createReport(securityService.currentUserInfo(), refBookId, version, pagingParams, AsyncTaskType.EXCEL_REF_BOOK);
    }

    /**
     * Формирование отчета в CSV
     *
     * @param refBookId    идентификатор справочника
     * @param version      версия, на которую строится отчет (для версионируемых справочников)
     * @param pagingParams параметры сортировки для отображения записей в отчете так же как и в GUI
     * @return информация о создании отчета
     */
    @PostMapping(value = "/actions/refBook/{refBookId}/reportCsv")
    public ActionResult createDeclarationReportCsv(@PathVariable long refBookId,
                                                   @RequestParam(required = false) Date version,
                                                   @RequestParam(required = false) PagingParams pagingParams) {
        return commonRefBookService.createReport(securityService.currentUserInfo(), refBookId, version, pagingParams, AsyncTaskType.CSV_REF_BOOK);
    }
}
