package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.server;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.FormTypeService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.FormTemplateVersion;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.GetFTVersionListAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.GetFTVersionListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * User: avanteev
 * Date: 2013
 */

@Component
@PreAuthorize("hasRole('ROLE_CONF')")
public class GetFTVersionListHandler extends AbstractActionHandler<GetFTVersionListAction, GetFTVersionListResult> {

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private FormTypeService formTypeService;

    public GetFTVersionListHandler() {
        super(GetFTVersionListAction.class);
    }

    private static final ThreadLocal<SimpleDateFormat> SDF = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Override
    public GetFTVersionListResult execute(GetFTVersionListAction action, ExecutionContext context) throws ActionException {
        GetFTVersionListResult result = new GetFTVersionListResult();
        FormType formType = formTypeService.get(action.getFormTypeId());
        result.setFormTypeName(formType.getName());
        result.setTaxType(formType.getTaxType());

        List<FormTemplate> formTemplates = formTemplateService.getFormTemplateVersionsByStatus(action.getFormTypeId());
        List<FormTemplateVersion> formTemplateVersions = new LinkedList<FormTemplateVersion>();
        for (int i = 0; i < formTemplates.size() - 1; i++){
            FormTemplateVersion formTemplateVersion = new FormTemplateVersion();
            FormTemplate formTemplate = formTemplates.get(i);
            formTemplateVersion.setFormTemplateId(String.valueOf(formTemplate.getId()));
            formTemplateVersion.setTypeName(formTemplate.getName());
            formTemplateVersion.setActualBeginVersionDate(SDF.get().format(formTemplate.getVersion()));
            formTemplateVersion.setActualEndVersionDate(formTemplates.get(i + 1).getVersion() != null?
                    SDF.get().format(new Date(formTemplates.get(i + 1).getVersion().getTime() - AdminConstants.oneDayMilliseconds)):"");
            if (formTemplates.get(i + 1).getStatus() == VersionedObjectStatus.FAKE){
                i++;
            }

            formTemplateVersions.add(formTemplateVersion);

        }
        if (!formTemplates.isEmpty() && formTemplates.get(formTemplates.size() - 1).getStatus() != VersionedObjectStatus.FAKE){
            FormTemplateVersion formTemplateVersion = new FormTemplateVersion();
            formTemplateVersion.setFormTemplateId(String.valueOf(formTemplates.get(formTemplates.size() - 1).getId()));
            formTemplateVersion.setTypeName(formTemplates.get(formTemplates.size() - 1).getName());
            formTemplateVersion.setActualBeginVersionDate(SDF.get().format(formTemplates.get(formTemplates.size() - 1).getVersion()));
            formTemplateVersions.add(formTemplateVersion);
        }

        result.setFormTemplateVersions(formTemplateVersions);
        return result;
    }

    @Override
    public void undo(GetFTVersionListAction action, GetFTVersionListResult result, ExecutionContext context) throws ActionException {

    }
}
