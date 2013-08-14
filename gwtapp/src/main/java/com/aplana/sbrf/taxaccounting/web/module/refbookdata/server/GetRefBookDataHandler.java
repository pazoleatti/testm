package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookDataAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookDataResult;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookAttribute;
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


		Map<String, RefBookAttribute> resultItems = new HashMap<String, RefBookAttribute>();
		for (Map.Entry<String, RefBookValue> val : record.entrySet()) {
			RefBookAttribute refBookAttribute = new RefBookAttribute();
			refBookAttribute.setAttributeType(val.getValue().getAttributeType());
			switch (val.getValue().getAttributeType()) {
					case NUMBER:
						refBookAttribute.setNumberValue(val.getValue().getNumberValue());
						break;
					case DATE:
						refBookAttribute.setDateValue(val.getValue().getDateValue());
						break;
					case STRING:
						refBookAttribute.setStringValue(val.getValue().getStringValue());
						break;
					case REFERENCE:

						if (val.getValue().getReferenceObject() != null) {
							refBookAttribute.setReferenceValue(val.getValue().getReferenceValue());
							break;
						}
					default:
						refBookAttribute.setStringValue("undefined");
						break;
				}
			resultItems.put(val.getKey(), refBookAttribute);

		}
		GetRefBookDataResult result = new GetRefBookDataResult();
		result.setRecord(resultItems);
		return result;
	}

	@Override
	public void undo(GetRefBookDataAction getRefBookDataAction, GetRefBookDataResult getRefBookDataResult, ExecutionContext executionContext) throws ActionException {
	}
}
