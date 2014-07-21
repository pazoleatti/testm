package com.aplana.sbrf.taxaccounting.web.module.refbooklist.server;

import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LoadRefBookDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.LoadRefBookAction;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.LoadRefBookResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;


@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
@Component
public class LoadRefBookHandler extends AbstractActionHandler<LoadRefBookAction, LoadRefBookResult> {

    @Autowired
    private LoadRefBookDataService loadRefBookDataService;
    
    @Autowired
    private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    public LoadRefBookHandler() {
        super(LoadRefBookAction.class);
    }

	@Override
	public LoadRefBookResult execute(LoadRefBookAction arg0,
			ExecutionContext arg1) throws ActionException {
		LoadRefBookResult result = new LoadRefBookResult();
		Logger logger = new Logger();
        // Импорт справочников из ЦАС НСИ
		loadRefBookDataService.importRefBookNsi(securityService.currentUserInfo(), logger);
        // Импорт справочников из Diasoft Custody
        loadRefBookDataService.importRefBookDiasoft(securityService.currentUserInfo(), logger);
        result.setUuid(logEntryService.save(logger.getEntries()));
		return result;
	}

	@Override
	public void undo(LoadRefBookAction arg0, LoadRefBookResult arg1,
			ExecutionContext arg2) throws ActionException {
		// Auto-generated method stub
	}
}
