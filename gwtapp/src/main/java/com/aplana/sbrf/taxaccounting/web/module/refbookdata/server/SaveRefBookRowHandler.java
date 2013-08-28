package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookValueSerializable;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.SaveRefBookRowAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.SaveRefBookRowResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
public class SaveRefBookRowHandler extends AbstractActionHandler<SaveRefBookRowAction, SaveRefBookRowResult> {

	public SaveRefBookRowHandler() {
		super(SaveRefBookRowAction.class);
	}

	@Autowired
	RefBookFactory refBookFactory;

	@Override
	public SaveRefBookRowResult execute(SaveRefBookRowAction action, ExecutionContext executionContext) throws ActionException {
		RefBookDataProvider refBookDataProvider = refBookFactory
				.getDataProvider(action.getRefbookId());
		Map<String, RefBookValue> valueToSave = new HashMap<String, RefBookValue>();

		for(Map.Entry<String, RefBookValueSerializable> v : action.getValueToSave().entrySet()) {
			RefBookValue value = new RefBookValue(v.getValue().getAttributeType(), v.getValue().getValue());
			valueToSave.put(v.getKey(), value);
		}
		List<Map<String, RefBookValue>> valuesToSaveList = new ArrayList<Map<String, RefBookValue>>();
		RefBookValue id = new RefBookValue(RefBookAttributeType.NUMBER, action.getRecordId());
		valueToSave.put(RefBook.RECORD_ID_ALIAS, id);
		valuesToSaveList.add(valueToSave);


		refBookDataProvider.updateRecords(new Date(), valuesToSaveList);
		return new SaveRefBookRowResult();
	}

	@Override
	public void undo(SaveRefBookRowAction saveRefBookRowAction, SaveRefBookRowResult saveRefBookRowResult, ExecutionContext executionContext) throws ActionException {
	}
}
