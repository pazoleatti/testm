package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
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

//import com.google.gwt.thirdparty.guava.common.base.Function;
//import com.google.gwt.thirdparty.guava.common.collect.Lists;

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
        List<DeclarationType> declarationTypes = declarationTypeService.getByFilter(action.getFilter());
        //List<Integer> ids = Lists.transform(declarationTypes, new Function<DeclarationType, Integer>() {
        //    @Override
        //    public Integer apply(@Nullable DeclarationType formType) {
        //        return formType != null ? formType.getId() : 0;
        //    }
        //});
        //Map<Long, Integer> idsVsCount = declarationTemplateService.versionTemplateCountByFormType(ids);

        List<DeclarationTypeTemplate> typeTemplateList = new ArrayList<DeclarationTypeTemplate>();
        for (DeclarationType type : declarationTypes){
            DeclarationTypeTemplate typeTemplate = new DeclarationTypeTemplate();
            typeTemplate.setTypeId(type.getId());
            typeTemplate.setTypeName(type.getName());
            //typeTemplate.setVersionCount(idsVsCount.containsKey((long) type.getId()) ? idsVsCount.get((long)type.getId()) : 0);

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

