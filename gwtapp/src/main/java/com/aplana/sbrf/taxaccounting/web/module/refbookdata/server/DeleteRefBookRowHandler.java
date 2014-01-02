package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.DeleteRefBookRowAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.DeleteRefBookRowResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
public class DeleteRefBookRowHandler extends AbstractActionHandler<DeleteRefBookRowAction, DeleteRefBookRowResult> {

	public DeleteRefBookRowHandler() {
		super(DeleteRefBookRowAction.class);
	}

	@Autowired
	RefBookFactory refBookFactory;

	@Override
	public DeleteRefBookRowResult execute(DeleteRefBookRowAction action, ExecutionContext executionContext) throws ActionException {
		RefBookDataProvider refBookDataProvider = refBookFactory
				.getDataProvider(action.getRefBookId());

        if (action.isDeleteVersion()) {
            refBookDataProvider.deleteRecordVersions(action.getRecordsId());
        } else {
            refBookDataProvider.deleteAllRecordVersions(action.getRecordsId());
        }
		return new DeleteRefBookRowResult();
	}

	@Override
	public void undo(DeleteRefBookRowAction deleteRefBookRowAction, DeleteRefBookRowResult deleteRefBookRowResult, ExecutionContext executionContext) throws ActionException {
	}
}
