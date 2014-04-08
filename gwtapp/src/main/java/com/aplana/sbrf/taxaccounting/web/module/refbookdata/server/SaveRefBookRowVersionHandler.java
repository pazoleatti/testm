package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookValueSerializable;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.SaveRefBookRowVersionAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.SaveRefBookRowVersionResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class SaveRefBookRowVersionHandler extends AbstractActionHandler<SaveRefBookRowVersionAction, SaveRefBookRowVersionResult> {

	public SaveRefBookRowVersionHandler() {
		super(SaveRefBookRowVersionAction.class);
	}

	@Autowired
	RefBookFactory refBookFactory;

    @Autowired
    private LogEntryService logEntryService;

	@Override
	public SaveRefBookRowVersionResult execute(SaveRefBookRowVersionAction action, ExecutionContext executionContext) throws ActionException {
		RefBookDataProvider refBookDataProvider = refBookFactory
				.getDataProvider(action.getRefBookId());
		Map<String, RefBookValue> valueToSave = new HashMap<String, RefBookValue>();

		for(Map.Entry<String, RefBookValueSerializable> v : action.getValueToSave().entrySet()) {
			RefBookValue value = new RefBookValue(v.getValue().getAttributeType(), v.getValue().getValue());
			valueToSave.put(v.getKey(), value);
		}

        SaveRefBookRowVersionResult result = new SaveRefBookRowVersionResult();
        Logger logger = new Logger();
        try {
            refBookDataProvider.updateRecordVersion(logger, action.getRecordId(), action.getVersionFrom(), action.getVersionTo(), valueToSave);
        } catch (ServiceLoggerException e) {
            result.setException(true);
        }

        result.setUuid(logEntryService.save(logger.getEntries()));
		return result;
	}

	@Override
	public void undo(SaveRefBookRowVersionAction saveRefBookRowAction, SaveRefBookRowVersionResult saveRefBookRowResult, ExecutionContext executionContext) throws ActionException {
	}
}
