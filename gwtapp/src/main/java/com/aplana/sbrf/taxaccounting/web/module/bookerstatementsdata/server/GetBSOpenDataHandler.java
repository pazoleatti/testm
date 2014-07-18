package com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.shared.GetBSOpenDataAction;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.shared.GetBSOpenDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetBSOpenDataHandler extends AbstractActionHandler<GetBSOpenDataAction, GetBSOpenDataResult> {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    SourceService departmentFormTypService;

    @Autowired
    RefBookFactory refBookFactory;

    public GetBSOpenDataHandler() {
        super(GetBSOpenDataAction.class);
    }

    @Override
    public GetBSOpenDataResult execute(GetBSOpenDataAction action, ExecutionContext executionContext) throws ActionException {
        GetBSOpenDataResult result = new GetBSOpenDataResult();

        if (action.getAccountPeriodId() == null) {
            throw new ActionException();
        }
        if (action.getDepartmentId() == null) {
            throw new ActionException();
        }
        if (action.getStatementsKind() == null ||
                !(action.getStatementsKind() == BookerStatementsType.INCOME101.getId() || action.getStatementsKind() == BookerStatementsType.INCOME102.getId())) {
            throw new ActionException();
        }
        // Текущий пользователь
        TAUser currUser = securityService.currentUserInfo().getUser();

        // Признак контролера
        if (currUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            result.setControlUNP(true);
        } else if (currUser.hasRole(TARole.ROLE_CONTROL) || currUser.hasRole(TARole.ROLE_CONTROL_NS)) {
            result.setControlUNP(false);
        }

        if (result.getControlUNP() == null) {
            // Не контролер, далее не загружаем
            return result;
        }

        if (action.getDepartmentId() != null) {
            result.setDepartmentName(departmentService.getParentsHierarchy(action.getDepartmentId()));
        }

        if (action.getAccountPeriodId() != null) {
            RefBookDataProvider dataProvider = refBookFactory.getDataProvider(107L);
            Map<String, RefBookValue> refBookValueMap = dataProvider.getRecordData((long) action.getAccountPeriodId());
            String date = String.valueOf(refBookValueMap.get("YEAR").getNumberValue());

            dataProvider = refBookFactory.getDataProvider(106L);
            refBookValueMap = dataProvider.getRecordData(refBookValueMap.get("ACCOUNT_PERIOD_ID").getReferenceValue());
            String name = refBookValueMap.get("NAME").getStringValue();

            result.setAccountPeriodName(date + " - " + name);
        }

        if (action.getStatementsKind() != null) {
            if (action.getStatementsKind() == BookerStatementsType.INCOME101.getId()) {
                result.setStatementsKindName(BookerStatementsType.INCOME101.getName());
            } else if (action.getStatementsKind() == BookerStatementsType.INCOME102.getId()) {
                result.setStatementsKindName(BookerStatementsType.INCOME102.getName());
            }
        }

        return result;
    }

    @Override
    public void undo(GetBSOpenDataAction action, GetBSOpenDataResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
