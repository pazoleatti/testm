package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
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
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author Lenar Haziev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetCheckDeclarationHandler extends AbstractActionHandler<GetCheckDeclarationAction, GetCheckDeclarationResult> {

    private static final String WARN_MSG = "\"%s\" %s, \"%s\", состояние - \"%s\"";

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
    @Autowired
    SecurityService securityService;

    @Autowired
    DepartmentReportPeriodService departmentReportPeriodService;

    public GetCheckDeclarationHandler() {
        super(GetCheckDeclarationAction.class);
    }

    @Override
    public GetCheckDeclarationResult execute(GetCheckDeclarationAction action, ExecutionContext executionContext) throws ActionException {
        GetCheckDeclarationResult result = new GetCheckDeclarationResult();
        Logger logger = new Logger();

        ReportPeriod period = reportService.getReportPeriod(action.getReportPeriodId());
        String periodName = period.getName() + " " + period.getTaxPeriod().getYear();
        DepartmentReportPeriodFilter departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setDepartmentIdList(Arrays.asList(action.getDepartment()));
        departmentReportPeriodFilter.setReportPeriodIdList(Arrays.asList(action.getReportPeriodId()));
        departmentReportPeriodFilter.setIsActive(true);
        List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.getListByFilter(departmentReportPeriodFilter);
        DepartmentReportPeriod departmentReportPeriod = null;
        if (departmentReportPeriodList.size() == 1) {
            departmentReportPeriod = departmentReportPeriodList.get(0);
        }
        String correctionDate = (departmentReportPeriod == null || departmentReportPeriod.getCorrectionDate() == null) ? "" :
                "с датой сдачи корректировки \"" + (departmentReportPeriod.getCorrectionDate()) + "\"";

        DeclarationDataFilter declarationDataFilter = new DeclarationDataFilter();
        declarationDataFilter.setReportPeriodIds(asList(action.getReportPeriodId()));
        declarationDataFilter.setDepartmentIds(asList(action.getDepartment()));
        declarationDataFilter.setSearchOrdering(DeclarationDataSearchOrdering.DECLARATION_TYPE_NAME);
        declarationDataFilter.setStartIndex(0);
        declarationDataFilter.setCountOfRecords(10);
        declarationDataFilter.setTaxType(action.getTaxType());
        PagingResult<DeclarationDataSearchResultItem> page = declarationDataSearchService.search(declarationDataFilter);
        for(DeclarationDataSearchResultItem item: page) {
            logger.warn(String.format(WARN_MSG, periodName, correctionDate, item.getDeclarationType(), item.isAccepted() ? "Принята" : "Создана"));
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
            PagingResult<DataRow<Cell>> resultDataRow = dataRowService.getDataRows(formDataId, null, true, manual);
            for(DataRow<Cell> dataRow : resultDataRow) {
                BigDecimal regionBankDivisionId = dataRow.getCell("regionBankDivision").getNumericValue();
                if (regionBankDivisionId != null && regionBankDivisionId.intValue() == action.getDepartment()) {
                    logger.warn(String.format(WARN_MSG, periodName, correctionDate, formData.getFormType().getName(), formData.getState().getName()));
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
        result.setControlUnp(securityService.currentUserInfo().getUser().hasRole(TARole.ROLE_CONTROL_UNP));
        return result;
    }

    @Override
    public void undo(GetCheckDeclarationAction action, GetCheckDeclarationResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}