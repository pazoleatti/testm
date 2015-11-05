package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.server;

import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.TAException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerUtils;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.GetRefBookMultiValuesAction;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.GetRefMultiBookValuesResult;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.PickerContext;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookItem;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookRecordDereferenceValue;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * обработчик для загрузки данных для компонента линейного справочника
 *
 * @author sgoryachkin
 */
@Component
@PreAuthorize("isAuthenticated()")
public class GetRefBookMultiValuesHandler extends AbstractActionHandler<GetRefBookMultiValuesAction, GetRefMultiBookValuesResult> {

    @Autowired
    RefBookFactory refBookFactory;

    @Autowired
    RefBookHelper refBookHelper;

    @Autowired
    LogEntryService logEntryService;

    @Autowired
    SecurityService securityService;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    FormDataService formDataService;

    @Autowired
    RefBookPickerFilterBuilder buildFilter;

    public GetRefBookMultiValuesHandler() {
        super(GetRefBookMultiValuesAction.class);
    }

    @Override
    public GetRefMultiBookValuesResult execute(GetRefBookMultiValuesAction action,
                                               ExecutionContext executionContext) throws ActionException {
        GetRefMultiBookValuesResult result = new GetRefMultiBookValuesResult();
        Logger logger = new Logger();
        RefBook refBook = refBookFactory.getByAttribute(action.getRefBookAttrId());
        PickerContext context = action.getContext();

        RefBookAttribute sortAttribute = getRefBookAttributeById(refBook, action.getSortAttributeIndex());

        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(refBook.getId());

        String filter = buildFilter.buildMultiPickerFilter(action.getFilter(), action.getSearchPattern(), refBook, context);
        if (filter != null && filter.equals(RefBookPickerUtils.NO_REGION_MATCHES_FLAG)) {
            //Среди подразделений пользователя нет относящихся к какому то региону и нет смысла получать записи справочника - ни одна не должна быть ему доступна
            result.setPage(new PagingResult<RefBookItem>(new LinkedList<RefBookItem>(), 0));
            result.setUuid(logEntryService.save(logger.getEntries()));
            return result;
        }

        PagingResult<Map<String, RefBookValue>> refBookPage;

        // Получаем нужные объекты по идентификаторам, что бы потом получить разименнованные значения
        if (action.getIdsTofind() != null) {
            if (!action.getIdsTofind().isEmpty()) {
                refBookPage = new PagingResult<Map<String, RefBookValue>>();
                for (Long id : action.getIdsTofind()) {
                    if (id != null) {
                        try {
                            refBookPage.add(refBookDataProvider.getRecordData(id));
                        } catch (TAException e) {
                            logger.error(e.getMessage());
                        }
                    }
                }
                refBookPage.setTotalCount(action.getIdsTofind().size());
            } else {
                refBookPage = refBookDataProvider
                        .getRecords(action.getVersion(), null, filter, null);
            }
        } else {
            // TODO сделать так что бы через filter можно было задать сет идентификаторов для выборки (aivanov)
            refBookPage = refBookDataProvider
                    .getRecords(action.getVersion(), action.getPagingParams(), filter, sortAttribute, action.isSortAscending());

        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        result.setPage(asseblRefBookPage(action, refBookPage, refBook));

        return result;
    }

    @Override
    public void undo(GetRefBookMultiValuesAction action, GetRefMultiBookValuesResult result, ExecutionContext context)
            throws ActionException {
    }

    private RefBookAttribute getRefBookAttributeById(RefBook refBook, Integer attributeId) {
        if (attributeId != null) {
            int i = 0;
            for (RefBookAttribute refBookAttribute : refBook.getAttributes()) {
                if (i++ == attributeId) {
                    return refBookAttribute;
                }
            }
        }
        return null;
    }

    /**
     * Трансформация объектов из базы в логические модели для таблицы
     *
     * @param action
     * @param refBookPage
     * @param refBook
     * @return
     */
    private PagingResult<RefBookItem> asseblRefBookPage(GetRefBookMultiValuesAction action,
                                                        PagingResult<Map<String, RefBookValue>> refBookPage,
                                                        RefBook refBook) {
        List<RefBookItem> items = new LinkedList<RefBookItem>();
        List<RefBookAttribute> attributes = refBook.getAttributes();
        Set<Long> attributeIds = new HashSet<Long>();

        // кэшируем список дополнительных атрибутов если есть для каждого аттрибута
        Map<Long, List<Long>> attrId2Map = refBookHelper.getAttrToListAttrId2Map(attributes);

        Map<Long, List<RefBookAttributePair>> attributesMap = new HashMap<Long, List<RefBookAttributePair>>();

        /**
         * Заполняем мапу связками атрибут-запись, для дальнейшего разыменовывания пачкой
         * Упорядочиваем их по идентификатору справочника - для каждого справочника будет своя логика
         */
        for (Map<String, RefBookValue> record : refBookPage) {
            for (RefBookAttribute refBookAttribute : attributes) {
                String alias = refBookAttribute.getAlias();
                Long id = refBookAttribute.getId();
                Long refBookId = refBookAttribute.getRefBookId();

                if (RefBookAttributeType.REFERENCE.equals(refBookAttribute.getAttributeType())) {
                    Long refValue = record.get(alias).getReferenceValue();
                    if (refValue != null) {
                        attributeIds.add(refBookAttribute.getRefBookAttributeId());
                        //Получаем связки для основных атрибутов
                        if (attributesMap.containsKey(refBookId)) {
                            attributesMap.get(refBookId).add(new RefBookAttributePair(refBookAttribute.getRefBookAttributeId(), refValue));
                        } else {
                            List<RefBookAttributePair> list = new ArrayList<RefBookAttributePair>();
                            list.add(new RefBookAttributePair(refBookAttribute.getRefBookAttributeId(), refValue));
                            attributesMap.put(refBookId, list);
                        }
                        //Получаем связки для атрибутов второго уровня
                        if (attrId2Map.get(id) != null) {
                            for (Long id2 : attrId2Map.get(id)) {
                                RefBook refBook2 = refBookFactory.getByAttribute(id2);
                                attributeIds.add(id2);
                                if (attributesMap.containsKey(refBook2.getId())) {
                                    attributesMap.get(refBook2.getId()).add(new RefBookAttributePair(id2, refValue));
                                } else {
                                    List<RefBookAttributePair> list = new ArrayList<RefBookAttributePair>();
                                    list.add(new RefBookAttributePair(refBookAttribute.getRefBookAttributeId(), refValue));
                                    attributesMap.put(refBook2.getId(), list);
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * Получаем провайдеры для каждого справочника и кэшируем их
         * Получаем разыменованую пачку значений
         */
        Map<Long, RefBookDataProvider> refProviders = refBookHelper.getProviders(attributeIds);
        Map<RefBookAttributePair, String> dereferencedAttributes = new HashMap<RefBookAttributePair, String>();

        for (Map.Entry<Long, List<RefBookAttributePair>> attribute : attributesMap.entrySet()) {
            RefBookDataProvider provider = refProviders.get(attribute.getKey());
            dereferencedAttributes.putAll(provider.getAttributesValues(attribute.getValue()));
        }

        HashMap<Formats, SimpleDateFormat> formatHashMap = new HashMap<Formats, SimpleDateFormat>();
        /**
         * Раздаем разыменованные значения по требованию
         */
        for (Map<String, RefBookValue> record : refBookPage) {
            RefBookItem item = new RefBookItem();

            List<RefBookRecordDereferenceValue> refBookDereferenceValues = new LinkedList<RefBookRecordDereferenceValue>();

            item.setId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());

            for (RefBookAttribute refBookAttribute : attributes) {
                String alias = refBookAttribute.getAlias();
                Long id = refBookAttribute.getId();
                String value = null;

                RefBookRecordDereferenceValue dereferenceValue = new RefBookRecordDereferenceValue(
                        refBookAttribute.getId(),
                        refBookAttribute.getAlias());

                if (RefBookAttributeType.REFERENCE.equals(refBookAttribute.getAttributeType())) {
                    Long refValue = record.get(alias).getReferenceValue();
                    if (refValue != null) {
                        value = dereferencedAttributes.get(new RefBookAttributePair(refBookAttribute.getRefBookAttributeId(), refValue));
                        // для каждого найденного дополнительного аттрибута разименуем значение
                        if (attrId2Map.get(id) != null) {
                            for (Long id2 : attrId2Map.get(id)) {
                                String value2 = dereferencedAttributes.get(new RefBookAttributePair(id2, refValue));
                                dereferenceValue.getAttrId2DerefValueMap().put(id2, value2 == null ? "" : value2);
                            }
                        }
                    }
                } else {
                    RefBookValue refBookValue = record.get(alias);
                    if (refBookValue != null) {
                        if (refBookValue.getAttributeType() == RefBookAttributeType.DATE) {
                            Date dValue = refBookValue.getDateValue();
                            if (dValue != null) {
                                Formats format = refBookAttribute.getFormat();
                                if (format != null) {
                                    if (formatHashMap.containsKey(format)) {
                                        value = formatHashMap.get(format).format(refBookValue.getDateValue());
                                    } else {
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format.getFormat());
                                        formatHashMap.put(format, simpleDateFormat);
                                        value = simpleDateFormat.format(refBookValue.getDateValue());
                                    }
                                } else {
                                    value = String.valueOf(refBookValue);
                                }
                            }
                        } else {
                            value = String.valueOf(refBookValue);
                        }
                    } else {
                        value = "";
                    }
                }
                dereferenceValue.setDereferenceValue(value);
                refBookDereferenceValues.add(dereferenceValue);
                if (id.equals(action.getRefBookAttrId())) {
                    item.setDereferenceValue(value);
                }
            }

            item.setRefBookRecordDereferenceValues(refBookDereferenceValues);
            items.add(item);
        }

        return new PagingResult<RefBookItem>(items, refBookPage.getTotalCount());
    }
}
