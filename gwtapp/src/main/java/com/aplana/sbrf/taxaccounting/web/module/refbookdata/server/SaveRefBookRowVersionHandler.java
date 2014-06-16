package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.RefBookExternalService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookValueSerializable;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.SaveRefBookRowVersionAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.SaveRefBookRowVersionResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private RefBookExternalService refBookExternalService;

    @Autowired
    private SecurityService securityService;

	@Override
	public SaveRefBookRowVersionResult execute(SaveRefBookRowVersionAction action, ExecutionContext executionContext) throws ActionException {
		Map<String, RefBookValue> valueToSave = new HashMap<String, RefBookValue>();
		for(Map.Entry<String, RefBookValueSerializable> v : action.getValueToSave().entrySet()) {
			RefBookValue value = new RefBookValue(v.getValue().getAttributeType(), v.getValue().getValue());
			valueToSave.put(v.getKey(), value);
		}

        Logger logger = new Logger();

        // проверка новых значений по БЛ
        List<Map<String, RefBookValue>> saveRecords = new ArrayList<Map<String, RefBookValue>>();
        saveRecords.add(valueToSave);
        refBookExternalService.saveRefBookRecords(action.getRefBookId(), saveRecords, action.getVersionFrom(),
                action.getVersionTo(), false, securityService.currentUserInfo(), logger);

        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(action.getRefBookId());
        SaveRefBookRowVersionResult result = new SaveRefBookRowVersionResult();
        /*try {
            refBookDataProvider.updateRecordVersion(logger, action.getRecordId(), action.getVersionFrom(), action.getVersionTo(), valueToSave);
        } catch (ServiceLoggerException e) {
            result.setException(true);
        }*/
        logger.setTaUserInfo(securityService.currentUserInfo());
        refBookDataProvider.updateRecordVersion(logger, action.getRecordId(), action.getVersionFrom(), action.getVersionTo(), valueToSave);
        if (logger.containsLevel(LogLevel.ERROR)) {
            result.setException(true);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
		return result;
	}

	@Override
	public void undo(SaveRefBookRowVersionAction saveRefBookRowAction, SaveRefBookRowVersionResult saveRefBookRowResult, ExecutionContext executionContext) throws ActionException {
	}
}
