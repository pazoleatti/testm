package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFile;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
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
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Хандлер обрабатывает событие добавления файлов в НФ
 *
 * @author Lhaziev
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class AddDeclarationFilesHandler extends AbstractActionHandler<AddDeclarationFileAction, AddDeclarationFileResult> {

    private final static int DEFAULT_FILE_TYPE_CODE = 6;
    private final static String ATTRIBUTE_NAME = "NAME";

	@Autowired
	private BlobDataService blobDataService;
	@Autowired
	private SecurityService securityService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private DeclarationDataService declarationDataService;

	public AddDeclarationFilesHandler() {
		super(AddDeclarationFileAction.class);
	}

	@Override
	public AddDeclarationFileResult execute(AddDeclarationFileAction action, ExecutionContext context) throws ActionException {
        AddDeclarationFileResult result = new AddDeclarationFileResult();
        if (!declarationDataService.existDeclarationData(action.getDeclarationData().getId())) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(action.getDeclarationData().getId());
            return result;
        }

        RefBookDataProvider provider = refBookFactory.getDataProvider(RefBook.Id.ATTACH_FILE_TYPE.getId());
        Long defaultFileTypeId = provider.getUniqueRecordIds(new Date(), "code = " + DEFAULT_FILE_TYPE_CODE + "").get(0);
        Map<String, RefBookValue> recordData = provider.getRecordData(defaultFileTypeId);

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
            declarationDataFile.setFileTypeId(defaultFileTypeId.intValue());
            declarationDataFile.setFileTypeName(recordData.get(ATTRIBUTE_NAME).getStringValue());
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
