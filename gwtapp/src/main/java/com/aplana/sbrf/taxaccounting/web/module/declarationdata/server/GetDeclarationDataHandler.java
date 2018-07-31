package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationFormKind;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
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

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
public class GetDeclarationDataHandler
        extends
        AbstractActionHandler<GetDeclarationDataAction, GetDeclarationDataResult> {

    public static final int DEFAULT_IMAGE_RESOLUTION = 150;

    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private DepartmentService departmentService;
    //@Autowired
    //private DeclarationDataAccessService declarationAccessService;
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
    @Autowired
    private TAUserService userService;

    public GetDeclarationDataHandler() {
        super(GetDeclarationDataAction.class);
    }

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        }
    };

    @Override
    public GetDeclarationDataResult execute(GetDeclarationDataAction action,
                                            ExecutionContext context) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();

        GetDeclarationDataResult result = new GetDeclarationDataResult();
        if (!declarationDataService.existDeclarationData(action.getId())) {
            throw new ServiceLoggerException(String.format(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, action.getId()), null);
        }
        //Set<FormDataEvent> permittedEvents = declarationAccessService.getPermittedEvents(userInfo, action.getId());

        DeclarationData declaration = declarationDataService.get(action.getId(), userInfo);
        Date docDate = declarationDataService.getXmlDataDocDate(action.getId(), userInfo);
        result.setDeclarationData(declaration);
        result.setDocDate(docDate != null ? docDate : new Date());

        /*result.setCanCheck(permittedEvents.contains(FormDataEvent.CHECK));
        result.setCanAccept(permittedEvents.contains(FormDataEvent.MOVE_PREPARED_TO_ACCEPTED));
        result.setCanReject(permittedEvents.contains(FormDataEvent.MOVE_ACCEPTED_TO_CREATED));
        result.setCanDelete(permittedEvents.contains(FormDataEvent.DELETE));
        result.setCanRecalculate(permittedEvents.contains(FormDataEvent.CALCULATE));
        result.setCanChangeStatusED(permittedEvents.contains(FormDataEvent.CHANGE_STATUS_ED));*/

        String userLogin = logBusinessService.getFormCreationUserName(declaration.getId());
        if (userLogin != null && !userLogin.isEmpty()) {
            result.setCreationUserName(userService.getUser(userLogin).getName());
        }
        result.setCreationDate(sdf.get().format(logBusinessService.getFormCreationDate(declaration.getId())));

        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        TaxType taxType = TaxType.NDFL;
        result.setTaxType(taxType);

        result.setDeclarationType(declarationTemplate.getType().getName());
        result.setDeclarationFormKind(declarationTemplate.getDeclarationFormKind());
        result.setShowPrintToXml(!declarationTemplate.getDeclarationFormKind().equals(DeclarationFormKind.CONSOLIDATED));
        result.setSubreports(declarationTemplate.getSubreports());
        result.setDepartment(departmentService.getParentsHierarchy(
                declaration.getDepartmentId()));
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(
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
