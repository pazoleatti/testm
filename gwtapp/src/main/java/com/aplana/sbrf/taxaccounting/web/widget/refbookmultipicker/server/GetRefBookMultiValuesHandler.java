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
import com.aplana.sbrf.taxaccounting.service.FormDataService;
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

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.*;

/**
 * обработчик для загрузки данных для компонента линейного справочника
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

        String filter = buildFilter(action.getFilter(), action.getSearchPattern(), refBook, context);
        if (filter != null && filter.equals(RefBookPickerUtils.NO_REGION_MATCHES_FLAG)) {
            //Среди подразделений пользователя нет относящихся к какому то региону и нет смысла получать записи справочника - ни одна не должна быть ему доступна
            result.setPage(new PagingResult<RefBookItem>(new LinkedList<RefBookItem>(), 0));
            result.setUuid(logEntryService.save(logger.getEntries()));
            return result;
        }

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
            // TODO сделать так что бы через filter можно было задать сет идентификаторов для выборки (aivanov)
            refBookPage = refBookDataProvider
                    .getRecords(action.getVersion(), action.getPagingParams(), filter, sortAttribute, action.isSortAscending());

        }
        result.setUuid(logEntryService.save(logger.getEntries()));
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


    private String buildFilter(String filter, String searchPattern, RefBook refBook, PickerContext context) {
        StringBuilder resultFilter = new StringBuilder();
        if (filter != null && !filter.trim().isEmpty()) {
            resultFilter.append(filter.trim());
        }

        if ((refBook != null)
                && (refBook.getRegionAttribute() != null)
                && (context != null) ) {

            String regionFilter;
            switch (context.getRegionFilter()) {
                case DEPARTMENT_CONFIG_FILTER:
                    regionFilter = refBook.getRegionAttribute().getAlias() + " = " + context.getAttributeId();
                    break;
                case DEFAULT:
                case FORM_FILTER:
                    Department department = null;
                    if (context.getFormDataId() != null) {
                        department = departmentService.getFormDepartment(context.getFormDataId());
                    }
                    regionFilter = RefBookPickerUtils.buildRegionFilterForUser(department == null ? null : Arrays.asList(department), refBook);
                    if (regionFilter != null && regionFilter.equals(RefBookPickerUtils.NO_REGION_MATCHES_FLAG)) {
                        return regionFilter;
                    }
                    break;
                default:
                    regionFilter = null;
            }

            if (regionFilter != null) {
                if (resultFilter.length() > 0) {
                    resultFilter.append(" and ");
                }
                resultFilter.append("(" + regionFilter + ")");
            }

        }

        // TODO порефакторить вынести в отдельный метод в refBookFactory
        StringBuilder resultSearch = new StringBuilder();
        if (searchPattern != null && !searchPattern.trim().isEmpty()) {

            for (RefBookAttribute attribute : refBook.getAttributes()) {
                if (RefBookAttributeType.STRING.equals(attribute.getAttributeType()) || RefBookAttributeType.DATE.equals(attribute.getAttributeType())) {
                    if (resultSearch.length() > 0) {
                        resultSearch.append(" or ");
                    }
                    resultSearch.append("LOWER(").append(attribute.getAlias()).append(")").append(" like ")
                            .append("'%" + searchPattern.trim().toLowerCase() + "%'");
                } else if (RefBookAttributeType.NUMBER.equals(attribute.getAttributeType())) {
                    if (resultSearch.length() > 0) {
                        resultSearch.append(" or ");
                    }
                    resultSearch.append("TO_CHAR(").append(attribute.getAlias()).append(")").append(" like ")
                            .append("'%" + searchPattern.trim().toLowerCase() + "%'");
                } else if (RefBookAttributeType.REFERENCE.equals(attribute.getAttributeType())) {
                    if (resultSearch.length() > 0) {
                        resultSearch.append(" or ");
                    }

                    RefBookAttribute nextAttribute = attribute;
                    String alias = attribute.getAlias();
                    while (nextAttribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        RefBook rb = refBookFactory.getByAttribute(nextAttribute.getRefBookAttributeId());
                        nextAttribute = rb.getAttribute(nextAttribute.getRefBookAttributeId());
                        alias = alias + "." + nextAttribute.getAlias();
                    }

                    if (RefBookAttributeType.STRING.equals(nextAttribute.getAttributeType()) || RefBookAttributeType.DATE.equals(nextAttribute.getAttributeType())) {
                        resultSearch.append("LOWER(").append(alias).append(")").append(" like ")
                                .append("'%" + searchPattern.trim().toLowerCase() + "%'");
                    } else if (RefBookAttributeType.NUMBER.equals(nextAttribute.getAttributeType())) {
                        resultSearch.append("TO_CHAR(").append(alias).append(")").append(" like ")
                                .append("'%" + searchPattern.trim().toLowerCase() + "%'");
                    }
                }
            }

        }

        if (resultFilter.length() > 0 && resultSearch.length() > 0) {
            return "(" + resultFilter.toString() + ") and (" + resultSearch.toString() + ")";
        } else if (resultFilter.length() > 0 && resultSearch.length() == 0) {
            return resultFilter.toString();
        } else if (resultSearch.length() > 0 && resultFilter.length() == 0) {
            return resultSearch.toString();
        } else if ("".equals(filter)) {
            return "";
        }
        return null;
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
            List<RefBookAttribute> attributes = refBook.getAttributes();

            Map<Long, String> dereferenceRecord = refBookHelper.singleRecordDereferenceWithAttrId2(refBook, provider, attributes, record);
            Map<Long, List<Long>> attrId2Map = refBookHelper.getAttrToListAttrId2Map(attributes);

            for (RefBookAttribute refBookAttribute : attributes) {
                String dereferanceValue = dereferenceRecord.get(refBookAttribute.getId());
                if (refBookAttribute.isVisible()) {

                    RefBookRecordDereferenceValue dereferenceValue = new RefBookRecordDereferenceValue(
                            refBookAttribute.getId(),
                            refBookAttribute.getAlias(),
                            dereferanceValue);
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

            item.setRefBookRecordDereferenceValues(refBookDereferenceValues);
            items.add(item);
        }

        return new PagingResult<RefBookItem>(items, refBookPage.getTotalCount());
    }

}
