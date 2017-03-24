package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.PrepareSpecificReportResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.PrepareSubreportAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.PrepareSubreportResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author lhaziev
 *
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class PrepareSubreportDeclarationHandler extends AbstractActionHandler<PrepareSubreportAction, PrepareSubreportResult> {

    @Autowired
    private SecurityService securityService;
    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private LogEntryService logEntryService;

    public PrepareSubreportDeclarationHandler() {
        super(PrepareSubreportAction.class);
    }

    @Override
    @Transactional
    public PrepareSubreportResult execute(PrepareSubreportAction action, ExecutionContext executionContext) throws ActionException {
        PrepareSubreportResult result = new PrepareSubreportResult();
        if (!declarationDataService.existDeclarationData(action.getDeclarationDataId())) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(action.getDeclarationDataId());
            return result;
        }
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        DeclarationData declarationData = declarationDataService.get(action.getDeclarationDataId(), userInfo);
        DeclarationDataReportType ddReportType = DeclarationDataReportType.getDDReportTypeByName(action.getType());
        ddReportType.setSubreport(declarationTemplateService.getSubreportByAlias(declarationData.getDeclarationTemplateId(), action.getType()));

        Map<String, Object> subreportParamValues = null;
        if (!ddReportType.getSubreport().getDeclarationSubreportParams().isEmpty()) {
            subreportParamValues = action.getSubreportParamValues();
        }

        result.setPrepareSpecificReportResult(declarationDataService.prepareSpecificReport(logger, declarationData, ddReportType, subreportParamValues, userInfo));
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(PrepareSubreportAction action, PrepareSubreportResult result, ExecutionContext executionContext) throws ActionException {
        // Nothing!
    }

}
