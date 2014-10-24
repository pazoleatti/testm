package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.source.SourceClientData;
import com.aplana.sbrf.taxaccounting.model.source.SourceMode;
import com.aplana.sbrf.taxaccounting.model.source.SourcePair;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.CreateAssignAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.CreateAssignResult;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.DepartmentAssign;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodsInterval;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class CreateAssignHandler extends AbstractActionHandler<CreateAssignAction, CreateAssignResult> {

    @Autowired
    private SourceService sourceService;

    @Autowired
    private LogEntryService logEntryService;

    public CreateAssignHandler() {
        super(CreateAssignAction.class);
    }

    @Override
    public CreateAssignResult execute(CreateAssignAction action, ExecutionContext context) throws ActionException {
        CreateAssignResult result = new CreateAssignResult();
        SourceClientData sourceClientData = new SourceClientData();
        Logger logger = new Logger();

        List<SourcePair> sourcePairs = new ArrayList<SourcePair>();
        for (DepartmentAssign right : action.getRightSelectedObjects()) {
            SourcePair sourcePair;
            if (action.getMode() == SourceMode.SOURCES) {
                sourcePair = new SourcePair(right.getId(), action.getLeftObject().getId());
                sourcePair.setSourceKind(right.getKind().getName());
                sourcePair.setSourceType(sourceService.getFormType(right.getTypeId()).getName());
                if (action.isDeclaration()) {
                    sourcePair.setDestinationType(sourceService.getDeclarationType(action.getLeftObject().getTypeId()).getName());
                } else {
                    sourcePair.setDestinationKind(action.getLeftObject().getKind().getName());
                    sourcePair.setDestinationType(sourceService.getFormType(action.getLeftObject().getTypeId()).getName());
                }
            } else {
                sourcePair = new SourcePair(action.getLeftObject().getId(), right.getId());
                sourcePair.setSourceKind(action.getLeftObject().getKind().getName());
                sourcePair.setSourceType(sourceService.getFormType(action.getLeftObject().getTypeId()).getName());
                if (action.isDeclaration()) {
                    sourcePair.setDestinationType(sourceService.getDeclarationType(right.getTypeId()).getName());
                } else {
                    sourcePair.setDestinationKind(right.getKind().getName());
                    sourcePair.setDestinationType(sourceService.getFormType(right.getTypeId()).getName());
                }
            }
            sourcePairs.add(sourcePair);
        }
        PeriodsInterval period = action.getPeriodsInterval();

        sourceClientData.setSourcePairs(sourcePairs);
        sourceClientData.setMode(action.getMode());
        sourceClientData.setDeclaration(action.isDeclaration());
        sourceClientData.setPeriodStart(PeriodConvertor.getDateFrom(period));
        sourceClientData.setPeriodStartName(period.getPeriodStartName());
        sourceClientData.setPeriodEnd(PeriodConvertor.getDateTo(period));
        sourceClientData.setPeriodEndName(period.getPeriodTo() != null ? period.getPeriodEndName() : null);
        if (action.getMode() == SourceMode.SOURCES) {
            sourceClientData.setSourceDepartmentId(action.getRightDepartmentId());
            sourceClientData.setDestinationDepartmentId(action.getLeftDepartmentId());
        } else {
            sourceClientData.setSourceDepartmentId(action.getLeftDepartmentId());
            sourceClientData.setDestinationDepartmentId(action.getRightDepartmentId());
        }

        sourceService.createSources(logger, sourceClientData);
        if (logger.containsLevel(LogLevel.ERROR)) {
            logger.clear(LogLevel.INFO);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(CreateAssignAction action, CreateAssignResult result, ExecutionContext context) throws ActionException {
        //Do nothing
    }
}
