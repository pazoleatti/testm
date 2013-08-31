package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookTableDataAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookTableDataResult;
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
public class GetRefBookDataRowHandler extends AbstractActionHandler<GetRefBookTableDataAction, GetRefBookTableDataResult> {

	@Autowired
	RefBookFactory refBookFactory;

	public GetRefBookDataRowHandler() {
		super(GetRefBookTableDataAction.class);
	}

	@Override
	public GetRefBookTableDataResult execute(GetRefBookTableDataAction action, ExecutionContext executionContext) throws ActionException {

		RefBookDataProvider refBookDataProvider = refBookFactory
				.getDataProvider(action.getRefbookId());

		GetRefBookTableDataResult result = new GetRefBookTableDataResult();
		RefBook refBook = refBookFactory.get(action.getRefbookId());
		result.setTableHeaders(refBook.getAttributes());
		result.setDesc(refBook.getName());
		if (action.getPagingParams() != null) {//TODO перенести в отдельный хэндлер
			PagingResult<Map<String, RefBookValue>> refBookPage = refBookDataProvider
					.getRecords(new Date(), action.getPagingParams(), null, refBook.getAttributes().get(0));
			List<RefBookDataRow> rows = new ArrayList<RefBookDataRow>();
			for (Map<String, RefBookValue> record : refBookPage) {

				Map<String, String> tableRowData = new HashMap<String, String>();
				for (Map.Entry<String, RefBookValue> val : record.entrySet()) {

					String tableCell;
					if (val.getValue() == null) {
						tableCell = "";
					} else {
						switch (val.getValue().getAttributeType()) {
							case NUMBER:
								if (val.getValue().getNumberValue() == null) tableCell = "";
								else tableCell = val.getValue().getNumberValue().toString();
								break;
							case DATE:
								if (val.getValue().getDateValue() == null) tableCell = "";
								else tableCell = val.getValue().getDateValue().toString();
								break;
							case STRING:
								if (val.getValue().getStringValue() == null) tableCell = "";
								else tableCell = val.getValue().getStringValue();
								break;
							case REFERENCE:
								if (val.getValue().getReferenceValue() == null) tableCell = "";
								else tableCell = refBookDataProvider.getRecordData(val.getValue().getReferenceValue()).values().iterator().next().toString();
								break;
							default:
								tableCell = "undefined";
								break;
						}
					}
					tableRowData.put(val.getKey(), tableCell);
				}
				RefBookDataRow tableRow = new RefBookDataRow();
				tableRow.setValues(tableRowData);
				tableRow.setRefBookRowId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());
				rows.add(tableRow);

			}
			result.setTotalCount(refBookPage.getTotalRecordCount());

			result.setDataRows(rows);
		}
		return result;
	}

	@Override
	public void undo(GetRefBookTableDataAction getRefBookDataRowAction, GetRefBookTableDataResult getRefBookDataRowResult, ExecutionContext executionContext) throws ActionException {
	}
}
