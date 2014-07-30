package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetCheckDeclarationAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetCheckDeclarationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author Lenar Haziev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetCheckDeclarationHandler extends AbstractActionHandler<GetCheckDeclarationAction, GetCheckDeclarationResult> {

    private static final String FORM_WARN = "В налоговой форме \"%s\" в подразделении \"%s\" периоде \"%s\" используется старая версия настроек.";
    private static final String FORM_WARN_S = "В форме \"%s\" в подразделении \"%s\" периоде \"%s\" используется старая версия настроек.";

    private static final String DECLARATION_WARN = "В декларации \"%s\" в подразделении \"%s\" периоде \"%s\" используется старая версия настроек.";
    private static final String DECLARATION_WARN_D = "В уведомлении \"%s\" в подразделении \"%s\" периоде \"%s\" используется старая версия настроек.";

    @Autowired
    private TAUserService userService;

    @Autowired
    private FormDataSearchService formDataSearchService;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private DeclarationDataSearchService declarationDataSearchService;

    @Autowired
    SourceService departmentFormTypService;

    @Autowired
    DataRowService dataRowService;

    @Autowired
    PeriodService reportService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    DepartmentService departmentService;

    public GetCheckDeclarationHandler() {
        super(GetCheckDeclarationAction.class);
    }

    @Override
    public GetCheckDeclarationResult execute(GetCheckDeclarationAction action, ExecutionContext executionContext) throws ActionException {
        GetCheckDeclarationResult result = new GetCheckDeclarationResult();
        Logger logger = new Logger();

        ReportPeriod period = reportService.getReportPeriod(action.getReportPeriodId());
        String periodName = period.getName() + " " + period.getTaxPeriod().getYear();
        String departmentName = departmentService.getDepartment(action.getDepartment()).getName();

        DeclarationDataFilter declarationDataFilter = new DeclarationDataFilter();
        declarationDataFilter.setReportPeriodIds(asList(action.getReportPeriodId()));
        declarationDataFilter.setDepartmentIds(asList(action.getDepartment()));
        declarationDataFilter.setSearchOrdering(DeclarationDataSearchOrdering.DECLARATION_TYPE_NAME);
        declarationDataFilter.setStartIndex(0);
        declarationDataFilter.setCountOfRecords(10);
        declarationDataFilter.setTaxType(action.getTaxType());
        PagingResult<DeclarationDataSearchResultItem> page = declarationDataSearchService.search(declarationDataFilter);
        for(DeclarationDataSearchResultItem item: page) {
            logger.warn(String.format(action.getTaxType().equals(TaxType.DEAL) ? DECLARATION_WARN_D : DECLARATION_WARN, item.getDeclarationType(), departmentName, periodName));
            result.setDeclarationFormFound(true);
        }

        FormDataFilter formDataFilter = new FormDataFilter();
        formDataFilter.setReportPeriodIds(asList(action.getReportPeriodId()));
        ArrayList<Long> formTypeIds = new ArrayList<Long>();
        formTypeIds.add(372L); // приложение 5
        formTypeIds.add(500L); // сводная 5
        formDataFilter.setFormTypeId(formTypeIds);
        formDataFilter.setFormState(WorkflowState.ACCEPTED);
        formDataFilter.setTaxType(action.getTaxType());
        TAUserInfo userInfo = userService.getSystemUserInfo();
        boolean manual = true;
        List<Long> formDataIds = formDataSearchService.findDataIdsByUserAndFilter(userInfo, formDataFilter);
        for(Long formDataId : formDataIds) {
            FormData formData = formDataService.getFormData(userInfo, formDataId, manual, logger);
            PagingResult<DataRow<Cell>> resultDataRow = dataRowService.getDataRows(userInfo, formDataId, null, true, manual);
            for(DataRow<Cell> dataRow : resultDataRow) {
                BigDecimal regionBankDivisionId = dataRow.getCell("regionBankDivision").getNumericValue();
                if (regionBankDivisionId != null && regionBankDivisionId.intValue() == action.getDepartment()) {
                    logger.warn(String.format(action.getTaxType().equals(TaxType.DEAL) ? FORM_WARN_S : FORM_WARN, formData.getFormType().getName(), departmentName, periodName));
                    result.setDeclarationFormFound(true);
                    break;
                }
            }
        }
        // Запись ошибок в лог при наличии
        if (!logger.getEntries().isEmpty()) {
            result.setUuid(logEntryService.save(logger.getEntries()));
            if (logger.containsLevel(LogLevel.ERROR)) {
                result.setHasError(true);
            }
        }
        return result;
    }

    @Override
    public void undo(GetCheckDeclarationAction action, GetCheckDeclarationResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}