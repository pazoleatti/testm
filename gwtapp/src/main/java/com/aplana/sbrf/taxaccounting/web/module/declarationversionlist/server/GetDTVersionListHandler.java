package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
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

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");

    public GetDTVersionListHandler() {
        super(GetDTVersionListAction.class);
    }

    @Override
    public GetDTVersionListResult execute(GetDTVersionListAction action, ExecutionContext context) throws ActionException {
        GetDTVersionListResult result = new GetDTVersionListResult();
        List<DeclarationTemplate> declarationTemplateList = declarationTemplateService.getDecTemplateVersionsByStatus(action.getDeclarationFormType());
        List<DeclarationTemplateVersion> declarationTemplateVersions = new LinkedList<DeclarationTemplateVersion>();
        for (int i = 0; i < declarationTemplateList.size() - 1; i++){
            DeclarationTemplateVersion decTemplateVersion = new DeclarationTemplateVersion();
            DeclarationTemplate formTemplate = declarationTemplateList.get(i);
            decTemplateVersion.setDtId(String.valueOf(formTemplate.getId()));
            decTemplateVersion.setTypeName(formTemplate.getType().getName());
            decTemplateVersion.setVersionNumber(String.valueOf(formTemplate.getEdition()));
            decTemplateVersion.setActualBeginVersionDate(SDF.format(formTemplate.getVersion()));

            if (declarationTemplateList.get(i + 1).getStatus() == VersionedObjectStatus.FAKE){
                decTemplateVersion.setActualEndVersionDate(declarationTemplateList.get(i + 1).getVersion() != null ?
                        SDF.format(new Date(declarationTemplateList.get(i + 1).getVersion().getTime())) : "");
                i++;
            }else {
                decTemplateVersion.setActualEndVersionDate(declarationTemplateList.get(i + 1).getVersion() != null ?
                        SDF.format(new Date(declarationTemplateList.get(i + 1).getVersion().getTime() - 86400000)) : "");
            }

            declarationTemplateVersions.add(decTemplateVersion);

        }
        if (!declarationTemplateList.isEmpty() && declarationTemplateList.get(declarationTemplateList.size() - 1).getStatus() != VersionedObjectStatus.FAKE){
            DeclarationTemplateVersion decTemplateVersion = new DeclarationTemplateVersion();
            decTemplateVersion.setDtId(String.valueOf(declarationTemplateList.get(declarationTemplateList.size() - 1).getId()));
            decTemplateVersion.setTypeName(declarationTemplateList.get(declarationTemplateList.size() - 1).getType().getName());
            decTemplateVersion.setVersionNumber(String.valueOf(declarationTemplateList.get(declarationTemplateList.size() - 1).getEdition()));
            decTemplateVersion.setActualBeginVersionDate(SDF.format(declarationTemplateList.get(declarationTemplateList.size() - 1).getVersion()));
            declarationTemplateVersions.add(decTemplateVersion);
        }

        result.setTemplateVersions(declarationTemplateVersions);
        return result;
    }

    @Override
    public void undo(GetDTVersionListAction action, GetDTVersionListResult result, ExecutionContext context) throws ActionException {

    }
}
