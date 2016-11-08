package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.ScriptStatusHolder;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.AddRowRefBookAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.AddRowRefBookResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("isAuthenticated()")
public class AddRowRefBookHandler extends AbstractActionHandler<AddRowRefBookAction, AddRowRefBookResult> {
    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private RefBookScriptingService refBookScriptingService;

    public AddRowRefBookHandler() {
        super(AddRowRefBookAction.class);
    }

    @Override
    public AddRowRefBookResult execute(AddRowRefBookAction action, ExecutionContext executionContext) throws ActionException {
        AddRowRefBookResult result = new AddRowRefBookResult();
        RefBook refBook = refBookFactory.get(action.getRefBookId());
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();

        Map<String, RefBookValue> record = new HashMap<String, RefBookValue>();
        Map<String, Object> additionalParameters = new HashMap<String, Object>();
        additionalParameters.put("record", record);
        record.put("CODE", new RefBookValue(RefBookAttributeType.NUMBER, new BigDecimal(1L)));
        additionalParameters.put("scriptStatusHolder", new ScriptStatusHolder()); // Статус пока не обрабатывается
        refBookScriptingService.executeScript(userInfo, action.getRefBookId(), FormDataEvent.ADD_ROW, logger, additionalParameters);

        result.setRecord(GetRefBookRecordHandler.convert(refBook, record, refBookFactory));
        return result;
    }

    @Override
    public void undo(AddRowRefBookAction action, AddRowRefBookResult result, ExecutionContext executionContext) throws ActionException {

    }
}
