package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.FormDataFile;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.AddFileAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.AddFileResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Хандлер обрабатывает событие добавления файлов в НФ
 *
 * @author Lhaziev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class AddFilesHandler extends
		AbstractActionHandler<AddFileAction, AddFileResult> {

	@Autowired
	private BlobDataService blobDataService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private DepartmentService departmentService;

	public AddFilesHandler() {
		super(AddFileAction.class);
	}

	@Override
	public AddFileResult execute(AddFileAction action, ExecutionContext context)
		throws ActionException {
        AddFileResult result = new AddFileResult();
        BlobData blobData = blobDataService.get(action.getUuid());
        TAUser user = securityService.currentUserInfo().getUser();
        FormDataFile formDataFile = new FormDataFile();
        //formDataFile.setFormDataId(action.getFormData().getId());
        formDataFile.setUuid(action.getUuid());
        formDataFile.setFileName(blobData.getName());
        formDataFile.setDate(blobData.getCreationDate());
        formDataFile.setUserName(user.getName());
        formDataFile.setUserDepartmentName(departmentService.getDepartment(user.getDepartmentId()).getName());
        formDataFile.setNote("");
        result.setFile(formDataFile);
		return result;
	}

	@Override
	public void undo(AddFileAction action, AddFileResult result,
			ExecutionContext context) throws ActionException {
		// Nothing!
	}
}
