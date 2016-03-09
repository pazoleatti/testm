package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.server;

import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.TAException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.refbook.RefBookCache;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
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
import org.springframework.context.ApplicationContext;
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
	private ApplicationContext applicationContext;
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
    public GetRefMultiBookValuesResult execute(GetRefBookMultiValuesAction action, ExecutionContext executionContext) throws ActionException {
		RefBookCache refBookCacher = (RefBookCache) applicationContext.getBean(RefBookCache.class);

        GetRefMultiBookValuesResult result = new GetRefMultiBookValuesResult();
        Logger logger = new Logger();
        RefBook refBook = refBookCacher.getByAttribute(action.getRefBookAttrId());
        PickerContext context = action.getContext();

        RefBookAttribute sortAttribute = getRefBookAttributeByIndex(refBook, action.getSortAttributeIndex());

        RefBookDataProvider refBookDataProvider = refBookCacher.getDataProvider(refBook.getId());

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

    private RefBookAttribute getRefBookAttributeByIndex(RefBook refBook, Integer index) {
        if (index != null) {
            int i = 0;
            for (RefBookAttribute refBookAttribute : refBook.getAttributes()) {
                if (i++ == index) {
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
			PagingResult<Map<String, RefBookValue>> refBookPage, RefBook refBook) {
        List<RefBookItem> items = new LinkedList<RefBookItem>();
        if (refBookPage.isEmpty()) {
            return new PagingResult<RefBookItem>();
        }
        List<RefBookAttribute> attributes = refBook.getAttributes();
        // разыменовывание ссылок
        Map<Long, Map<Long, String>> dereferenceValues = refBookHelper.dereferenceValues(refBook, refBookPage);

        for (Map<String, RefBookValue> record : refBookPage) {
            RefBookItem item = new RefBookItem();
            List<RefBookRecordDereferenceValue> refBookDereferenceValues = new LinkedList<RefBookRecordDereferenceValue>();
            item.setId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());
            for (RefBookAttribute attribute : attributes) {
                RefBookRecordDereferenceValue dereferenceValue = new RefBookRecordDereferenceValue(
                        attribute.getId(),
                        attribute.getAlias());

                RefBookValue value = record.get(attribute.getAlias());
                String tableCell;
                if (value == null) {
                    tableCell = "";
                } else {
                    switch (value.getAttributeType()) {
                        case NUMBER:
                            if (value.getNumberValue() == null) tableCell = "";
                            else tableCell = value.getNumberValue().toString();
                            break;
                        case DATE:
                            if (value.getDateValue() == null) tableCell = "";
                            else {
                                if (attribute.getFormat() != null) {
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                                            attribute.getFormat().getFormat());
                                    tableCell = simpleDateFormat.format(value.getDateValue());
                                } else {
                                    tableCell = value.getDateValue().toString();
                                }
                            }
                            break;
                        case STRING:
                            if (value.getStringValue() == null) tableCell = "";
                            else tableCell = value.getStringValue();
                            break;
                        case REFERENCE:
                            if (value.getReferenceValue() == null) tableCell = "";
                            else {
                                tableCell = dereferenceValues.get(attribute.getId()).get(value.getReferenceValue());
                            }
                            break;
                        default:
                            tableCell = "undefined";
                            break;
                    }
                }
                dereferenceValue.setDereferenceValue(tableCell);
                refBookDereferenceValues.add(dereferenceValue);
                if (attribute.getId().equals(action.getRefBookAttrId())) {
                    item.setDereferenceValue(tableCell);
                }
            }
            item.setRefBookRecordDereferenceValues(refBookDereferenceValues);
            items.add(item);
        }
        return new PagingResult<RefBookItem>(items, refBookPage.getTotalCount());
    }
}
