package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFormTemplateListAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFormTemplateListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

import java.util.Collections;
import java.util.Comparator;

/**
 * Get all form types.
 *
 * @author Vitalii Samolovskikh
 */
@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class GetFormTemplateListHandler extends AbstractActionHandler<GetFormTemplateListAction, GetFormTemplateListResult> {
	@Autowired
	private FormTemplateService formTemplateService;

    public GetFormTemplateListHandler() {
        super(GetFormTemplateListAction.class);
    }

    @Override
    public GetFormTemplateListResult execute(GetFormTemplateListAction formListAction, ExecutionContext executionContext) throws ActionException {
        GetFormTemplateListResult result = new GetFormTemplateListResult();
        result.setForms(formTemplateService.listAll());
        // Сортировка
        if (result.getForms() != null) {
            Collections.sort(result.getForms(), new Comparator<FormTemplate>() {
                @Override
                public int compare(FormTemplate ft1, FormTemplate ft2) {
                    if (ft1.getName() == null || ft2.getName() == null) {
                        return 0;
                    }
                    return ft1.getName().compareToIgnoreCase(ft2.getName());
                }
            });
        }
        return result;
    }

    @Override
    public void undo(GetFormTemplateListAction formListAction, GetFormTemplateListResult formListResult, ExecutionContext executionContext) throws ActionException {
        // Nothing!!!
    }
}
