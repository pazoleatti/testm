package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.FormDataElementName;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFieldsNames;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFieldsNamesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetFieldNamesHandler extends AbstractActionHandler<GetFieldsNames, GetFieldsNamesResult> {

	public GetFieldNamesHandler() {
		super(GetFieldsNames.class);
	}

	@Override
	public GetFieldsNamesResult execute(GetFieldsNames getFieldsNames, ExecutionContext executionContext) throws ActionException {

		Map<FormDataElementName, String> names = new HashMap<FormDataElementName, String>();
		switch (getFieldsNames.getTaxType()) {
			case DEAL:
				names.put(FormDataElementName.HEADER, "Список форм");
				names.put(FormDataElementName.FORM_KIND, "Тип формы");
				names.put(FormDataElementName.FORM_TYPE, "Вид формы");
				names.put(FormDataElementName.REPORT_PERIOD, "Период");
				break;
			case INCOME:
			case PROPERTY:
			case TRANSPORT:
			case VAT:
				names.put(FormDataElementName.HEADER,  "Список налоговых форм");
				names.put(FormDataElementName.FORM_KIND, "Тип налоговой формы");
				names.put(FormDataElementName.FORM_TYPE, "Вид налоговой формы");
				names.put(FormDataElementName.REPORT_PERIOD, "Отчетный период");
				break;
			default:
				break;
		}

		GetFieldsNamesResult result = new GetFieldsNamesResult();
		result.setFieldNames(names);
		return result;
	}

	@Override
	public void undo(GetFieldsNames getFieldsNames, GetFieldsNamesResult getFieldsNamesResult, ExecutionContext executionContext) throws ActionException {}
}
