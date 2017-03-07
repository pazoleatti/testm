package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReferenceColumn;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookTableDataAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookTableDataResult;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerUtils;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
@PreAuthorize("isAuthenticated()")
public class GetRefBookDataRowHandler extends AbstractActionHandler<GetRefBookTableDataAction, GetRefBookTableDataResult> {

    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private RefBookHelper refBookHelper;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private DepartmentService departmentService;


    public GetRefBookDataRowHandler() {
        super(GetRefBookTableDataAction.class);
    }

    @Override
    public GetRefBookTableDataResult execute(GetRefBookTableDataAction action, ExecutionContext executionContext)
            throws ActionException {
        Map<RefBookAttribute, Column> columnMap = new HashMap<RefBookAttribute, Column>();

        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(action.getRefBookId());

        GetRefBookTableDataResult result = new GetRefBookTableDataResult();
        RefBook refBook = refBookFactory.get(action.getRefBookId());
        result.setTableHeaders(refBook.getAttributes());
        result.setDesc(refBook.getName());
        result.setTotalCount(0);
        result.setDataRows(new ArrayList<RefBookDataRow>(0));

        if (action.getPagingParams() == null) {
            return result;
        }

        String filter = null;
        if (filter != null && !filter.isEmpty()) {
            filter = filter + " and " + action.getFilter();
        } else {
            filter = action.getFilter();
        }
        String searchPattern = action.getSearchPattern();
        if (searchPattern != null && !searchPattern.isEmpty()) {
            if (filter != null && !filter.isEmpty()) {
                filter += " and (" + refBookFactory.getSearchQueryStatement(searchPattern, refBook.getId(), action.isExactSearch()) + ")";
            } else {
                filter = refBookFactory.getSearchQueryStatement(searchPattern, refBook.getId(), action.isExactSearch());
            }
        }

        if (action.getRecordId() != null) {
            Long rowNum = refBookDataProvider.getRowNum(action.getRelevanceDate(), action.getRecordId(), filter,
                    refBook.getAttributes().get(0), true);
            result.setRowNum(rowNum);

            /*if (rowNum != null) {
                rowNum = rowNum--;
                int countOfRecords = action.getPagingParams().getCount();
                int startIndex = action.getPagingParams().getStartIndex();
                int page = (int) ((rowNum - 1) / countOfRecords);
                if ((startIndex / countOfRecords) != page) {
                    return result;
                }
            }*/
        }
        if (action.getSortColumnIndex() < 0) {
            action.setSortColumnIndex(0);
        }

        List<RefBookAttribute> refBookAttributeList = new LinkedList<RefBookAttribute>();
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (attribute.isVisible()) {
                refBookAttributeList.add(attribute);
            }
        }

        PagingResult<Map<String, RefBookValue>> refBookPage = refBookDataProvider.getRecords(action.getRelevanceDate(),
                action.getPagingParams(), filter, refBookAttributeList.get(action.getSortColumnIndex()),
                action.isAscSorting());
        List<RefBookDataRow> rows = new LinkedList<RefBookDataRow>();

        dereference(refBook, refBookPage, rows, refBookHelper, columnMap);

        result.setTotalCount(refBookPage.getTotalCount());
        result.setDataRows(rows);
        return result;
    }

    /**
     * Разыменование ссылок
	 * @param refBook справочник, строки которого разыменовываем
	 * @param refBookPage исходные данные для разыменовывания
	 * @param rows пустой список, в который должен быть записан результат
     */
    public static void dereference(RefBook refBook, List<Map<String, RefBookValue>> refBookPage, List<RefBookDataRow> rows, RefBookHelper refBookHelper, Map<RefBookAttribute, Column> columnMap) {
		if (refBookPage.isEmpty()) {
			return;
		}
		List<RefBookAttribute> attributes = refBook.getAttributes();
		// разыменовывание ссылок
		Map<Long, Map<Long, String>> dereferenceValues = refBookHelper.dereferenceValues(refBook, refBookPage, false);

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
                            else {
                                tableCell = getColumn(attribute, columnMap).getFormatter().format(value.getNumberValue().toString());
                            }
                            break;
                        case DATE:
                            if (value.getDateValue() == null) tableCell = "";
                            else {
                                if (attribute.getFormat() != null) {
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                                            attribute.getFormat().getFormat());
                                    tableCell = simpleDateFormat.format(value.getDateValue());
                                } else {
                                    tableCell = value.getDateValue().toString();
                                }
                            }
                            break;
                        case STRING:
                            if (value.getStringValue() == null) tableCell = "";
                            else tableCell = value.getStringValue();
                            break;
                        case REFERENCE:
                            if (value.getReferenceValue() == null) tableCell = "";
                            else {
								Map<Long, String> row = dereferenceValues.get(attribute.getId());
								if (row == null) {
									throw new com.aplana.sbrf.taxaccounting.model.exception.ServiceException("Can't to dereference value");
								}
                                tableCell = getColumn(attribute, columnMap).getFormatter().format(row.get(value.getReferenceValue()));
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
    }

    private static Column getColumn(RefBookAttribute attribute, Map<RefBookAttribute, Column> columnMap) {
        if (columnMap.containsKey(attribute))
            return columnMap.get(attribute);
        switch (attribute.getAttributeType()) {
            case NUMBER:
                NumericColumn numericColumn = new NumericColumn();
                numericColumn.setMaxLength(attribute.getMaxLength());
                numericColumn.setPrecision(attribute.getPrecision());
                columnMap.put(attribute, numericColumn);
                return numericColumn;
            case REFERENCE:
                ReferenceColumn referenceColumn = new ReferenceColumn();
                referenceColumn.setRefBookAttribute(attribute.getRefBookAttribute());
                columnMap.put(attribute, referenceColumn);
                return referenceColumn;
        }
        return null;
    }

    @Override
    public void undo(GetRefBookTableDataAction getRefBookDataRowAction, GetRefBookTableDataResult getRefBookDataRowResult,
                     ExecutionContext executionContext) throws ActionException {
    }
}
