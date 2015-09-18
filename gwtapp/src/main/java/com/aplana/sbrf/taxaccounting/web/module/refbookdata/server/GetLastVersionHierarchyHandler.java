package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetLastVersionHierarchyAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetLastVersionHierarchyResult;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("isAuthenticated()")
public class GetLastVersionHierarchyHandler extends AbstractActionHandler<GetLastVersionHierarchyAction, GetLastVersionHierarchyResult> {

    @Autowired
    RefBookFactory refBookFactory;

    public GetLastVersionHierarchyHandler() {
        super(GetLastVersionHierarchyAction.class);
    }

    @Override
    public GetLastVersionHierarchyResult execute(GetLastVersionHierarchyAction action, ExecutionContext executionContext) throws ActionException {
        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(action.getRefBookId());

        GetLastVersionHierarchyResult result = new GetLastVersionHierarchyResult();
        RefBook refBook = refBookFactory.get(action.getRefBookId());
        //1000 записей взято с потолка, предполагается что версий много не будет
        PagingResult<Map<String, RefBookValue>> refBookPage = refBookDataProvider
                .getRecordVersionsById(action.getRefBookRecordId(), new PagingParams(0, 1000), null, refBook.getAttributes().get(0));

        if (refBookPage.isEmpty()){
            return new GetLastVersionHierarchyResult();
        }

        //кэшируем список провайдеров для атрибутов-ссылок, чтобы для каждой строки их заново не создавать
        Map<String, RefBookDataProvider> refProviders = new HashMap<String, RefBookDataProvider>();
        Map<String, String> refAliases = new HashMap<String, String>();

        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (attribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
                refProviders.put(attribute.getAlias(), refBookFactory.getDataProvider(attribute.getRefBookId()));
                RefBook refRefBook = refBookFactory.get(attribute.getRefBookId());
                RefBookAttribute refAttribute = refRefBook.getAttribute(attribute.getRefBookAttributeId());
                refAliases.put(attribute.getAlias(), refAttribute.getAlias());
            }
        }

        //Перекладываем атрибуты и добавляем даты. Иначе при каждой перезагрузке формы будут добавляться +2 атрибута
        List<RefBookAttribute> attributes = new ArrayList<RefBookAttribute>();
        attributes.addAll(refBook.getAttributes());
        attributes.add(RefBook.getVersionFromAttribute());
        attributes.add(RefBook.getVersionToAttribute());

        Map<String, RefBookValue> record = refBookPage.get(refBookPage.getTotalCount()-1);
        Map<String, String> tableRowData = new HashMap<String, String>();
        for (RefBookAttribute attribute : attributes) {
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
                        if (value.getDateValue() == null) {
                            tableCell = "";
                        } else {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                            tableCell = dateFormat.format(value.getDateValue());
                        }
                        break;
                    case STRING:
                        if (value.getStringValue() == null) tableCell = "";
                        else tableCell = value.getStringValue();
                        break;
                    case REFERENCE:
                        if (value.getReferenceValue() == null) tableCell = "";
                        else  {
                            Map<String, RefBookValue> refValue = refProviders.get(attribute.getAlias()).getRecordData(value.getReferenceValue());
                            tableCell = refValue.get(refAliases.get(attribute.getAlias())).toString();
                        }
                        break;
                    default:
                        tableCell = "undefined";
                        break;
                }
            }
            tableRowData.put(attribute.getAlias(), tableCell);
        }
        RefBookDataRow tableRow = new RefBookDataRow();

        Long recordId = record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();

        tableRow.setValues(tableRowData);
        tableRow.setRefBookRowId(recordId);

        result.setDataRow(tableRow);
        return result;
    }

    @Override
    public void undo(GetLastVersionHierarchyAction getLastVersionHierarchyAction, GetLastVersionHierarchyResult getLastVersionHierarchyResult, ExecutionContext executionContext) throws ActionException {

    }
}
