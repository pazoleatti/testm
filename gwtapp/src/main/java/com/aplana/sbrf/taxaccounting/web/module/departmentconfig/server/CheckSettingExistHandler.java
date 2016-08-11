package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.CheckSettingExistAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.CheckSettingExistResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class CheckSettingExistHandler extends AbstractActionHandler<CheckSettingExistAction, CheckSettingExistResult> {

    public CheckSettingExistHandler() {
        super(CheckSettingExistAction.class);
    }


    @Autowired
    private PeriodService reportService;

    @Autowired
    private RefBookFactory rbFactory;


    @Override
    public CheckSettingExistResult execute(CheckSettingExistAction action, ExecutionContext executionContext) throws ActionException {
        CheckSettingExistResult result = new CheckSettingExistResult();

        Long refBookId = null;
        switch (action.getTaxType()) {
            case INCOME:
                refBookId = RefBook.DEPARTMENT_CONFIG_INCOME;
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
            case LAND:
                refBookId = RefBook.DEPARTMENT_CONFIG_LAND;
                break;
        }

        String filter = DepartmentParamAliases.DEPARTMENT_ID.name() + " = " + action.getDepartmentId();
        RefBookDataProvider provider = rbFactory.getDataProvider(refBookId);
        ReportPeriod period = reportService.getReportPeriod(action.getReportPeriodId());
        List<Long> existSettings = provider.getUniqueRecordIds(period.getCalendarStartDate(), filter);
        if (existSettings == null || existSettings.isEmpty()) {
            result.setSettingsExist(false);
        } else {
            result.setSettingsExist(true);
        }
        return result;
    }

    @Override
    public void undo(CheckSettingExistAction checkSettingExistAction, CheckSettingExistResult checkSettingExistResult, ExecutionContext executionContext) throws ActionException {

    }
}
