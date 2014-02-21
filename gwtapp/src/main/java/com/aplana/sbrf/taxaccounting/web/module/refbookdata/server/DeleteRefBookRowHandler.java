package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
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

    @Autowired
    private LogEntryService logEntryService;

	@Override
	public DeleteRefBookRowResult execute(DeleteRefBookRowAction action, ExecutionContext executionContext) throws ActionException {
		RefBookDataProvider refBookDataProvider = refBookFactory
				.getDataProvider(action.getRefBookId());

        DeleteRefBookRowResult result = new DeleteRefBookRowResult();
        Logger logger = new Logger();
        if (action.getRecordsId().size() > 0) {
            if (action.isDeleteVersion()) {
                Long nextVersion = refBookDataProvider.getFirstRecordId(action.getRecordsId().get(0));
                refBookDataProvider.deleteRecordVersions(logger, action.getRecordsId());
                result.setNextVersion(nextVersion);
            } else {
                try {
                    refBookDataProvider.deleteAllRecords(logger, action.getRecordsId());
                } catch (ServiceLoggerException e) {
                    result.setException(true);
                }
            }
            result.setUuid(logEntryService.save(logger.getEntries()));
        }
		return result;
	}

	@Override
	public void undo(DeleteRefBookRowAction deleteRefBookRowAction, DeleteRefBookRowResult deleteRefBookRowResult, ExecutionContext executionContext) throws ActionException {
	}
}
