package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.LoadRefBookDataService;
import com.aplana.sbrf.taxaccounting.service.RegionSecurityService;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    private LoadRefBookDataService loadRefBookDataService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private RegionSecurityService regionSecurityService;

    @Override
    @Transactional
    public AddRefBookRowVersionResult execute(AddRefBookRowVersionAction action, ExecutionContext executionContext) throws ActionException {
        AddRefBookRowVersionResult result = new AddRefBookRowVersionResult();
        TAUser user = securityService.currentUserInfo().getUser();

        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
        for(Map.Entry<String, RefBookValueSerializable> v : action.getRecord().entrySet()) {
            RefBookValue value = new RefBookValue(v.getValue().getAttributeType(), v.getValue().getValue());
            values.put(v.getKey(), value);
        }
        Boolean check = regionSecurityService.check(user, action.getRefBookId(), null,
                action.getRecordId(), values, action.getVersionFrom(), action.getVersionTo());
        if (!check) {
            result.setCheckRegion(false);
            return result;
        }

        RefBookRecord record = new RefBookRecord();
        record.setValues(values);
        record.setRecordId(action.getRecordId());

        Logger logger = new Logger();
        logger.setTaUserInfo(securityService.currentUserInfo());

        // проверка новых значений по БЛ
        loadRefBookDataService.saveRefBookRecords(action.getRefBookId(), null, action.getRecordId(), action.getSourceUniqueRecordId(), Arrays.asList(values), action.getVersionFrom(),
                action.getVersionTo(), true, securityService.currentUserInfo(), logger);

        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(action.getRefBookId());

        logger.setTaUserInfo(securityService.currentUserInfo());
        // По фактц сохраняем по одной записи
        result.setNewIds(refBookDataProvider.createRecordVersion(logger, action.getVersionFrom(), action.getVersionTo(), Arrays.asList(record)));

        loadRefBookDataService.saveRefBookRecords(action.getRefBookId(), result.getNewIds().get(0), action.getRecordId(), action.getSourceUniqueRecordId(), Arrays.asList(values), action.getVersionFrom(),
                action.getVersionTo(), true, securityService.currentUserInfo(), logger);

        refBookDataProvider.updateRecordVersion(logger, result.getNewIds().get(0), action.getVersionFrom(), action.getVersionTo(), values);

        result.setUuid(logEntryService.save(logger.getEntries()));
        result.setCheckRegion(true);

        return result;
    }

    @Override
    public void undo(AddRefBookRowVersionAction action, AddRefBookRowVersionResult result, ExecutionContext executionContext) throws ActionException {
    }
}
