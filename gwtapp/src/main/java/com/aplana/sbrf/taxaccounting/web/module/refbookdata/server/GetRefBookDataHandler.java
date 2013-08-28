package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookDataAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookDataResult;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookValueSerializable;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
public class GetRefBookDataHandler extends AbstractActionHandler<GetRefBookDataAction, GetRefBookDataResult> {
	public GetRefBookDataHandler() {
		super(GetRefBookDataAction.class);
	}

	@Autowired
	RefBookFactory refBookFactory;

	@Override
	public GetRefBookDataResult execute(GetRefBookDataAction action, ExecutionContext executionContext) throws ActionException {
		RefBookDataProvider refBookDataProvider = refBookFactory
				.getDataProvider(action.getRefbookId());
		Map<String, RefBookValue> record = refBookDataProvider.getRecordData(action.getRecordId());


		Map<String, RefBookValueSerializable> resultItems = new HashMap<String, RefBookValueSerializable>();
		for (Map.Entry<String, RefBookValue> val : record.entrySet()) {
			RefBookValueSerializable refBookAttributeSerializable = new RefBookValueSerializable();
			refBookAttributeSerializable.setAttributeType(val.getValue().getAttributeType());
			switch (val.getValue().getAttributeType()) {
					case NUMBER:
						refBookAttributeSerializable.setNumberValue(val.getValue().getNumberValue());
						break;
					case DATE:
						refBookAttributeSerializable.setDateValue(val.getValue().getDateValue());
						break;
					case STRING:
						refBookAttributeSerializable.setStringValue(val.getValue().getStringValue());
						break;
					case REFERENCE:

						if (val.getValue().getReferenceObject() != null) {
							refBookAttributeSerializable.setReferenceValue(val.getValue().getReferenceValue());
							break;
						}
					default:
						refBookAttributeSerializable.setStringValue("undefined");
						break;
				}
			resultItems.put(val.getKey(), refBookAttributeSerializable);

		}
		GetRefBookDataResult result = new GetRefBookDataResult();
		result.setRecord(resultItems);
		return result;
	}

	@Override
	public void undo(GetRefBookDataAction getRefBookDataAction, GetRefBookDataResult getRefBookDataResult, ExecutionContext executionContext) throws ActionException {
	}
}
