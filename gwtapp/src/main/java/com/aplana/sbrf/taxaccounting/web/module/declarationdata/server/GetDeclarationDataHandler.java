package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetDeclarationDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class GetDeclarationDataHandler
        extends
        AbstractActionHandler<GetDeclarationDataAction, GetDeclarationDataResult> {

    public static final int DEFAULT_IMAGE_RESOLUTION = 150;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DeclarationDataAccessService declarationAccessService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private RefBookFactory rbFactory;

    @Autowired
    private LogBusinessService logBusinessService;

    public GetDeclarationDataHandler() {
        super(GetDeclarationDataAction.class);
    }

    @Override
    public GetDeclarationDataResult execute(GetDeclarationDataAction action,
                                            ExecutionContext context) throws ActionException {
		TAUserInfo userInfo = securityService.currentUserInfo();

        GetDeclarationDataResult result = new GetDeclarationDataResult();
        Set<FormDataEvent> permittedEvents = declarationAccessService.getPermittedEvents(userInfo, action.getId());

        DeclarationData declaration = declarationDataService.get(action.getId(), userInfo);
        Date docDate = declarationDataService.getXmlDataDocDate(action.getId(), userInfo);
        result.setDeclarationData(declaration);
        result.setDocDate(docDate != null ? docDate : new Date());

        result.setCanAccept(permittedEvents.contains(FormDataEvent.MOVE_PREPARED_TO_ACCEPTED));
        result.setCanReject(permittedEvents.contains(FormDataEvent.MOVE_ACCEPTED_TO_CREATED));
        result.setCanDelete(permittedEvents.contains(FormDataEvent.DELETE));
        result.setCanRecalculate(permittedEvents.contains(FormDataEvent.CALCULATE));

        result.setUserLoginImportTf(logBusinessService.getUserLoginImportTf(declaration.getId()));

        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        TaxType taxType = declarationTemplate.getType().getTaxType();
        result.setTaxType(taxType);

        result.setDeclarationType(declarationTemplate.getType().getName());
        result.setDeclarationFormKind(declarationTemplate.getDeclarationFormKind());
        result.setSubreports(declarationTemplate.getSubreports());
        result.setDepartment(departmentService.getParentsHierarchy(
                declaration.getDepartmentId()));
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(
                declaration.getDepartmentReportPeriodId());

        result.setReportPeriod(departmentReportPeriod.getReportPeriod().getName());

        result.setReportPeriodYear(departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear());

        result.setCorrectionDate(departmentReportPeriod.getCorrectionDate());

        result.setFileName(declaration.getFileName());

        if (declaration.getDocState() != null) {
            RefBookDataProvider stateEDProvider = rbFactory.getDataProvider(RefBook.Id.DOC_STATE.getId());
            result.setStateEDName(stateEDProvider.getRecordData(declaration.getDocState()).get("NAME").getStringValue());
        }

        if (declaration.getAsnuId() != null) {
            RefBookDataProvider asnuProvider = rbFactory.getDataProvider(RefBook.Id.ASNU.getId());
            result.setAsnuName(asnuProvider.getRecordData(declaration.getAsnuId()).get("NAME").getStringValue());
        }
        result.setVisiblePDF(declarationDataService.isVisiblePDF(declaration, userInfo));
        //Проверка статуса макета декларации при открытиии экземпляра декларации.
        if (declarationTemplate.getStatus() == VersionedObjectStatus.DRAFT) {
            Logger logger = new Logger();
            if (taxType.equals(taxType.DEAL)) {
                logger.error("Уведомление выведено из действия!");
            } else {
                logger.error("Налоговая форма выведена из действия!");
            }
            result.setUuid(logEntryService.save(logger.getEntries()));
        }

        return result;
    }

    @Override
    public void undo(GetDeclarationDataAction action,
                     GetDeclarationDataResult result, ExecutionContext context)
            throws ActionException {
        // Nothing!
    }
}
