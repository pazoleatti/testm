package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.TAException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.*;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Хендлер для загрузки данных для иерархичного компнента
 * @author aivanov
 */
@Component
@PreAuthorize("isAuthenticated()")
public class GetRefBookTreeValuesHandler extends AbstractActionHandler<GetRefBookTreeValuesAction, GetRefBookTreeValuesResult> {

    @Autowired
    RefBookFactory refBookFactory;

    @Autowired
    RefBookHelper refBookHelper;

    @Autowired
    LogEntryService logEntryService;

    public GetRefBookTreeValuesHandler() {
        super(GetRefBookTreeValuesAction.class);
    }

    @Override
    public GetRefBookTreeValuesResult execute(GetRefBookTreeValuesAction action, ExecutionContext context) throws ActionException {

        Logger logger = new Logger();
        RefBook refBook = refBookFactory.getByAttribute(action.getRefBookAttrId());
        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(refBook.getId());
        String filter = buildFilter(action.getFilter(), action.getSearchPattern(), refBook);

        PagingResult<Map<String, RefBookValue>> refBookPage;

        // Получаем нужные объекты по идентификаторам, что бы потом получить разименнованные значения
        if (action.getIdsTofind() != null && !action.getIdsTofind().isEmpty()) {
            refBookPage = new PagingResult<Map<String, RefBookValue>>();
            for (Long id : action.getIdsTofind()) {
                if (id != null) {
                    try{
                        refBookPage.add(refBookDataProvider.getRecordData(id));
                    } catch (TAException e){
                        logger.error(e.getMessage());
                    }
                }
            }
            refBookPage.setTotalCount(action.getIdsTofind().size());
        } else {
            RefBookTreeItem parent = action.getParent();
            refBookPage = refBookDataProvider.getChildrenRecords(parent != null ? parent.getId() : null, action.getVersion(), null, filter, null);

        }

        GetRefBookTreeValuesResult result = new GetRefBookTreeValuesResult();

        result.setUuid(logEntryService.save(logger.getEntries()));
        result.setPage(asseblRefBookPage(action, refBookDataProvider, refBookPage, refBook));

        return result;
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

    // Преобразуем в гуи модельку
    private PagingResult<RefBookTreeItem> asseblRefBookPage(GetRefBookTreeValuesAction action, RefBookDataProvider provider,
                                                            PagingResult<Map<String, RefBookValue>> refBookPage, RefBook refBook) {

        List<RefBookTreeItem> items = new ArrayList<RefBookTreeItem>();

        for (Map<String, RefBookValue> record : refBookPage) {

            RefBookTreeItem item = new RefBookTreeItem();

            List<RefBookRecordDereferenceValue> refBookDereferenceValues = new LinkedList<RefBookRecordDereferenceValue>();

            item.setId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());
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

    @Override
    public void undo(GetRefBookTreeValuesAction action, GetRefBookTreeValuesResult result, ExecutionContext context)
            throws ActionException {
    }

}
