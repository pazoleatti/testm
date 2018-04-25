package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
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

    public RefBookController(RefBookFactory refBookFactory) {
        this.refBookFactory = refBookFactory;
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
     * @param version      Дата актуальности для выборки записей справочника
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    @GetMapping(value = "/rest/refBookRecords/{refBookId}")
    public JqgridPagedList<Map<String, RefBookValue>> fetchRefBookRecords(@PathVariable Long refBookId,
                                                                          @RequestParam Date version,
                                                                          @RequestParam PagingParams pagingParams) {
        RefBookDataProvider provider = refBookFactory.getDataProvider(refBookId);
        RefBookAttribute sortAttribute = StringUtils.isNotEmpty(pagingParams.getProperty()) ?
                refBookFactory.getAttributeByAlias(refBookId, pagingParams.getProperty()) : null;
        PagingResult<Map<String, RefBookValue>> records = provider.getRecordsWithVersionInfo(version,
                pagingParams, null, sortAttribute, pagingParams.getDirection().toLowerCase().equals("asc"));
        return JqgridPagedResourceAssembler.buildPagedList(records, records.getTotalCount(), pagingParams);
    }

    /**
     * Получение списка всех записей иерархического справочника без учета пэйджинга
     * Версии так же не учитываются, считаем, что у нас нет версионируемых иерархических справочников
     *
     * @param refBookId    Идентификатор справочника
     * @return Страница списка значений справочника
     */
    @SuppressWarnings("unchecked")
    @GetMapping(value = "/rest/refBookRecords/{refBookId}", params = "projection=hier")
    public Collection<Map<String, RefBookValue>> fetchHierRefBookRecords(@PathVariable Long refBookId) {
        RefBookDataProvider provider = refBookFactory.getDataProvider(refBookId);
        List<Map<String, RefBookValue>> records = provider.getRecords(null,null, null, null, true);
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
}
