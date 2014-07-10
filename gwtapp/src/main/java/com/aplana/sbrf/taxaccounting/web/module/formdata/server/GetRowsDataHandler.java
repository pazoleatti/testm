package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetRowsDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetRowsDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;

@Service
public class GetRowsDataHandler extends
		AbstractActionHandler<GetRowsDataAction, GetRowsDataResult> {

	@Autowired
	DataRowService dataRowService;
	@Autowired
	SecurityService securityService;
	@Autowired
	FormTemplateService formTemplateService;

    @Autowired
    RefBookHelper refBookHelper;

    @Autowired
    private LogEntryService logEntryService;

	public GetRowsDataHandler() {
		super(GetRowsDataAction.class);
	}

	@Override
	public GetRowsDataResult execute(GetRowsDataAction action,
			ExecutionContext context) throws ActionException {
		GetRowsDataResult result = new GetRowsDataResult();
		FormTemplate formTemplate = formTemplateService.get(action
				.getFormDataTemplateId());
		boolean fixedRows = formTemplate.isFixedRows();
		TAUserInfo userInfo = securityService.currentUserInfo();
		if (!action.getModifiedRows().isEmpty()) {
			dataRowService.update(userInfo, action.getFormDataId(),
					action.getModifiedRows(), action.isManual());
		}
		DataRowRange dataRowRange;
		if (fixedRows) {
			dataRowRange = new DataRowRange(1, dataRowService.getRowCount(
					userInfo, action.getFormDataId(), action.isReadOnly(), action.isManual()));
		} else {
			dataRowRange = new DataRowRange(action.getRange().getOffset(),
					action.getRange().getLimit());
		}

        PagingResult<DataRow<Cell>> rows = dataRowService.getDataRows(userInfo,
                action.getFormDataId(), dataRowRange, action.isReadOnly(), action.isManual());
        Collections.sort(rows, new Comparator<DataRow<Cell>>() {
            @Override
            public int compare(DataRow<Cell> o1, DataRow<Cell> o2) {
                return o1.getIndex().compareTo(o2.getIndex());
            }
        });
		result.setDataRows(rows);

        Logger logger = new Logger();
        refBookHelper.dataRowsDereference(logger, result.getDataRows(),
                formTemplate.getColumns());
        if (action.getInnerLogUuid() != null && !action.getInnerLogUuid().isEmpty()) {
            result.setUuid(logEntryService.update(logger.getEntries(), action.getInnerLogUuid()));
        } else {
            result.setUuid(logEntryService.save(logger.getEntries()));
        }

		return result;
	}

	@Override
	public void undo(GetRowsDataAction action, GetRowsDataResult result,
			ExecutionContext context) throws ActionException {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}
}
