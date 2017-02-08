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
    private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

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
            case NDFL:
                refBookId = RefBook.WithTable.NDFL.getRefBookId();
                break;
            case PFR:
                refBookId = RefBook.WithTable.FOND.getRefBookId();
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
            periodName.append(SIMPLE_DATE_FORMAT.get().format(dateStart));
            periodName.append(" по ");
            if (dateEnd != null) {
                periodName.append(SIMPLE_DATE_FORMAT.get().format(dateEnd));
            } else {
                periodName.append(" \"-\"");
            }

            result.setReportPeriodName(periodName.toString());
        }

        return result;
    }

    @Override
    public void undo(GetCheckDeclarationAction action, GetCheckDeclarationResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}