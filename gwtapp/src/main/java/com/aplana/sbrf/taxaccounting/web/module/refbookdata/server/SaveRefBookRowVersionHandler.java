package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

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
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
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
		List<Map<String, RefBookValue>> valuesToSaveList = new ArrayList<Map<String, RefBookValue>>();
		RefBookValue id = new RefBookValue(RefBookAttributeType.NUMBER, action.getRecordId());
		valueToSave.put(RefBook.RECORD_ID_ALIAS, id);
		valuesToSaveList.add(valueToSave);

        SaveRefBookRowVersionResult result = new SaveRefBookRowVersionResult();
        Logger logger = new Logger();
		refBookDataProvider.updateRecordVersion(logger, action.getRecordId(), action.getVersionFrom(), action.getVersionTo(), valuesToSaveList);
        result.setUuid(logEntryService.save(logger.getEntries()));
		return result;
	}

	@Override
	public void undo(SaveRefBookRowVersionAction saveRefBookRowAction, SaveRefBookRowVersionResult saveRefBookRowResult, ExecutionContext executionContext) throws ActionException {
	}
}
