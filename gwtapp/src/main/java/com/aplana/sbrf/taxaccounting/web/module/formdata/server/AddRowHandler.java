package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.AddRowAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.FormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * @author Vitalii Samolovskikh
 */
@Service
public class AddRowHandler extends AbstractActionHandler<AddRowAction, FormDataResult> {
	public AddRowHandler() {
		super(AddRowAction.class);
	}

	@Override
	public FormDataResult execute(AddRowAction action, ExecutionContext context) throws ActionException {
		FormData formData = action.getFormData();
		formData.appendDataRow();
		FormDataResult result = new FormDataResult();
		result.setFormData(formData);
		result.setLogEntries(new ArrayList<LogEntry>(0));
		return result;
	}

	@Override
	public void undo(AddRowAction action, FormDataResult result, ExecutionContext context) throws ActionException {
		// Nothing!
	}
}
