package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFile;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.AddDeclarationFileAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.AddDeclarationFileResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Хандлер обрабатывает событие добавления файлов в НФ
 *
 * @author Lhaziev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class AddDeclarationFilesHandler extends
		AbstractActionHandler<AddDeclarationFileAction, AddDeclarationFileResult> {

	@Autowired
	private BlobDataService blobDataService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private DepartmentService departmentService;

	public AddDeclarationFilesHandler() {
		super(AddDeclarationFileAction.class);
	}

	@Override
	public AddDeclarationFileResult execute(AddDeclarationFileAction action, ExecutionContext context)
		throws ActionException {
        AddDeclarationFileResult result = new AddDeclarationFileResult();
        TAUser user = securityService.currentUserInfo().getUser();
        String userName = user.getName();
        String userDepartmentName = departmentService.getParentsHierarchyShortNames(user.getDepartmentId());
        List<DeclarationDataFile> files = new ArrayList<DeclarationDataFile>();
        for(String uuid: action.getUuid().split(",")) {
            BlobData blobData = blobDataService.get(uuid);
            DeclarationDataFile declarationDataFile = new DeclarationDataFile();
            declarationDataFile.setUuid(uuid);
            declarationDataFile.setFileName(blobData.getName());
            declarationDataFile.setDate(blobData.getCreationDate());
            declarationDataFile.setUserName(userName);
            declarationDataFile.setUserDepartmentName(userDepartmentName);
            declarationDataFile.setNote("");
            files.add(declarationDataFile);
        }
        result.setFiles(files);
		return result;
	}

	@Override
	public void undo(AddDeclarationFileAction action, AddDeclarationFileResult result,
			ExecutionContext context) throws ActionException {
		// Nothing!
	}
}
