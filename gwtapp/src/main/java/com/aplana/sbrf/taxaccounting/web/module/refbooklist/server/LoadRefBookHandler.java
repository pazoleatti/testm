package com.aplana.sbrf.taxaccounting.web.module.refbooklist.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.RefBookExternalService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.LoadRefBookAction;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.LoadRefBookResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;


@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
@Component
public class LoadRefBookHandler extends AbstractActionHandler<LoadRefBookAction, LoadRefBookResult> {

    @Autowired
    RefBookExternalService refBookExternalService;
    
    @Autowired
    private SecurityService securityService;

    public LoadRefBookHandler() {
        super(LoadRefBookAction.class);
    }

	@Override
	public LoadRefBookResult execute(LoadRefBookAction arg0,
			ExecutionContext arg1) throws ActionException {
		LoadRefBookResult result = new LoadRefBookResult();
		Logger logger = new Logger();
		refBookExternalService.importRefBook(securityService.currentUserInfo(), logger);
		result.setEntries(logger.getEntries());
		return result;
	}

	@Override
	public void undo(LoadRefBookAction arg0, LoadRefBookResult arg1,
			ExecutionContext arg2) throws ActionException {
		// Auto-generated method stub
		
	}


}
