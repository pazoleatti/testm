package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.FormTypeService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.FormTypeTemplate;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFormTemplateListAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFormTemplateListResult;
import com.google.gwt.thirdparty.guava.common.base.Function;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.*;

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

    @Autowired
    private FormTypeService formTypeService;

    public GetFormTemplateListHandler() {
        super(GetFormTemplateListAction.class);
    }

    @Override
    public GetFormTemplateListResult execute(GetFormTemplateListAction formListAction, ExecutionContext executionContext) throws ActionException {
        GetFormTemplateListResult result = new GetFormTemplateListResult();

        List<FormType> formTypes = formTypeService.getByFilter(formListAction.getFilter());
        List<FormTypeTemplate> formTypeTemplates = new ArrayList<FormTypeTemplate>();
        List<Integer> ids = Lists.transform(formTypes, new Function<FormType, Integer>() {
            @Override
            public Integer apply(@Nullable FormType formType) {
                return formType != null ? formType.getId() : 0;
            }
        });
        Map<Long, Integer> idsVsCount = formTemplateService.versionTemplateCountByFormType(ids);

        for (FormType type : formTypes){
            FormTypeTemplate typeTemplate = new FormTypeTemplate();
            typeTemplate.setTaxType(type.getTaxType());
            typeTemplate.setFormTypeId(type.getId());
            typeTemplate.setFormTypeName(type.getName());
            typeTemplate.setVersionCount(idsVsCount.containsKey((long) type.getId()) ? idsVsCount.get((long)type.getId()) : 0);

            formTypeTemplates.add(typeTemplate);
        }
        result.setFormTypeTemplates(formTypeTemplates);
        // Сортировка
        if (result.getFormTypeTemplates() != null) {
            Collections.sort(result.getFormTypeTemplates(), new Comparator<FormTypeTemplate>() {
                @Override
                public int compare(FormTypeTemplate ft1, FormTypeTemplate ft2) {
                    if (ft1.getFormTypeName() == null || ft2.getFormTypeName() == null) {
                        return 0;
                    }
                    return ft1.getFormTypeName().compareToIgnoreCase(ft2.getFormTypeName());
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
