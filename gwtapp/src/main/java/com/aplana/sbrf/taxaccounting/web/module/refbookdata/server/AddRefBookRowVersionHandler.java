package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.RefBookExternalService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.AddRefBookRowVersionAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.AddRefBookRowVersionResult;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookValueSerializable;
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
public class AddRefBookRowVersionHandler extends AbstractActionHandler<AddRefBookRowVersionAction, AddRefBookRowVersionResult> {

    public AddRefBookRowVersionHandler() {
        super(AddRefBookRowVersionAction.class);
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
    public AddRefBookRowVersionResult execute(AddRefBookRowVersionAction action, ExecutionContext executionContext) throws ActionException {
        RefBookDataProvider refBookDataProvider = refBookFactory
                .getDataProvider(action.getRefBookId());


        List<RefBookRecord> records = new ArrayList<RefBookRecord>();
        List<Map<String, RefBookValue>> checkRecords = new ArrayList<Map<String, RefBookValue>>();
        for (Map<String, RefBookValueSerializable> map : action.getRecords()) {
            Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
            for(Map.Entry<String, RefBookValueSerializable> v : map.entrySet()) {
                RefBookValue value = new RefBookValue(v.getValue().getAttributeType(), v.getValue().getValue());
                values.put(v.getKey(), value);
            }
            checkRecords.add(values);
            RefBookRecord record = new RefBookRecord();
            record.setValues(values);
            record.setRecordId(action.getRecordId());
            records.add(record);
        }

        Logger logger = new Logger();

        // проверка новых значений по БЛ
        refBookExternalService.checkRefBook(action.getRefBookId(), checkRecords, action.getVersionFrom(),
                action.getVersionTo(), true, securityService.currentUserInfo(), logger);

        AddRefBookRowVersionResult result = new AddRefBookRowVersionResult();
        result.setNewIds(refBookDataProvider.createRecordVersion(logger, action.getVersionFrom(), action.getVersionTo(), records));
        result.setUuid(logEntryService.save(logger.getEntries()));

        return result;
    }

    @Override
    public void undo(AddRefBookRowVersionAction action, AddRefBookRowVersionResult result, ExecutionContext executionContext) throws ActionException {
    }
}
