package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTypeService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationListAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationListResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationTypeTemplate;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class DeclarationListHandler	extends AbstractActionHandler<DeclarationListAction, DeclarationListResult> {
	@Autowired
	private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private DeclarationTypeService declarationTypeService;

	public DeclarationListHandler() {
		super(DeclarationListAction.class);
	}

	@Override
	public DeclarationListResult execute(DeclarationListAction action, ExecutionContext executionContext) throws ActionException {
		DeclarationListResult result = new DeclarationListResult();
		/*result.setDeclarations(declarationTemplateService.getByFilter(action.getFilter()));*/
        List<DeclarationType> declarationTypes = declarationTypeService.listAll();

        List<DeclarationTypeTemplate> typeTemplateList = new ArrayList<DeclarationTypeTemplate>();
        for (DeclarationType type : declarationTypes){
            DeclarationTypeTemplate typeTemplate = new DeclarationTypeTemplate();
            typeTemplate.setTypeId(type.getId());
            typeTemplate.setTypeName(type.getName());
            typeTemplate.setVersionCount(declarationTemplateService.versionTemplateCount(type.getId(), VersionedObjectStatus.NORMAL, VersionedObjectStatus.DRAFT));

            typeTemplateList.add(typeTemplate);
        }

        result.setTypeTemplates(typeTemplateList);
		return result;
	}

	@Override
	public void undo(DeclarationListAction formListAction, DeclarationListResult formListResult, ExecutionContext executionContext) throws ActionException {
		// Nothing!!!
	}
}

