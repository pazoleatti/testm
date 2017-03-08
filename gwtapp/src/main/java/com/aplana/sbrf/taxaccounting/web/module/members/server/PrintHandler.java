package com.aplana.sbrf.taxaccounting.web.module.members.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserView;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.module.members.shared.PrintAction;
import com.aplana.sbrf.taxaccounting.web.module.members.shared.PrintResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class PrintHandler  extends AbstractActionHandler<PrintAction, PrintResult> {

	@Autowired
	PrintingService printingService;
	@Autowired
	TAUserService taUserService;
	@Autowired
	BlobDataService blobDataService;
	@Autowired
	DepartmentService departmentService;

	public PrintHandler() {
		super(PrintAction.class);
	}

	@Override
	public PrintResult execute(PrintAction printAction, ExecutionContext executionContext) throws ActionException {

        PagingResult<TAUserView> usersByFilter = taUserService.getUsersByFilter(printAction.getMembersFilterData());

        PrintResult result = new PrintResult();
        result.setUuid(printingService.generateExcelUsers(usersByFilter));
        return result;
    }

	@Override
	public void undo(PrintAction printAction, PrintResult printResult, ExecutionContext executionContext) throws ActionException {}
}
