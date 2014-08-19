package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.source.SourceClientData;
import com.aplana.sbrf.taxaccounting.model.source.SourceMode;
import com.aplana.sbrf.taxaccounting.model.source.SourceObject;
import com.aplana.sbrf.taxaccounting.model.source.SourcePair;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.DeleteCurrentAssignsAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.DeleteCurrentAssignsResult;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.CurrentAssign;
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
public class DeleteCurrentAssignsHandler  extends AbstractActionHandler<DeleteCurrentAssignsAction, DeleteCurrentAssignsResult> {

    @Autowired
    private SourceService sourceService;

    @Autowired
    private LogEntryService logEntryService;

    public DeleteCurrentAssignsHandler() {
        super(DeleteCurrentAssignsAction.class);
    }

    @Override
    public DeleteCurrentAssignsResult execute(DeleteCurrentAssignsAction action, ExecutionContext context) throws ActionException {
        DeleteCurrentAssignsResult result = new DeleteCurrentAssignsResult();
        PeriodsInterval period = action.getPeriodsInterval();
        SourceClientData sourceClientData = new SourceClientData();
        Logger logger = new Logger();

        List<SourceObject> sourceObjects = new ArrayList<SourceObject>();
        List<SourcePair> sourcePairs = new ArrayList<SourcePair>();
        for (CurrentAssign assign : action.getCurrentAssigns()) {
            SourcePair sourcePair;
            if (action.getMode() == SourceMode.SOURCES) {
                sourcePair = new SourcePair(assign.getId(), action.getDepartmentAssign().getId());
                sourcePair.setSourceKind(assign.getFormKind().getName());
                sourcePair.setSourceType(assign.getFormType().getName());
                sourcePair.setDestinationKind(action.getDepartmentAssign().getKind().getName());
                if (action.isDeclaration()) {
                    sourcePair.setDestinationType(sourceService.getDeclarationType(action.getDepartmentAssign().getTypeId()).getName());
                } else {
                    sourcePair.setDestinationType(sourceService.getFormType(action.getDepartmentAssign().getTypeId()).getName());
                }
            } else {
                sourcePair = new SourcePair(action.getDepartmentAssign().getId(), assign.getId());
                sourcePair.setSourceKind(action.getDepartmentAssign().getKind().getName());
                sourcePair.setSourceType(sourceService.getFormType(action.getDepartmentAssign().getTypeId()).getName());
                sourcePair.setDestinationKind(assign.getFormKind().getName());
                sourcePair.setDestinationType(assign.getFormType().getName());
                if (action.isDeclaration()) {
                    sourcePair.setDestinationType(assign.getDeclarationType().getName());
                } else {
                    sourcePair.setDestinationType(assign.getFormType().getName());
                }
            }
            SourceObject sourceObject = new SourceObject(sourcePair, assign.getStartDateAssign(), assign.getEndDateAssign());
            sourceObjects.add(sourceObject);
            sourcePairs.add(sourcePair);
        }

        sourceClientData.setSourceObjects(sourceObjects);
        sourceClientData.setSourcePairs(sourcePairs);
        sourceClientData.setMode(action.getMode());
        sourceClientData.setDeclaration(action.isDeclaration());
        sourceClientData.setPeriodStart(PeriodConvertor.getDateFrom(period));
        sourceClientData.setPeriodStartName(period.getPeriodStartName());
        sourceClientData.setPeriodEnd(PeriodConvertor.getDateTo(period));
        sourceClientData.setPeriodEndName(period.getPeriodTo() != null ? period.getPeriodEndName() : null);

        sourceService.deleteSources(logger, sourceClientData);
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(DeleteCurrentAssignsAction action, DeleteCurrentAssignsResult result, ExecutionContext context) throws ActionException {
        //Do nothing
    }
}
