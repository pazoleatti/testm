package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.AddRefBookRowAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.AddRefBookRowResult;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookAttributeSerializable;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
public class AddRefBookRowHandler extends AbstractActionHandler<AddRefBookRowAction, AddRefBookRowResult> {

	public AddRefBookRowHandler() {
		super(AddRefBookRowAction.class);
	}

	@Autowired
	RefBookFactory refBookFactory;

	@Override
	public AddRefBookRowResult execute(AddRefBookRowAction action, ExecutionContext executionContext) throws ActionException {
		RefBookDataProvider refBookDataProvider = refBookFactory
				.getDataProvider(action.getRefbookId());


		List<Map<String, RefBookValue>> valuesToSaveList = new ArrayList<Map<String, RefBookValue>>();
		for (Map<String, RefBookAttributeSerializable> map : action.getRecords()) {
			Map<String, RefBookValue> valueToSave = new HashMap<String, RefBookValue>();
			for(Map.Entry<String, RefBookAttributeSerializable> v : map.entrySet()) {
				RefBookValue value = new RefBookValue(v.getValue().getAttributeType(), v.getValue().getValue());
				valueToSave.put(v.getKey(), value);
			}
			valuesToSaveList.add(valueToSave);
		}

		refBookDataProvider.insertRecords(new Date(), valuesToSaveList);

		AddRefBookRowResult result = new AddRefBookRowResult();
		return result;
	}

	@Override
	public void undo(AddRefBookRowAction addRefBookRowAction, AddRefBookRowResult addRefBookRowResult, ExecutionContext executionContext) throws ActionException {
	}
}
