package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookAttributesAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookAttributesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
public class GetRefBookAttributesHandler extends AbstractActionHandler<GetRefBookAttributesAction, GetRefBookAttributesResult> {

	@Autowired
	RefBookFactory refBookFactory;

	public GetRefBookAttributesHandler() {
		super(GetRefBookAttributesAction.class);
	}

	@Override
	public GetRefBookAttributesResult execute(GetRefBookAttributesAction action, ExecutionContext executionContext) throws ActionException {
		RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(action.getRefBookId());

		List<RefBookAttribute> attributes = refBookFactory.get(action.getRefBookId()).getAttributes();
		GetRefBookAttributesResult result = new GetRefBookAttributesResult();
		result.setAttributes(attributes);
		return result;
	}

	@Override
	public void undo(GetRefBookAttributesAction getRefBookAttributesAction, GetRefBookAttributesResult getRefBookAttributesResult,
	                 ExecutionContext executionContext) throws ActionException {
	}
}
