package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTypeService;
import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared.DeclarationTemplateVersion;
import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared.GetDTVersionListAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared.GetDTVersionListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class GetDTVersionListHandler extends AbstractActionHandler<GetDTVersionListAction, GetDTVersionListResult> {

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private DeclarationTypeService declarationTypeService;

    private static final ThreadLocal<SimpleDateFormat> SDF = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    public GetDTVersionListHandler() {
        super(GetDTVersionListAction.class);
    }

    @Override
    public GetDTVersionListResult execute(GetDTVersionListAction action, ExecutionContext context) throws ActionException {
        GetDTVersionListResult result = new GetDTVersionListResult();
        DeclarationType dType = declarationTypeService.get(action.getDeclarationFormTypeId());
        result.setDtTypeName(dType.getName());
        result.setTaxType(dType.getTaxType());


        List<DeclarationTemplate> declarationTemplateList = declarationTemplateService.getDecTemplateVersionsByStatus(action.getDeclarationFormTypeId());
        List<DeclarationTemplateVersion> declarationTemplateVersions = new LinkedList<DeclarationTemplateVersion>();
        for (int i = 0; i < declarationTemplateList.size() - 1; i++){
            DeclarationTemplateVersion decTemplateVersion = new DeclarationTemplateVersion();
            DeclarationTemplate declarationTemplate = declarationTemplateList.get(i);
            decTemplateVersion.setDtId(String.valueOf(declarationTemplate.getId()));
            decTemplateVersion.setTypeName(declarationTemplate.getName());
            decTemplateVersion.setActualBeginVersionDate(SDF.get().format(declarationTemplate.getVersion()));
            decTemplateVersion.setActualEndVersionDate(declarationTemplateList.get(i + 1).getVersion() != null ?
                    SDF.get().format(new Date(declarationTemplateList.get(i + 1).getVersion().getTime() - 86400000)) : "");

            if (declarationTemplateList.get(i + 1).getStatus() == VersionedObjectStatus.FAKE){
                i++;
            }
            declarationTemplateVersions.add(decTemplateVersion);

        }
        if (!declarationTemplateList.isEmpty() && declarationTemplateList.get(declarationTemplateList.size() - 1).getStatus() != VersionedObjectStatus.FAKE){
            DeclarationTemplateVersion decTemplateVersion = new DeclarationTemplateVersion();
            decTemplateVersion.setDtId(String.valueOf(declarationTemplateList.get(declarationTemplateList.size() - 1).getId()));
            decTemplateVersion.setTypeName(declarationTemplateList.get(declarationTemplateList.size() - 1).getName());
            decTemplateVersion.setActualBeginVersionDate(SDF.get().format(declarationTemplateList.get(declarationTemplateList.size() - 1).getVersion()));
            declarationTemplateVersions.add(decTemplateVersion);
        }

        result.setTemplateVersions(declarationTemplateVersions);
        return result;
    }

    @Override
    public void undo(GetDTVersionListAction action, GetDTVersionListResult result, ExecutionContext context) throws ActionException {

    }
}
