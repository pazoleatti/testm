package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookRecordAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookRecordResult;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookValueSerializable;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Comp-1
 * Date: 28.08.13
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
public class GetRefBookRecordHandler extends AbstractActionHandler<GetRefBookRecordAction, GetRefBookRecordResult> {

	@Autowired
	RefBookFactory refBookFactory;

	public GetRefBookRecordHandler() {
		super(GetRefBookRecordAction.class);
	}

	@Override
	public GetRefBookRecordResult execute(GetRefBookRecordAction action, ExecutionContext executionContext) throws ActionException {
		RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(action.getRefBookDataId());
		Map<String, RefBookValue> record = refBookDataProvider.getRecordData(action.getRefBookRecordId());

		GetRefBookRecordResult result = new GetRefBookRecordResult();
		result.setRecord(convert(record));
		return result;
	}

	private Map<String, RefBookValueSerializable> convert(Map<String, RefBookValue> record) {
		Map<String, RefBookValueSerializable> convertedRecord = new HashMap<String, RefBookValueSerializable>();
		for (Map.Entry<String, RefBookValue> attr : record.entrySet()) {
			RefBookValueSerializable serializedValue = new RefBookValueSerializable();
			serializedValue.setAttributeType(attr.getValue().getAttributeType());
			switch (attr.getValue().getAttributeType()) {
				case NUMBER:
					serializedValue.setNumberValue(attr.getValue().getNumberValue());
					break;
				case STRING:
					serializedValue.setStringValue(attr.getValue().getStringValue());
					break;
				case DATE:
					serializedValue.setDateValue(attr.getValue().getDateValue());
					break;
				case REFERENCE:
					serializedValue.setReferenceValue(attr.getValue().getReferenceValue());
					break;
				default:
					serializedValue.setStringValue("");
					break;
			}
			convertedRecord.put(attr.getKey(), serializedValue);
		}

		return convertedRecord;
	}

	@Override
	public void undo(GetRefBookRecordAction getRefBookRecordAction, GetRefBookRecordResult getRefBookRecordResult, ExecutionContext executionContext) throws ActionException {
	}
}
