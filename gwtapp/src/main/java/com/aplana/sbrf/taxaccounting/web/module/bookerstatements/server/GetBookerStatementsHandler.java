package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.GetBookerStatementsAction;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.GetBookerStatementsResult;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.HorizontalAlignment;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetBookerStatementsHandler extends AbstractActionHandler<GetBookerStatementsAction, GetBookerStatementsResult> {

    @Autowired
    RefBookFactory rbFactory;

    public static final long REF_BOOK_101 = 50L;
    public static final long REF_BOOK_102 = 52L;

    public GetBookerStatementsHandler() {
        super(GetBookerStatementsAction.class);
    }

    @Override
    public GetBookerStatementsResult execute(GetBookerStatementsAction action, ExecutionContext context) throws ActionException {
        GetBookerStatementsResult result = new GetBookerStatementsResult();
        RefBookDataProvider provider = null;
        RefBook refBook = null;
        List<RefBookDataRow> rows = new ArrayList<RefBookDataRow>();
        List<RefBookColumn> columns = new ArrayList<RefBookColumn>();

        if (action.getStatementsKind() == 0) {
            provider = rbFactory.getDataProvider(REF_BOOK_101);
            refBook = rbFactory.get(REF_BOOK_101);
        } else {
            provider = rbFactory.getDataProvider(REF_BOOK_102);
            refBook = rbFactory.get(REF_BOOK_102);
        }
        String filter = "DEPARTMENT_ID = " + action.getDepartmentId();

        if (action.isNeedOnlyIds()) {
            //Получаем только идентификаторы
            result.setUniqueRecordIds(provider.getUniqueRecordIds(action.getVersion(), filter));
        } else {
            PagingResult<Map<String, RefBookValue>> refBookPage = provider.getRecords(action.getVersion(), action.getPagingParams(), filter, null);
            Map<String, RefBookDataProvider> refProviders = new HashMap<String, RefBookDataProvider>();
            Map<String, String> refAliases = new HashMap<String, String>();

            for (RefBookAttribute attribute : refBook.getAttributes()) {
                if (attribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
                    refProviders.put(attribute.getAlias(), rbFactory.getDataProvider(attribute.getRefBookId()));
                    RefBook refRefBook = rbFactory.get(attribute.getRefBookId());
                    RefBookAttribute refAttribute = refRefBook.getAttribute(attribute.getRefBookAttributeId());
                    refAliases.put(attribute.getAlias(), refAttribute.getAlias());
                }

                RefBookColumn col = new RefBookColumn();
                col.setId(attribute.getId());
                col.setAlias(attribute.getAlias());
                col.setAttributeType(attribute.getAttributeType());
                col.setName(attribute.getName());
                col.setRefBookName(attribute.getRefBookId()==null?"":rbFactory.get(attribute.getRefBookId()).getName());
                col.setRefBookAttributeId(attribute.getRefBookAttributeId());
                col.setWidth(attribute.getWidth());
                col.setAlignment(getHorizontalAlignment(attribute));
                col.setRequired(attribute.isRequired());
                columns.add(col);
            }

            for (Map<String, RefBookValue> record : refBookPage) {
                Map<String, String> tableRowData = new HashMap<String, String>();
                for (RefBookAttribute attribute : refBook.getAttributes()) {
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
                tableRow.setValues(tableRowData);
                tableRow.setRefBookRowId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());
                rows.add(tableRow);
            }

            result.setColumns(columns);
            result.setTotalCount(refBookPage.getTotalCount());

            result.setDataRows(rows);
        }
        return result;
    }

    private HorizontalAlignment getHorizontalAlignment(RefBookAttribute attribute) {
        HorizontalAlignment alignment;
        switch (attribute.getAttributeType()) {
            case NUMBER:
                alignment = HorizontalAlignment.ALIGN_RIGHT;
                break;
            case STRING:
                alignment = HorizontalAlignment.ALIGN_LEFT;
                break;
            case DATE:
                alignment = HorizontalAlignment.ALIGN_CENTER;
                break;
            case REFERENCE:
                RefBook refBook = rbFactory.get(attribute.getRefBookId());
                RefBookAttribute refAttr = refBook.getAttribute(attribute.getRefBookAttributeId());
                alignment = getHorizontalAlignment(refAttr);
                break;
            default:
                alignment = HorizontalAlignment.ALIGN_LEFT;
                break;
        }
        return alignment;
    }

    @Override
    public void undo(GetBookerStatementsAction action, GetBookerStatementsResult result, ExecutionContext context) throws ActionException {
        //do nothing
    }
}
