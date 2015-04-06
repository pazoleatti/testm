package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.server;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LoadFormDataService;
import com.aplana.sbrf.taxaccounting.service.LoadRefBookDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared.LoadAllAction;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared.LoadAllResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER','ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class LoadAllHandler extends AbstractActionHandler<LoadAllAction, LoadAllResult> {

    @Autowired
    private LoadFormDataService loadFormDataService;

    @Autowired
    LoadRefBookDataService loadRefBookDataService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    public LoadAllHandler() {
        super(LoadAllAction.class);
    }

    @Override
    public LoadAllResult execute(LoadAllAction action, ExecutionContext context) throws ActionException {
        Logger logger = new Logger();

        TAUserInfo userInfo = securityService.currentUserInfo();

        if (loadRefBookDataService.checkPathArchiveError(securityService.currentUserInfo(), logger)) {
            // Diasoft
            loadRefBookDataService.importRefBookDiasoft(userInfo, logger);

            loadRefBookDataService.importRefBookAvgCost(userInfo, logger);
        }
        // НФ
        loadFormDataService.importFormData(userInfo, loadFormDataService.getTB(userInfo, logger), null, logger);

        LoadAllResult result = new LoadAllResult();
        result.setUuid(logEntryService.save(logger.getEntries()));

        return result;
    }

    @Override
    public void undo(LoadAllAction action, LoadAllResult result, ExecutionContext context) throws ActionException {
    }
}
