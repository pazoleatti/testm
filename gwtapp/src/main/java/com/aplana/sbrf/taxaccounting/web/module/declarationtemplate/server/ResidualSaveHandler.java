package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.ResidualSaveAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.ResidualSaveResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class ResidualSaveHandler extends AbstractActionHandler<ResidualSaveAction, ResidualSaveResult> {

    @Autowired
    @Qualifier("declarationTemplateMainOperatingService")
    private MainOperatingService mainOperatingService;
    @Autowired
	private DeclarationTemplateImpexService declarationTemplateImpexService;
    @Autowired
	private SecurityService securityService;
    @Autowired
	private BlobDataService blobDataService;
    @Autowired
	private DeclarationTemplateService declarationTemplateService;
    @Autowired
	private LogEntryService logEntryService;

    public ResidualSaveHandler() {
        super(ResidualSaveAction.class);
    }

    @Override
    public ResidualSaveResult execute(ResidualSaveAction action, ExecutionContext context) throws ActionException {
        Logger logger = new Logger();
        ResidualSaveResult result = new ResidualSaveResult();
        if (action.isArchive()){
            BlobData data = blobDataService.get(action.getUploadUuid());
            DeclarationTemplate declarationTemplate = declarationTemplateImpexService.importDeclarationTemplate
                    (securityService.currentUserInfo(), action.getDtId(), data.getInputStream());
            Date endDate = declarationTemplateService.getDTEndDate(action.getDtId());
            mainOperatingService.edit(declarationTemplate, endDate, logger, securityService.currentUserInfo());
            result.setUploadUuid(declarationTemplate.getJrxmlBlobId());
        } else {
            DeclarationTemplate declarationTemplate = declarationTemplateService.get(action.getDtId());
            declarationTemplate.setCreateScript(declarationTemplateService.getDeclarationTemplateScript((action.getDtId())));
            declarationTemplate.setJrxmlBlobId(action.getUploadUuid());
            declarationTemplateService.save(declarationTemplate);
            result.setUploadUuid(declarationTemplate.getJrxmlBlobId());
        }

        result.setSuccessUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(ResidualSaveAction action, ResidualSaveResult result, ExecutionContext context) throws ActionException {

    }
}
