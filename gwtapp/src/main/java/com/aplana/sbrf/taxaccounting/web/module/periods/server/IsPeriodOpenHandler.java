package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.IsPeriodOpenAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.IsPeriodOpenResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP')")
public class IsPeriodOpenHandler extends AbstractActionHandler<IsPeriodOpenAction, IsPeriodOpenResult> {

    @Autowired
    PeriodService periodService;
    @Autowired
    DepartmentReportPeriodService departmentReportPeriodService;

    public IsPeriodOpenHandler() {
        super(IsPeriodOpenAction.class);
    }

    @Override
    public IsPeriodOpenResult execute(IsPeriodOpenAction isPeriodOpenAction, ExecutionContext executionContext) throws ActionException {
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setDepartmentIdList(Arrays.asList(isPeriodOpenAction.getDepartmentId()));
        filter.setReportPeriodIdList(Arrays.asList(isPeriodOpenAction.getReportPeriodId()));
        filter.setIsCorrection(false);
        List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.getListByFilter(filter);
        if (departmentReportPeriodList.size() == 1) {
            IsPeriodOpenResult result = new IsPeriodOpenResult();
            result.setPeriodOpen(departmentReportPeriodList.get(0).isActive());
            return result;
        } else if (departmentReportPeriodList.size() == 0){
            throw new ServiceException("Период не найден. Возможно он был удалён. Попробуйте обновить страницу.");
        }
        throw new ServiceException("Найдено слишком много периодов");
    }

    @Override
    public void undo(IsPeriodOpenAction isPeriodOpenAction, IsPeriodOpenResult isPeriodOpenResult, ExecutionContext executionContext) throws ActionException {

    }
}
