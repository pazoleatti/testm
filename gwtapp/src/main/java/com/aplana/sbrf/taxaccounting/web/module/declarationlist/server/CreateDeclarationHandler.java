package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationFormKind;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CreateDeclaration;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CreateDeclarationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class CreateDeclarationHandler extends AbstractActionHandler<CreateDeclaration, CreateDeclarationResult> {

    public CreateDeclarationHandler() {
        super(CreateDeclaration.class);
    }

    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private DepartmentService departmentService;

    @Override
    public CreateDeclarationResult execute(CreateDeclaration command, ExecutionContext executionContext) throws ActionException {
        CreateDeclarationResult result = new CreateDeclarationResult();
        Logger logger = new Logger();

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchLast(command.getDepartmentId(),
                command.getReportPeriodId());
        Integer declarationTypeId = command.getDeclarationTypeId();

        if (departmentReportPeriod == null) {
            throw new ActionException("Не удалось определить налоговый период.");
        }

        int activeDeclarationTemplateId = declarationTemplateService.getActiveDeclarationTemplateId(declarationTypeId,
                departmentReportPeriod.getReportPeriod().getId());
        Long declarationId = null;
        try {
            declarationId = declarationDataService.create(logger, activeDeclarationTemplateId,
                    securityService.currentUserInfo(), departmentReportPeriod, null,
                    null, null, null, null, false, null, true);
        } catch (DaoException e) {
            DeclarationTemplate dt = declarationTemplateService.get(activeDeclarationTemplateId);
            if (dt.getDeclarationFormKind().getId() == DeclarationFormKind.CONSOLIDATED.getId()
                    || dt.getDeclarationFormKind().getId() == DeclarationFormKind.PRIMARY.getId()) {
                Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
                String strCorrPeriod = "";
                if (departmentReportPeriod.getCorrectionDate() != null) {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                    strCorrPeriod = ", с датой сдачи корректировки " + formatter.format(departmentReportPeriod.getCorrectionDate());
                }
                logger.error("Налоговая форма с заданными параметрами: Период: \"%s\", Подразделение: \"%s\", " +
                        " Вид налоговой формы: \"%s\" уже существует",
                        departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod,
                        department.getName(), dt.getDeclarationFormKind().getTitle());
            } else throw new ServiceException(e.getMessage());
        }

        result.setDeclarationId(declarationId);
        if (!logger.getEntries().isEmpty()) {
            result.setUuid(logEntryService.save(logger.getEntries()));
        }
        return result;
    }

    @Override
    public void undo(CreateDeclaration createDeclaration, CreateDeclarationResult createDeclarationResult, ExecutionContext executionContext) throws ActionException {
        //Nothing
    }
}
