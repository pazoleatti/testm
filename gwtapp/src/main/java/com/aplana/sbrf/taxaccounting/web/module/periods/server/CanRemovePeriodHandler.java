package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilter;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CanRemovePeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CanRemovePeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP')")
public class CanRemovePeriodHandler extends AbstractActionHandler<CanRemovePeriodAction, CanRemovePeriodResult> {

	public CanRemovePeriodHandler() {
		super(CanRemovePeriodAction.class);
	}

    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private DeclarationDataSearchService declarationDataSearchService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private DeclarationTemplateService declarationService;
    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private PeriodService periodService;

	@Override
	public CanRemovePeriodResult execute(CanRemovePeriodAction action, ExecutionContext executionContext) throws ActionException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void undo(CanRemovePeriodAction canRemovePeriodAction, CanRemovePeriodResult canRemovePeriodResult, ExecutionContext executionContext) throws ActionException {
	}
}
