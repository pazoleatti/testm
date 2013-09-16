package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetNameAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetNameResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
public class GetNameHandler extends AbstractActionHandler<GetNameAction, GetNameResult> {

	@Autowired
	RefBookFactory refBookFactory;

	public GetNameHandler() {
		super(GetNameAction.class);
	}

	@Override
	public GetNameResult execute(GetNameAction action, ExecutionContext executionContext) throws ActionException {
		String name = refBookFactory.get(action.getRefBookId()).getName();
		GetNameResult result = new GetNameResult();
		result.setName(name);
		return result;
	}

	@Override
	public void undo(GetNameAction getNameAction, GetNameResult getNameResult, ExecutionContext executionContext) throws ActionException {
	}
}
