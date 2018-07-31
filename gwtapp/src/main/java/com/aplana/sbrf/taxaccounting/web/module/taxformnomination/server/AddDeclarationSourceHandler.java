package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.AddDeclarationSourceAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.AddDeclarationSourceResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
public class AddDeclarationSourceHandler extends AbstractActionHandler<AddDeclarationSourceAction, AddDeclarationSourceResult> {
    {
    }

    public AddDeclarationSourceHandler() {
        super(AddDeclarationSourceAction.class);
    }

    @Autowired
    SourceService departmentFormTypeService;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    DeclarationTypeService declarationTypeService;
    @Autowired
    LogEntryService logEntryService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Override
    public AddDeclarationSourceResult execute(AddDeclarationSourceAction action, ExecutionContext executionContext) throws ActionException {
        List<LogEntry> logs = new ArrayList<LogEntry>();
        boolean detectRelations = false;

        for (Integer depId : action.getDepartmentId()) {
            for (Long dt : action.getDeclarationTypeId()) {
                boolean canAssign = true;
                //TODO тоже надо откуда то брать период
                for (DepartmentDeclarationType ddt : departmentFormTypeService.getDDTByDepartment(depId.intValue(), action.getTaxType(), new Date(), new Date())) {
                    if (ddt.getDeclarationTypeId() == dt) {
                        detectRelations = true;
                        canAssign = false;
                        logs.add(new LogEntry(LogLevel.WARNING, "Для \"" + departmentService.getDepartment(depId).getName() +
                                "\" уже существует назначение \"" + declarationTypeService.get(ddt.getDeclarationTypeId()).getName() + "\""));
                    }
                }
                if (canAssign) {
                    List<Integer> childrenDepartmentIdList = departmentService.getAllChildrenIds(depId);
                    for (Integer childrenDepartmentId : childrenDepartmentIdList) {
                        addPeriod(childrenDepartmentId);
                    }
                    departmentFormTypeService.saveDDT((long) depId, dt.intValue(), action.getPerformers());
                }
            }
        }
        AddDeclarationSourceResult result = new AddDeclarationSourceResult();
        result.setUuid(logEntryService.save(logs));
        result.setIssetRelations(detectRelations);
        return result;
    }

    @Override
    public void undo(AddDeclarationSourceAction addDeclarationSourceAction, AddDeclarationSourceResult addDeclarationSourceResult, ExecutionContext executionContext) throws ActionException {
    }

    private void addPeriod(int depId) {
        // Фильтр для проверки привязки подразделения к периоду
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setDepartmentIdList(Arrays.asList(depId));

        List<DepartmentReportPeriod> currDepDrpList = departmentReportPeriodService.fetchAllByFilter(filter);

        // Находим все периоды для ПАО "Сбербанк"
        DepartmentReportPeriodFilter filterAll = new DepartmentReportPeriodFilter();
        filterAll.setDepartmentIdList(Arrays.asList(0));
        List<DepartmentReportPeriod> drpList = departmentReportPeriodService.fetchAllByFilter(filterAll);

        List<DepartmentReportPeriod> drpForSave = new LinkedList<DepartmentReportPeriod>(drpList);

        // Удаляем периоды имеющиеся у текущего подразеления
        outer: for (DepartmentReportPeriod drp : drpList) {
            for (DepartmentReportPeriod currDepDrp : currDepDrpList) {
                if (currDepDrp.getReportPeriod().getId().equals(drp.getReportPeriod().getId())
                        && currDepDrp.isActive() == drp.isActive()
                        && ((currDepDrp.getCorrectionDate() == null && drp.getCorrectionDate() == null) || (currDepDrp.getCorrectionDate() != null && currDepDrp.getCorrectionDate().equals(drp.getCorrectionDate())))) {
                    drpForSave.remove(drp);
                    continue outer;
                }
            }
        }
        for (DepartmentReportPeriod drp : drpForSave) {
            drp.setId(null);
            drp.setDepartmentId(depId);
            departmentReportPeriodService.create(drp);
        }
    }
}
