package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookDataRowAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookDataRowResult;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookTableCell;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
public class GetRefBookDataRowHandler extends AbstractActionHandler<GetRefBookDataRowAction, GetRefBookDataRowResult> {

	@Autowired
	RefBookFactory refBookFactory;

	public GetRefBookDataRowHandler() {
		super(GetRefBookDataRowAction.class);
	}

	@Override
	public GetRefBookDataRowResult execute(GetRefBookDataRowAction action, ExecutionContext executionContext) throws ActionException {

		RefBookDataProvider refBookDataProvider = refBookFactory
				.getDataProvider(action.getRefbookId());
		PagingResult<Map<String, RefBookValue>> refBookPage = refBookDataProvider
				.getRecords(new Date(), null, null, null); //TODO Нужно доделать
		GetRefBookDataRowResult result = new GetRefBookDataRowResult();
		RefBook refBook = refBookFactory.get(action.getRefbookId());

		List<RefBookAttribute> headers = new ArrayList<RefBookAttribute>();
		for (RefBookAttribute attribute : refBook.getAttributes()) {
			headers.add(attribute);
		}
		List<RefBookDataRow> rows = new ArrayList<RefBookDataRow>();
		for (Map<String, RefBookValue> record : refBookPage.getRecords()) {

			Map<String, RefBookTableCell> row = new HashMap<String, RefBookTableCell>();
			for (Map.Entry<String, RefBookValue> val : record.entrySet()) {

				RefBookTableCell cell = new RefBookTableCell();
				cell.setAttributeType(val.getValue().getAttributeType());
				String value;
				switch (val.getValue().getAttributeType()) {
					case NUMBER:
						value = val.getValue().getStringValue();
						break;
					case DATE:
						value = val.getValue().getDateValue().toString();
						break;
					case STRING:
						value = val.getValue().getStringValue();
						break;
					case REFERENCE:
						if (val.getValue().getReferenceObject() != null) {
							value = val.getValue().getReferenceObject().get(val.getValue().getAttributeType()).toString();
							break;
						}
					default:
						value = "undefined";
						break;
				}
				cell.setStringValue(value);
				row.put(val.getKey(), cell);
			}
			RefBookDataRow tableRow = new RefBookDataRow();
			tableRow.setValues(row);
			rows.add(tableRow);

		}
		result.setDataRows(rows);
		result.setTableHeaders(headers);
		return result;
	}

	@Override
	public void undo(GetRefBookDataRowAction getRefBookDataRowAction, GetRefBookDataRowResult getRefBookDataRowResult, ExecutionContext executionContext) throws ActionException {
	}
}
