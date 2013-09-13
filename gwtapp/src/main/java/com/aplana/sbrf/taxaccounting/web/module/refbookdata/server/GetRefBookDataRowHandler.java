package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
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

		RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(action.getRefbookId());

		GetRefBookTableDataResult result = new GetRefBookTableDataResult();
		RefBook refBook = refBookFactory.get(action.getRefbookId());
		result.setTableHeaders(refBook.getAttributes());
		result.setDesc(refBook.getName());
		if (action.getPagingParams() != null) {//TODO перенести в отдельный хэндлер
			PagingResult<Map<String, RefBookValue>> refBookPage = refBookDataProvider
					.getRecords(action.getRelevanceDate(), action.getPagingParams(), null, refBook.getAttributes().get(0));
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
			result.setTotalCount(refBookPage.getTotalRecordCount());

			result.setDataRows(rows);
		}
		return result;
	}

	@Override
	public void undo(GetRefBookTableDataAction getRefBookDataRowAction, GetRefBookTableDataResult getRefBookDataRowResult, ExecutionContext executionContext) throws ActionException {
	}
}
