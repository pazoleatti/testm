package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author Lenar Haziev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetCheckDeclarationHandler extends AbstractActionHandler<GetCheckDeclarationAction, GetCheckDeclarationResult> {

    private static final String WARN_MSG = "\"%s\" %s, \"%s\"%s, состояние - \"%s\"";
    private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private TAUserService userService;

    @Autowired
    private FormDataSearchService formDataSearchService;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private DeclarationDataSearchService declarationDataSearchService;

    @Autowired
    private DataRowService dataRowService;

    @Autowired
    private PeriodService reportService;

    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private SecurityService securityService;

    @Autowired
    private RefBookFactory rbFactory;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    public GetCheckDeclarationHandler() {
        super(GetCheckDeclarationAction.class);
    }

    @Override
    public GetCheckDeclarationResult execute(GetCheckDeclarationAction action, ExecutionContext executionContext) throws ActionException {
        GetCheckDeclarationResult result = new GetCheckDeclarationResult();
        result.setControlUnp(securityService.currentUserInfo().getUser().hasRole(TARole.ROLE_CONTROL_UNP));

        Logger logger = new Logger();
        ReportPeriod period = reportService.getReportPeriod(action.getReportPeriodId());
        List<Integer> reportPeriodIds = new ArrayList<Integer>();
        ArrayList<Long> formTypeIds = new ArrayList<Long>();
        Long refBookId = null;
        switch (action.getTaxType()) {
            case INCOME:
                refBookId = RefBook.DEPARTMENT_CONFIG_INCOME;
                formTypeIds.add(372L); // приложение 5
                formTypeIds.add(500L); // сводная 5
                break;
            case TRANSPORT:
                refBookId = RefBook.DEPARTMENT_CONFIG_TRANSPORT;
                break;
            case DEAL:
                refBookId = RefBook.DEPARTMENT_CONFIG_DEAL;
                break;
            case VAT:
                refBookId = RefBook.DEPARTMENT_CONFIG_VAT;
                break;
            case PROPERTY:
                refBookId = RefBook.DEPARTMENT_CONFIG_PROPERTY;
                break;
        }
        RefBookDataProvider provider = rbFactory.getDataProvider(refBookId);
        String filter = DepartmentParamAliases.DEPARTMENT_ID.name() + " = " + action.getDepartment();
        List<Pair<Long, Long>> recordPairs = provider.checkRecordExistence(period.getCalendarStartDate(), filter);
        Date dateStart = period.getCalendarStartDate(), dateEnd;
        if (!recordPairs.isEmpty()) {
            //RefBookRecordVersion recordVersion = provider.getRecordVersionInfo(recordPairs.get(0).getFirst());
            dateEnd = provider.getRecordVersionInfo(recordPairs.get(0).getFirst()).getVersionEnd();
            List<ReportPeriod> reportPeriodList = reportService.getReportPeriodsByDate(action.getTaxType(), dateStart, dateEnd);
            if (reportPeriodList.isEmpty()){
                return result;
            }

            for(ReportPeriod reportPeriod: reportPeriodList)
                reportPeriodIds.add(reportPeriod.getId());

            StringBuffer periodName = new StringBuffer();
            periodName.append("с ");
            periodName.append(SIMPLE_DATE_FORMAT.format(dateStart));
            periodName.append(" по ");
            if (dateEnd != null) {
                periodName.append(SIMPLE_DATE_FORMAT.format(dateEnd));
            } else {
                periodName.append(" \"-\"");
            }

            result.setReportPeriodName(periodName.toString());
        } else {
            return result;
        }

        String periodName, correctionDate;

        DeclarationDataFilter declarationDataFilter = new DeclarationDataFilter();
        declarationDataFilter.setReportPeriodIds(reportPeriodIds);
        declarationDataFilter.setDepartmentIds(asList(action.getDepartment()));
        declarationDataFilter.setTaxType(action.getTaxType());
        List<DeclarationData> page = declarationDataSearchService.getDeclarationData(declarationDataFilter, DeclarationDataSearchOrdering.ID, false);
        for(DeclarationData item: page) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(item.getDepartmentReportPeriodId());
            periodName = departmentReportPeriod.getReportPeriod().getName() + " " + departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear();
            correctionDate = (departmentReportPeriod.getCorrectionDate() == null) ? "" :
                    "с датой сдачи корректировки \"" + (departmentReportPeriod.getCorrectionDate()) + "\"";
            String taxOrgan = (item.getTaxOrganCode() != null && !item.getTaxOrganCode().isEmpty()) ? ", налоговый орган " + item.getTaxOrganCode() : "";
            String kpp = (item.getKpp() != null && !item.getKpp().isEmpty()) ? ", КПП " + item.getKpp() : "";
            logger.warn(String.format(WARN_MSG, periodName, correctionDate,
                    declarationTemplateService.get(item.getDeclarationTemplateId()).getType().getName(),
                    taxOrgan + kpp,
                    item.isAccepted() ? "Принята" : "Создана"));
            result.setDeclarationFormFound(true);
        }

        if (!formTypeIds.isEmpty()) {
            FormDataFilter formDataFilter = new FormDataFilter();
            formDataFilter.setReportPeriodIds(reportPeriodIds);
            formDataFilter.setFormTypeId(formTypeIds);
            formDataFilter.setTaxType(action.getTaxType());
            TAUserInfo userInfo = userService.getSystemUserInfo();
            List<Long> formDataIds = formDataSearchService.findDataIdsByUserAndFilter(userInfo, formDataFilter);

            for (Long formDataId : formDataIds) {
                boolean manual = formDataService.existManual(formDataId);
                FormData formData = formDataService.getFormData(userInfo, formDataId, manual, logger);
                PagingResult<DataRow<Cell>> resultDataRow = dataRowService.getDataRows(formDataId, null, true, manual);
                for (DataRow<Cell> dataRow : resultDataRow) {
                    BigDecimal regionBankDivisionId = dataRow.getCell("regionBankDivision").getNumericValue();
                    if (regionBankDivisionId != null && regionBankDivisionId.intValue() == action.getDepartment()) {
                        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
                        String month = formData.getPeriodOrder() != null ? " " + Months.fromId(formData.getPeriodOrder()).getTitle() : "";
                        periodName = departmentReportPeriod.getReportPeriod().getName() + month + " " + departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear();
                        correctionDate = (departmentReportPeriod.getCorrectionDate() == null) ? "" :
                                "с датой сдачи корректировки \"" + (departmentReportPeriod.getCorrectionDate()) + "\"";
                        logger.warn(String.format(WARN_MSG, periodName, correctionDate, formData.getFormType().getName(), "",
                                formData.getState().getTitle()));
                        result.setDeclarationFormFound(true);
                    }
                }
            }
        }
        // Запись ошибок в лог при наличии
        if (result.isDeclarationFormFound() && action.isFatal()) {
            logger.logTopMessage(LogLevel.ERROR, "В периоде %s найдены экземпляры налоговых форм/деклараций, " +
                    "которые используют данные значения формы настроек подразделения. Для удаления данной версии настроек " +
                    "формы необходимо удалить найденные налоговые формы/декларации:", result.getReportPeriodName());
            throw new ServiceLoggerException("Настройка не удалена, обнаружены фатальные ошибки!",
                    logEntryService.save(logger.getEntries()));
        } else {
            if (!logger.getEntries().isEmpty()) {
                result.setUuid(logEntryService.save(logger.getEntries()));
            }
        }
        return result;
    }

    @Override
    public void undo(GetCheckDeclarationAction action, GetCheckDeclarationResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}