package com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.server;

import com.aplana.sbrf.taxaccounting.model.BookerStatementsType;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.shared.GetBookerStatementsAction;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.shared.GetBookerStatementsResult;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.HorizontalAlignment;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetBookerStatementsHandler extends AbstractActionHandler<GetBookerStatementsAction, GetBookerStatementsResult> {

    @Autowired
    RefBookFactory rbFactory;

    public static final long REF_BOOK_101 = 50L;
    public static final long REF_BOOK_102 = 52L;

    private static final Map<String, String> columns101;

    static {
        columns101 = new HashMap<String, String>();
        columns101.put("ACCOUNT", "Номер счёта");
        columns101.put("ACCOUNT_NAME", "Название счёта");
        columns101.put("INCOME_DEBET_REMAINS", "Входящие остатки по дебету");
        columns101.put("INCOME_CREDIT_REMAINS", "Входящие остатки по кредиту");
        columns101.put("DEBET_RATE", "Обороты по дебету");
        columns101.put("CREDIT_RATE", "Обороты по кредиту");
        columns101.put("OUTCOME_DEBET_REMAINS", "Исходящие остатки по дебету");
        columns101.put("OUTCOME_CREDIT_REMAINS", "Исходящие остатки по кредиту");
    }

    private static final Map<String, String> columns102;

    static {
        columns102 = new HashMap<String, String>();
        columns102.put("ITEM_NAME", "Наименование статьи");
        columns102.put("OPU_CODE", "Код ОФР");
        columns102.put("TOTAL_SUM", "Сумма");
    }

    public GetBookerStatementsHandler() {
        super(GetBookerStatementsAction.class);
    }

    @Override
    public GetBookerStatementsResult execute(GetBookerStatementsAction action, ExecutionContext context) throws ActionException {
        GetBookerStatementsResult result = new GetBookerStatementsResult();
        RefBookDataProvider provider;
        RefBook refBook;
        List<RefBookDataRow> rows = new ArrayList<RefBookDataRow>();
        List<RefBookColumn> columns = new ArrayList<RefBookColumn>();

        Date version = new Date();
        String notRecord;
        if (action.getStatementsKind() == BookerStatementsType.INCOME101.getId()) {
            notRecord = " AND ACCOUNT != '-1'";
            provider = rbFactory.getDataProvider(REF_BOOK_101);
            refBook = rbFactory.get(REF_BOOK_101);
        } else {
            notRecord = " AND OPU_CODE != '-1'";
            provider = rbFactory.getDataProvider(REF_BOOK_102);
            refBook = rbFactory.get(REF_BOOK_102);
        }
        String filter = "ACCOUNT_PERIOD_ID = " + action.getAccountPeriodId();

        if (action.isNeedOnlyIds()) {
            //Получаем только идентификаторы
            result.setUniqueRecordIds(provider.getUniqueRecordIds(version, filter));
        } else {
            PagingResult<Map<String, RefBookValue>> refBookPage = provider.getRecords(version, action.getPagingParams(), filter + notRecord, null);
            Map<String, RefBookDataProvider> refProviders = new HashMap<String, RefBookDataProvider>();
            Map<String, String> refAliases = new HashMap<String, String>();
            Map<String, String> columnsMap = (action.getStatementsKind() == 0) ? columns101 : columns102;

            for (RefBookAttribute attribute : refBook.getAttributes()) {
                if (!columnsMap.containsKey(attribute.getAlias())) {
                    continue;
                }

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
                col.setName(columnsMap.get(attribute.getAlias()));
                col.setRefBookName(attribute.getRefBookId() == null ? "" : rbFactory.get(attribute.getRefBookId()).getName());
                col.setRefBookAttributeId(attribute.getRefBookAttributeId());
                col.setWidth(attribute.getWidth());
                col.setAlignment(getHorizontalAlignment(attribute));
                col.setRequired(attribute.isRequired());
                columns.add(col);
            }

            for (Map<String, RefBookValue> record : refBookPage) {
                Map<String, String> tableRowData = new HashMap<String, String>();
                for (RefBookAttribute attribute : refBook.getAttributes()) {
                    if (!columnsMap.containsKey(attribute.getAlias())) {
                        continue;
                    }

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
                                else {
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

            List<Long> ids = provider.getUniqueRecordIds(version, filter);
			if (ids.isEmpty()) {
                result.setNotBlank(false);
            }

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
