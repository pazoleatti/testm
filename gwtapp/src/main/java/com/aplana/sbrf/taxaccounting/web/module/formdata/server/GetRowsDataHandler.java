package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DataRow.MapEntry;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.RefBookColumn;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetRowsDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetRowsDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
public class GetRowsDataHandler extends
		AbstractActionHandler<GetRowsDataAction, GetRowsDataResult> {

	@Autowired
	DataRowService dataRowService;
	@Autowired
	SecurityService securityService;
	@Autowired
	FormTemplateService formTemplateService;

	@Autowired
	RefBookFactory refBookFactory;

	public GetRowsDataHandler() {
		super(GetRowsDataAction.class);
	}

	@Override
	public GetRowsDataResult execute(GetRowsDataAction action,
			ExecutionContext context) throws ActionException {
		GetRowsDataResult result = new GetRowsDataResult();
		FormTemplate formTemplate = formTemplateService.get(action
				.getFormDataTemplateId());
		boolean fixedRows = formTemplate.isFixedRows();
		TAUserInfo userInfo = securityService.currentUserInfo();
		if (!action.getModifiedRows().isEmpty()) {
			dataRowService.update(userInfo, action.getFormDataId(),
					action.getModifiedRows());
		}
		DataRowRange dataRowRange;
		if (fixedRows) {
			dataRowRange = new DataRowRange(1, dataRowService.getRowCount(
					userInfo, action.getFormDataId(), action.isReadOnly()));
		} else {
			dataRowRange = new DataRowRange(action.getRange().getOffset(),
					action.getRange().getLimit());
		}

		result.setDataRows(dataRowService.getDataRows(userInfo,
				action.getFormDataId(), dataRowRange, action.isReadOnly()));

		refBookDereference(result.getDataRows().getRecords(),
				formTemplate.getColumns());

		return result;
	}

	private void refBookDereference(Collection<DataRow<Cell>> dataRows,
			List<Column> columns) {
		Map<Long, Pair<RefBookDataProvider, RefBookAttribute>> providers = new HashMap<Long, Pair<RefBookDataProvider, RefBookAttribute>>();
		for (DataRow<Cell> dataRow : dataRows) {
			for (Entry<String, Object> entry : dataRow.entrySet()) {
				Cell cell = ((MapEntry<Cell>) entry).getCell();
				Object value = cell.getValue();
				if ((cell.getColumn() instanceof RefBookColumn)	&& value != null) {
					RefBookColumn column = (RefBookColumn) cell.getColumn();
					Pair<RefBookDataProvider, RefBookAttribute> pair = providers
							.get(column.getRefBookAttributeId());
					if (pair == null) {
						RefBook refBook = refBookFactory.getByAttribute(column
								.getRefBookAttributeId());
						RefBookAttribute attribute = refBook
								.getAttribute(column.getRefBookAttributeId());
						RefBookDataProvider provider = refBookFactory
								.getDataProvider(refBook.getId());
						pair = new Pair<RefBookDataProvider, RefBookAttribute>(
								provider, attribute);
						providers.put(column.getRefBookAttributeId(), pair);
					}
					Map<String, RefBookValue> record = pair.getFirst()
							.getRecordData((Long) value);
					RefBookValue refBookValue = record.get(pair.getSecond()
							.getAlias());
					cell.setRefBookDereference(String.valueOf(refBookValue));
				}
			}

		}
	}

	@Override
	public void undo(GetRowsDataAction action, GetRowsDataResult result,
			ExecutionContext context) throws ActionException {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}
}
