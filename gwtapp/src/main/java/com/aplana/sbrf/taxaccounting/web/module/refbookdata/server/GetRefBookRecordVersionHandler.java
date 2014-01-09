package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookRecordVersionAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookRecordVersionResult;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
public class GetRefBookRecordVersionHandler extends AbstractActionHandler<GetRefBookRecordVersionAction, GetRefBookRecordVersionResult> {

    @Autowired
    RefBookFactory refBookFactory;

    public GetRefBookRecordVersionHandler() {
        super(GetRefBookRecordVersionAction.class);
    }

    @Override
    public GetRefBookRecordVersionResult execute(GetRefBookRecordVersionAction action, ExecutionContext executionContext) throws ActionException {
        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(action.getRefBookId());

        GetRefBookRecordVersionResult result = new GetRefBookRecordVersionResult();
        RefBook refBook = refBookFactory.get(action.getRefBookId());
        if (action.getPagingParams() != null) {//TODO перенести в отдельный хэндлер
            PagingResult<Map<String, RefBookValue>> refBookPage = refBookDataProvider
                    .getRecordVersions(action.getRefBookRecordId(), action.getPagingParams(), null, refBook.getAttributes().get(0));
            List<RefBookDataRow> rows = new ArrayList<RefBookDataRow>();

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

            for (Map<String, RefBookValue> record : refBookPage) {
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
                                if (value.getDateValue() == null) tableCell = "";
                                else tableCell = value.getDateValue().toString();
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
                rows.add(tableRow);
            }
            result.setTotalCount(refBookPage.getTotalCount());

            result.setDataRows(rows);
        }
        return result;
    }

    @Override
    public void undo(GetRefBookRecordVersionAction getRefBookRecordVersionAction, GetRefBookRecordVersionResult getRefBookRecordVersionResult, ExecutionContext executionContext) throws ActionException {
        //Do nothing
    }
}
