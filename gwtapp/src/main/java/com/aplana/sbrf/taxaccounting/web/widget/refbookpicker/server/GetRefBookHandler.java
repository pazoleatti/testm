package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.server;

import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.GetRefBookAction;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.GetRefBookResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;


/**
 * @author sgoryachkin
 *
 */
public class GetRefBookHandler
		extends AbstractActionHandler<GetRefBookAction, GetRefBookResult> {

	public GetRefBookHandler(Class<GetRefBookAction> actionType) {
		super(actionType);
	}

	@Override
	public GetRefBookResult execute(GetRefBookAction action,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void undo(GetRefBookAction action, GetRefBookResult result,
			ExecutionContext context) throws ActionException {
		// TODO Auto-generated method stub
		
	}


}
