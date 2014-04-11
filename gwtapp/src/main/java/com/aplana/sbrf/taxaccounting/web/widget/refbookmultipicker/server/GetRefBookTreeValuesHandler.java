package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.server;

import com.aplana.sbrf.taxaccounting.model.Department;
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
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerUtils;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.*;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.*;

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

    @Autowired
    SecurityService securityService;

    @Autowired
    DepartmentService departmentService;

    public GetRefBookTreeValuesHandler() {
        super(GetRefBookTreeValuesAction.class);
    }

    @Override
    public GetRefBookTreeValuesResult execute(GetRefBookTreeValuesAction action, ExecutionContext context) throws ActionException {

        Logger logger = new Logger();
        RefBook refBook = refBookFactory.getByAttribute(action.getRefBookAttrId());
        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(refBook.getId());
        Department userDep = departmentService.getDepartment(securityService.currentUserInfo().getUser().getDepartmentId());
        String filter = buildFilter(action.getFilter(), action.getSearchPattern(), refBook, userDep);

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

    private static String buildFilter(String filter, String searchPattern, RefBook refBook, Department dep) {
        StringBuilder resultFilter = new StringBuilder();
        if (filter != null && !filter.trim().isEmpty()) {
            resultFilter.append(filter.trim());
        }

        String regionFilter = RefBookPickerUtils.buildRegionFilterForUser(dep == null ? null : Arrays.asList(dep), refBook);
        if (regionFilter != null) {
            if (resultFilter.length() > 0) {
                resultFilter.append(" and ");
            }
            resultFilter.append("(" + regionFilter + ")");
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
            List<RefBookAttribute> attributes = refBook.getAttributes();

            Map<Long, String> dereferenceRecord =
                    refBookHelper.singleRecordDereferenceWithAttrId2(refBook, provider, attributes, record);
            Map<Long, List<Long>> attrId2Map = refBookHelper.getAttrToListAttrId2Map(attributes);

            for (RefBookAttribute refBookAttribute : attributes) {
                String dereferanceValue = dereferenceRecord.get(refBookAttribute.getId());
                if (refBookAttribute.isVisible()) {
                    RefBookRecordDereferenceValue dereferenceValue =
                            new RefBookRecordDereferenceValue(refBookAttribute.getId(), refBookAttribute.getAlias(), dereferanceValue);
                    refBookDereferenceValues.add(dereferenceValue);

                    // добавляем разоименованные значения по аттрибутам второго уровня
                    if (attrId2Map.get(refBookAttribute.getId()) != null) {
                        for (Long id2 : attrId2Map.get(refBookAttribute.getId())) {
                            dereferenceValue.getAttrId2DerefValueMap().put(id2, dereferenceRecord.get(id2));
                        }
                    }
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
