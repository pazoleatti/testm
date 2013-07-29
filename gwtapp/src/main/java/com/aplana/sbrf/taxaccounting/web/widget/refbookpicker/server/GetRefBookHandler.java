package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.GetRefBookAction;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.GetRefBookResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * @author sgoryachkin
 * 
 */
@Component
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetRefBookHandler extends
		AbstractActionHandler<GetRefBookAction, GetRefBookResult> {

	@Autowired
	RefBookFactory refBookFactory;

	public GetRefBookHandler() {
		super(GetRefBookAction.class);
	}

	@Override
	public GetRefBookResult execute(GetRefBookAction action,
			ExecutionContext context) throws ActionException {
		GetRefBookResult result = new GetRefBookResult();
		List<String> headers = new ArrayList<String>();

		RefBook refBook = null;//refBookFactory.get(action.getRefBookId());
		for (RefBookAttribute refBookAttribute : refBook.getAttributes()) {
			if (!RefBook.RECORD_ID_ALIAS.equals(refBookAttribute.getAlias())) {
				headers.add(refBookAttribute.getName());
			}
		}
		result.setHeaders(headers);
		
		List<Date> versions = new ArrayList<Date>();
		if (action.getFormDataId()!=null){
			// TODO: Получить список версий для периода formData
		} else {
			versions.add(new Date());
		}
		result.setVersions(versions);
		
		return result;
	}

	@Override
	public void undo(GetRefBookAction action, GetRefBookResult result,
			ExecutionContext context) throws ActionException {
		//
	}

}
