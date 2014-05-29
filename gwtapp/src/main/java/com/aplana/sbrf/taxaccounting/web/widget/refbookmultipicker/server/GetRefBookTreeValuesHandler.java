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
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookRecordDereferenceValue;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookTreeItem;
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

        GetRefBookTreeValuesResult result = new GetRefBookTreeValuesResult();
        Logger logger = new Logger();
        RefBook refBook = refBookFactory.getByAttribute(action.getRefBookAttrId());
        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(refBook.getId());
        Department userDep = departmentService.getDepartment(securityService.currentUserInfo().getUser().getDepartmentId());
        String filter = buildFilter(action.getFilter(), action.getSearchPattern(), refBook, userDep);
        String filterWithoutPattern = buildFilter(action.getFilter(), null, refBook, userDep);

        if (filter != null && filter.equals(RefBookPickerUtils.NO_REGION_MATCHES_FLAG)) {
            //Среди подразделений пользователя нет относящихся к какому то региону и нет смысла получать записи справочника - ни одна не должна быть ему доступна
            result.setPage(new PagingResult<RefBookTreeItem>(new LinkedList<RefBookTreeItem>(), 0));
            result.setUuid(logEntryService.save(logger.getEntries()));
            return result;
        }

        PagingResult<Map<String, RefBookValue>> refBookPage;
        List<Long> filterIds = new ArrayList<Long>();
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
            RefBookAttribute sort = null;
            try {
                sort = refBook.getAttribute("NAME");
            } catch (IllegalArgumentException ignored) {
            }

            // идея такая: если есть пользовательский фильтр то грузим все все равно, потом узнаем
            // какие попадают и потом при разиминовывании устанавливаем флаг итему что он попадает по фильтр
            RefBookTreeItem parent = action.getParent();
            refBookPage = refBookDataProvider.getChildrenRecords(parent != null ? parent.getId() : null, action.getVersion(), null, filterWithoutPattern, sort);
            // TODO aivanov заменить на более оптимальный вариант
            for (Map<String, RefBookValue> record : refBookDataProvider.getChildrenRecords(parent != null ? parent.getId() : null, action.getVersion(), null, filter, null)) {
                filterIds.add(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());
            }
        }

        result.setUuid(logEntryService.save(logger.getEntries()));
        result.setPage(asseblRefBookPage(action, refBookPage, refBook, filterIds));

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
            if (regionFilter.equals(RefBookPickerUtils.NO_REGION_MATCHES_FLAG)) {
                return regionFilter;
            }
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
    private PagingResult<RefBookTreeItem> asseblRefBookPage(GetRefBookTreeValuesAction action,
                                                            PagingResult<Map<String, RefBookValue>> refBookPage,
                                                            RefBook refBook,
                                                            List<Long> filterIds) {

        List<RefBookTreeItem> items = new LinkedList<RefBookTreeItem>();
        List<RefBookAttribute> attributes = refBook.getAttributes();

        // кэшируем список дополнительных атрибутов если есть для каждого аттрибута
        Map<Long, List<Long>> attrId2Map = refBookHelper.getAttrToListAttrId2Map(attributes);
        //кэшируем список провайдеров для атрибутов-ссылок, чтобы для каждой строки их заново не создавать
        Map<Long, RefBookDataProvider> refProviders = refBookHelper.getHashedProviders(attributes, attrId2Map);

        for (Map<String, RefBookValue> record : refBookPage) {
            RefBookTreeItem item = new RefBookTreeItem();

            List<RefBookRecordDereferenceValue> refBookDereferenceValues = new LinkedList<RefBookRecordDereferenceValue>();

            item.setId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());

            if(filterIds.contains(item.getId())){
                item.setNeedToOpen(true);
            }

            for (RefBookAttribute refBookAttribute : attributes) {
                String alias = refBookAttribute.getAlias();
                Long id = refBookAttribute.getId();
                RefBookValue value = null;
                String dereferanceValueString = null;

                if (refBookAttribute.isVisible()) {

                    RefBookRecordDereferenceValue dereferenceValue = new RefBookRecordDereferenceValue(
                            refBookAttribute.getId(),
                            refBookAttribute.getAlias());

                    if (RefBookAttributeType.REFERENCE.equals(refBookAttribute.getAttributeType())) {
                        Long refValue = record.get(alias).getReferenceValue();
                        if (refValue != null) {
                            // получаем провайдер данных для целевого справочника
                            RefBookDataProvider attrProvider = refProviders.get(id);
                            // запрашиваем значение для разыменовывания
                            value = attrProvider.getValue(refValue, refBookAttribute.getRefBookAttributeId());
                            // для каждого найденного дополнительного аттрибута разименуем значение
                            if (attrId2Map.get(id) != null) {
                                for (Long id2 : attrId2Map.get(id)) {
                                    RefBookDataProvider attr2Provider = refProviders.get(id2);
                                    RefBookValue value2 = attr2Provider.getValue(refValue, id2);
                                    dereferenceValue.getAttrId2DerefValueMap().put(id2, value2 == null ? "" : String.valueOf(value2));
                                }
                            }
                        }
                    } else {
                        value = record.get(alias);
                    }
                    dereferanceValueString = (value == null ? "" : String.valueOf(value));
                    dereferenceValue.setDereferenceValue(dereferanceValueString);
                    refBookDereferenceValues.add(dereferenceValue);
                }
                if (id.equals(action.getRefBookAttrId())) {
                    item.setDereferenceValue(dereferanceValueString);
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
