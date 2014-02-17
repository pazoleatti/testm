package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.GetRefBookMultiValuesAction;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.GetRefMultiBookValuesResult;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.RefBookItem;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.RefBookRecordDereferenceValue;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.*;

/**
 * @author sgoryachkin
 */
@Component
@PreAuthorize("isAuthenticated()")
public class GetRefBookMultiValuesHandler extends AbstractActionHandler<GetRefBookMultiValuesAction, GetRefMultiBookValuesResult> {

    @Autowired
    RefBookFactory refBookFactory;

    @Autowired
    RefBookHelper refBookHelper;

    public GetRefBookMultiValuesHandler() {
        super(GetRefBookMultiValuesAction.class);
    }

    @Override
    public GetRefMultiBookValuesResult execute(GetRefBookMultiValuesAction action,
                                               ExecutionContext context) throws ActionException {
        RefBook refBook = refBookFactory.getByAttribute(action.getRefBookAttrId());

        RefBookAttribute sortAttribute = getRefBookAttributeById(refBook, action.getSortAttributeIndex());

        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(refBook.getId());

        String filter = buildFilter(action.getFilter(), action.getSearchPattern(), refBook);

        PagingResult<Map<String, RefBookValue>> refBookPage;

        // Получаем нужные объекты по идентификаторам, что бы потом получить разименнованные значения
        if (action.getIdsTofind() != null && !action.getIdsTofind().isEmpty()) {
            refBookPage = new PagingResult<Map<String, RefBookValue>>();
            for (Long id : action.getIdsTofind()) {
                refBookPage.add(refBookDataProvider.getRecordData(id));
            }
            refBookPage.setTotalCount(action.getIdsTofind().size());
        } else {
            // TODO сделать так что бы через filter можно было задать сет идентификаторов для выборки (aivanov)
            refBookPage = refBookDataProvider
                    .getRecords(action.getVersion(), action.getPagingParams(), filter, sortAttribute, action.isSortAscending());

        }
        GetRefMultiBookValuesResult result = new GetRefMultiBookValuesResult();
        result.setPage(asseblRefBookPage(action, refBookDataProvider, refBookPage, refBook));
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
                if (refBookAttribute.isVisible() && i++ == attributeId) {
                    return refBookAttribute;
                }
            }
        }
        return null;
    }


    private static String buildFilter(String filter, String searchPattern, RefBook refBook) {
        StringBuilder resultFilter = new StringBuilder();
        if (filter != null && !filter.trim().isEmpty()) {
            resultFilter.append(filter.trim());
        }

        StringBuilder resultSearch = new StringBuilder();
        if (searchPattern != null && !searchPattern.trim().isEmpty()) {

            for (RefBookAttribute attribute : refBook.getAttributes()) {
                if (RefBookAttributeType.STRING.equals(attribute.getAttributeType())) {
                    if (resultSearch.length() > 0) {
                        resultSearch.append(" or ");
                    }
                    resultSearch.append("LOWER(").append(attribute.getAlias()).append(")").append(" like ")
                            .append("'%" + searchPattern.trim().toLowerCase() + "%'");
                }/*
                 * else if
				 * (RefBookAttributeType.NUMBER.equals(attribute.getAttributeType
				 * ()) && isNumeric(searchPattern)){ if (resultSearch.length() >
				 * 0){ resultSearch.append(" or "); }
				 * resultSearch.append(attribute
				 * .getAlias()).append("=").append("\"" + searchPattern + "\"");
				 * }
				 */
            }

        }

        if (resultFilter.length() > 0 && resultSearch.length() > 0) {
            return "(" + resultFilter.toString() + ") and (" + resultSearch.toString() + ")";
        } else if (resultFilter.length() > 0 && resultSearch.length() == 0) {
            return resultFilter.toString();
        } else if (resultSearch.length() > 0 && resultFilter.length() == 0) {
            return resultSearch.toString();
        } else {
            return null;
        }

    }

    public static boolean isNumeric(String str) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(str, pos);
        return str.length() == pos.getIndex();
    }

    /**
     * Трансформация объектов из базы в логические модели для таблицы
     *
     * @param action
     * @param provider
     * @param refBookPage
     * @param refBook
     * @return
     */
    private PagingResult<RefBookItem> asseblRefBookPage(
            GetRefBookMultiValuesAction action, RefBookDataProvider provider,
            PagingResult<Map<String, RefBookValue>> refBookPage, RefBook refBook) {

        List<RefBookItem> items = new LinkedList<RefBookItem>();

        for (Map<String, RefBookValue> record : refBookPage) {
            RefBookItem item = new RefBookItem();

            List<RefBookRecordDereferenceValue> refBookDereferenceValues = new LinkedList<RefBookRecordDereferenceValue>();

            item.setId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());
            List<RefBookAttribute> attribute = refBook.getAttributes();

            Map<String, String> dereferenceRecord = refBookHelper.singleRecordDereference(refBook, provider, attribute, record);

            for (RefBookAttribute refBookAttribute : attribute) {
                String dereferanceValue = dereferenceRecord.get(refBookAttribute.getAlias());
                if (refBookAttribute.isVisible()) {
                    RefBookRecordDereferenceValue dereferenceValue = new RefBookRecordDereferenceValue(
                            refBookAttribute.getId(),
                            refBookAttribute.getAlias(),
                            dereferanceValue);
                    refBookDereferenceValues.add(dereferenceValue);
                }
                if (refBookAttribute.getId().equals(action.getRefBookAttrId())) {
                    item.setDereferenceValue(dereferanceValue);
                }
            }

            item.setRefBookRecordDereferenceValues(refBookDereferenceValues);
            items.add(item);
        }

        return new PagingResult<RefBookItem>(items, refBookPage.getTotalCount());
    }

}
