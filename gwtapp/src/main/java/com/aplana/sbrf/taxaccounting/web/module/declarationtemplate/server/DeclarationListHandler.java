package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTypeService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationListAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationListResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationTypeTemplate;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class DeclarationListHandler	extends AbstractActionHandler<DeclarationListAction, DeclarationListResult> {
	@Autowired
	private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private DeclarationTypeService declarationTypeService;
    @Autowired
    private SecurityService securityService;

	public DeclarationListHandler() {
		super(DeclarationListAction.class);
	}

	@Override
	public DeclarationListResult execute(DeclarationListAction action, ExecutionContext executionContext) throws ActionException {
		DeclarationListResult result = new DeclarationListResult();
        TAUser user = securityService.currentUserInfo().getUser();
        if (action.getFilter().getTaxType() == null) {
            if (!user.hasRole(TARole.N_ROLE_CONF)) {
                action.getFilter().setTaxType(TaxType.PFR);
            } else if (!user.hasRole(TARole.F_ROLE_CONF)) {
                action.getFilter().setTaxType(TaxType.NDFL);
            }
        }
        List<DeclarationType> declarationTypes = declarationTypeService.getByFilter(action.getFilter());
        @SuppressWarnings("unchecked")
        Collection<Integer> ids = CollectionUtils.collect(declarationTypes, new Transformer() {
            @Override
            public Object transform(Object o) {
                return ((DeclarationType)o).getId();
            }
        });
        Map<Long, Integer> idsVsCount = declarationTemplateService.versionTemplateCountByFormType(ids);

        List<DeclarationTypeTemplate> typeTemplateList = new ArrayList<DeclarationTypeTemplate>();
        for (DeclarationType type : declarationTypes){
            DeclarationTypeTemplate typeTemplate = new DeclarationTypeTemplate();
            typeTemplate.setTypeId(type.getId());
            typeTemplate.setTypeName(type.getName());
            typeTemplate.setVersionCount(idsVsCount.containsKey((long) type.getId()) ? idsVsCount.get((long)type.getId()) : 0);

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

