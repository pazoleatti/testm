package com.aplana.sbrf.taxaccounting.web.module.members.server;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Component
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_OPER')")
public class PrintHandler  extends AbstractActionHandler<PrintAction, PrintResult> {

	@Autowired
	PrintingService printingService;
	@Autowired
	TAUserService taUserService;
	@Autowired
	BlobDataService blobDataService;

	public PrintHandler() {
		super(PrintAction.class);
	}

	@Override
	public PrintResult execute(PrintAction printAction, ExecutionContext executionContext) throws ActionException {
		String filePath = printingService.generateExcelUsers(taUserService.getByFilter(printAction.getMembersFilterData()));
		try {
			InputStream fileInputStream = new FileInputStream(filePath);

			PrintResult result = new PrintResult();
			result.setUuid(blobDataService.createTemporary(fileInputStream, "Список_пользователей.xlsx"));
			return result;
		} catch (FileNotFoundException e) {
			throw new ServiceException("Проблема при генерации списка пользователей." , e);
		}
	}

	@Override
	public void undo(PrintAction printAction, PrintResult printResult, ExecutionContext executionContext) throws ActionException {}
}
