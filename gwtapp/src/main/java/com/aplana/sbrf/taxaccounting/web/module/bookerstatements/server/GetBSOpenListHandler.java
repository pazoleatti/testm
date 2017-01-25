package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.server;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookBookerStatementPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.GetBSOpenListAction;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.GetBSOpenListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetBSOpenListHandler extends AbstractActionHandler<GetBSOpenListAction, GetBSOpenListResult> {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    SourceService departmentFormTypService;
    @Autowired
    RefBookFactory refBookFactory;

    public GetBSOpenListHandler() {
        super(GetBSOpenListAction.class);
    }

    @Override
    public GetBSOpenListResult execute(GetBSOpenListAction action, ExecutionContext executionContext) throws ActionException {
        GetBSOpenListResult result = new GetBSOpenListResult();

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

        // Подразделения доступные пользователю
        Set<Integer> avSet = new HashSet<Integer>();
        avSet.addAll(departmentService.getBADepartmentIds(currUser));

        // Необходимые для дерева подразделения
        result.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(avSet).values()));
        result.setAvailableDepartments(avSet);

        // Подразделение текущего пользователя
        result.setDepartment(departmentService.getDepartment(currUser.getDepartmentId()));
        result.setBookerReportTypes(Arrays.asList(BookerStatementsType.values()));

        RefBookDataProvider bookerRefBookDataProvider = refBookFactory.getDataProvider(RefBookBookerStatementPeriodDao.REF_BOOK_ID);

        PagingResult<Map<String, RefBookValue>> records = bookerRefBookDataProvider.getRecords(null, null, null, null, true);
        List<ReportPeriod> reportPeriods = new ArrayList<ReportPeriod>(records.size());
        for (Map<String, RefBookValue> record : records){
            final int year = record.get("YEAR").getNumberValue().intValue();
            TaxPeriod taxPeriod = new TaxPeriod();
            taxPeriod.setYear(year);
            ReportPeriod reportPeriod = new ReportPeriod();
            reportPeriod.setName(record.get("PERIOD_NAME").getStringValue().replaceFirst("^" + year + ".*:",""));
            reportPeriod.setId(record.get("record_id").getNumberValue().intValue());
            reportPeriod.setEndDate(null);
            reportPeriod.setStartDate(null);
            reportPeriod.setTaxPeriod(taxPeriod);
            reportPeriods.add(reportPeriod);
        }
        result.setReportPeriods(reportPeriods);
        return result;
    }

    @Override
    public void undo(GetBSOpenListAction action, GetBSOpenListResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
