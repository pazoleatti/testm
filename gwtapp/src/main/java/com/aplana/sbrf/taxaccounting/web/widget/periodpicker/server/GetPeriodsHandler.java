package com.aplana.sbrf.taxaccounting.web.widget.periodpicker.server;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.shared.GetPeriodsAction;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.shared.GetPeriodsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * @author sgoryachkin
 * 
 */
@Component
@PreAuthorize("isAuthenticated()")
public class GetPeriodsHandler extends
		AbstractActionHandler<GetPeriodsAction, GetPeriodsResult> {

	public GetPeriodsHandler() {
		super(GetPeriodsAction.class);
	}

	@Override
	public GetPeriodsResult execute(GetPeriodsAction arg0, ExecutionContext arg1)
			throws ActionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void undo(GetPeriodsAction arg0, GetPeriodsResult arg1,
			ExecutionContext arg2) throws ActionException {
		// Auto-generated method stub
		
	}

}
