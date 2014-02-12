package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.*;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author aivanov
 */
@Component
@PreAuthorize("isAuthenticated()")
public class GetRefBookTreeValuesHandler extends
        AbstractActionHandler<GetRefBookTreeValuesAction, GetRefBookTreeValuesResult> {

    @Autowired
    RefBookFactory refBookFactory;

    @Autowired
    RefBookHelper refBookHelper;

    public GetRefBookTreeValuesHandler() {
        super(GetRefBookTreeValuesAction.class);
    }

    @Override
    public GetRefBookTreeValuesResult execute(GetRefBookTreeValuesAction action, ExecutionContext context) throws ActionException {

        RefBook refBook = refBookFactory.getByAttribute(action.getRefBookAttrId());
        System.out.println("refBook " + refBook);
        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(refBook.getId());
        System.out.println("refBookDataProvider " + refBookDataProvider);
        String filter = buildFilter(action.getFilter(), action.getSearchPattern(), refBook);
        System.out.println("filter " + filter);

        RefBookTreeItem item = action.getParent();
        PagingResult<Map<String, RefBookValue>> refBookPage =
                refBookDataProvider.getChildrenRecords(item != null ? item.getId() : null, action.getVersion(), null, filter, null);

        System.out.println("refBookPage " + refBookPage);
        GetRefBookTreeValuesResult result = new GetRefBookTreeValuesResult();

        result.setPage(asseblRefBookPage(action, refBookDataProvider, refBookPage, refBook));

        return result;
    }

    @Override
    public void undo(GetRefBookTreeValuesAction action, GetRefBookTreeValuesResult result, ExecutionContext context)
            throws ActionException {
    }

    private RefBookAttribute getRefBookAttributeById(RefBook refBook, int attributeId) {
        int i = 0;
        for (RefBookAttribute refBookAttribute : refBook.getAttributes()) {
            if (refBookAttribute.isVisible() && i++ == attributeId) {
                return refBookAttribute;
            }
        }
        return null;
    }

    private static String buildFilter(String filter, String serachPattern, RefBook refBook) {
        StringBuilder resultFilter = new StringBuilder();
        if (filter != null && !filter.trim().isEmpty()) {
            resultFilter.append(filter.trim());
        }

        StringBuilder resultSearch = new StringBuilder();
        if (serachPattern != null && !serachPattern.trim().isEmpty()) {

            for (RefBookAttribute attribute : refBook.getAttributes()) {
                if (RefBookAttributeType.STRING.equals(attribute.getAttributeType())) {
                    if (resultSearch.length() > 0) {
                        resultSearch.append(" or ");
                    }
                    resultSearch.append("LOWER(").append(attribute.getAlias()).append(")").append(" like ")
                            .append("'%" + serachPattern.trim().toLowerCase() + "%'");
                }/*
                 * else if
				 * (RefBookAttributeType.NUMBER.equals(attribute.getAttributeType
				 * ()) && isNumeric(serachPattern)){ if (resultSearch.length() >
				 * 0){ resultSearch.append(" or "); }
				 * resultSearch.append(attribute
				 * .getAlias()).append("=").append("\"" + serachPattern + "\"");
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

    // Преобразуем в гуи модельку
    private PagingResult<RefBookTreeItem> asseblRefBookPage(GetRefBookTreeValuesAction action, RefBookDataProvider provider,
                                                            PagingResult<Map<String, RefBookValue>> refBookPage, RefBook refBook) {

        List<RefBookTreeItem> items = new ArrayList<RefBookTreeItem>();

        for (Map<String, RefBookValue> record : refBookPage) {

            RefBookTreeItem item = new RefBookTreeItem();
            // соответсвие гарантируется LinkedList'ом

            List<RefBookRecordDereferenceValue> refBookDereferenceValues = new LinkedList<RefBookRecordDereferenceValue>();

            item.setId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());
            //item.setParentId(record.get(RefBook.RECORD_PARENT_ID_ALIAS).getNumberValue().longValue());
            List<RefBookAttribute> attribute = refBook.getAttributes();

            Map<String, String> dereferenceRecord =
                    refBookHelper.singleRecordDereference(refBook, provider, attribute, record);

            for (RefBookAttribute refBookAttribute : attribute) {
                String dereferanceValue = dereferenceRecord.get(refBookAttribute.getAlias());
                if (refBookAttribute.isVisible()) {
                    RefBookRecordDereferenceValue dereferenceValue =
                            new RefBookRecordDereferenceValue(refBookAttribute.getId(), refBookAttribute.getAlias(), dereferanceValue);
                    refBookDereferenceValues.add(dereferenceValue);
                }
                if (refBookAttribute.getId().equals(action.getRefBookAttrId())) {
                    item.setDereferenceValue(dereferanceValue);
                }

            }
            item.setParent(action.getParent());
            item.setRefBookRecordDereferenceValues(refBookDereferenceValues);
            items.add(item);
        }

        return new PagingResult<RefBookTreeItem>(items, refBookPage.getTotalCount());
    }

}
