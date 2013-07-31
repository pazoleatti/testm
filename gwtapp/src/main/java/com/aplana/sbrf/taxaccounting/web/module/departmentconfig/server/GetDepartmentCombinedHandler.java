package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.dao.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetDepartmentCombinedAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetDepartmentCombinedResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Получение параметров подразделения и списка доступных налоговых периодов
 *
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetDepartmentCombinedHandler extends AbstractActionHandler<GetDepartmentCombinedAction, GetDepartmentCombinedResult> {

    //@Autowired
    //private DepartmentService departmentService;

    //@Autowired
    //private RefBookDao rbDao;

    //@Autowired
    //private ReportPeriodDao periodDao;

    public GetDepartmentCombinedHandler() {
        super(GetDepartmentCombinedAction.class);
    }

    @Override
    public GetDepartmentCombinedResult execute(GetDepartmentCombinedAction action, ExecutionContext executionContext) throws ActionException {

        DepartmentCombined depCombined = new DepartmentCombined();

        // PagingParams pp = new PagingParams();
        // getRecords(Long refBookId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);
        // rbDao.getRecords(32L, null, null, null, null);

        // TODO Заменить на рефбуки


        GetDepartmentCombinedResult result = new GetDepartmentCombinedResult();
        result.setDepartmentCombined(depCombined);
        //result.setPeriods(periodDao.listByTaxPeriodAndDepartmentId());

        return result;
    }

    @Override
    public void undo(GetDepartmentCombinedAction action, GetDepartmentCombinedResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
