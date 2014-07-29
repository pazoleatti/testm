package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.source.SourceClientData;
import com.aplana.sbrf.taxaccounting.model.source.SourceMode;
import com.aplana.sbrf.taxaccounting.model.source.SourceObject;
import com.aplana.sbrf.taxaccounting.model.source.SourcePair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.UpdateCurrentAssignsAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.UpdateCurrentAssignsResult;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.CurrentAssign;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodInfo;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodsInterval;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class UpdateCurrentAssignsHandler extends AbstractActionHandler<UpdateCurrentAssignsAction, UpdateCurrentAssignsResult> {

	@Autowired
	private SourceService sourceService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private RefBookFactory rbFactory;

    private static final Long PERIOD_CODE_REFBOOK = 8L;

    public UpdateCurrentAssignsHandler() {
        super(UpdateCurrentAssignsAction.class);
    }

    @Override
    public UpdateCurrentAssignsResult execute(UpdateCurrentAssignsAction action, ExecutionContext context) {
        UpdateCurrentAssignsResult result = new UpdateCurrentAssignsResult();
        PeriodsInterval period = action.getNewPeriodsInterval();
        SourceClientData sourceClientData = new SourceClientData();
        Logger logger = new Logger();

        List<SourceObject> sourceObjects = new ArrayList<SourceObject>();
        List<SourcePair> sourcePairs = new ArrayList<SourcePair>();
        SourcePair sourcePair;
        CurrentAssign assign = action.getCurrentAssign();
        if (action.getMode() == SourceMode.SOURCES) {
            sourcePair = new SourcePair(assign.getId(), action.getDepartmentAssign().getId());
            sourcePair.setSourceKind(assign.getFormKind());
            sourcePair.setSourceType(assign.getFormType());
            sourcePair.setDestinationKind(action.getDepartmentAssign().getKind());
            if (action.isDeclaration()) {
                sourcePair.setDestinationDeclarationType(sourceService.getDeclarationType(action.getDepartmentAssign().getTypeId()));
            } else {
                sourcePair.setDestinationFormType(sourceService.getFormType(action.getDepartmentAssign().getTypeId()));
            }
        } else {
            sourcePair = new SourcePair(action.getDepartmentAssign().getId(), assign.getId());
            sourcePair.setSourceKind(action.getDepartmentAssign().getKind());
            sourcePair.setSourceType(sourceService.getFormType(action.getDepartmentAssign().getTypeId()));
            sourcePair.setDestinationKind(assign.getFormKind());
            if (action.isDeclaration()) {
                sourcePair.setDestinationDeclarationType(assign.getDeclarationType());
            } else {
                sourcePair.setDestinationFormType(assign.getFormType());
            }
        }
        SourceObject sourceObject = new SourceObject(sourcePair, assign.getStartDateAssign(), assign.getEndDateAssign());
        sourceObjects.add(sourceObject);
        sourcePairs.add(sourcePair);

        sourceClientData.setSourceObjects(sourceObjects);
        sourceClientData.setSourcePairs(sourcePairs);
        sourceClientData.setMode(action.getMode());
        sourceClientData.setDeclaration(action.isDeclaration());
        sourceClientData.setPeriodStart(PeriodConvertor.getDateFrom(period));
        sourceClientData.setPeriodStartName(period.getPeriodStartName());
        sourceClientData.setPeriodEnd(PeriodConvertor.getDateTo(period));
        sourceClientData.setPeriodEndName(period.getPeriodTo() != null ? period.getPeriodEndName() : null);

        /** Получение информации по периодам из справочника Коды, определяющие налоговый (отчётный) период*/
        RefBook refBook = rbFactory.get(PERIOD_CODE_REFBOOK);
        RefBookDataProvider provider = rbFactory.getDataProvider(refBook.getId());

        String filter = action.getTaxType().getCode() + " = 1";
        PagingResult<Map<String, RefBookValue>> records = provider.getRecords(new Date(), null, filter ,null);
        if (records.isEmpty()) {
            throw new ServiceException("Некорректные данные в справочнике \"Коды, определяющие налоговый (отчётный) период\"");
        }

        Calendar calendarFrom = Calendar.getInstance();
        calendarFrom.setTime(action.getOldDateFrom());
        calendarFrom.set(Calendar.YEAR, 1970);
        Date oldDateFrom = calendarFrom.getTime();

        Date oldDateTo = null;
        if (action.getOldDateTo() != null) {
            Calendar calendarTo = Calendar.getInstance();
            calendarTo.setTime(action.getOldDateTo());
            calendarTo.set(Calendar.YEAR, 1970);
            oldDateTo = calendarTo.getTime();
        }

        /** Начало старого периода в текстовом представлении. Используется в обработке ошибок */
        String oldPeriodStartName = null;
        /** Окончание старого периода в текстовом представлении. Используется в обработке ошибок */
        String oldPeriodEndName = null;
        for (Map<String, RefBookValue> record : records) {
            Date startDate = record.get("CALENDAR_START_DATE").getDateValue();
            Date endDate = record.get("END_DATE").getDateValue();
            if (startDate.equals(oldDateFrom)) {
                oldPeriodStartName = record.get("NAME").getStringValue();
            }
            if (action.getOldDateTo() != null && endDate.equals(oldDateTo)) {
                oldPeriodEndName = record.get("NAME").getStringValue();
            }
        }
        sourceClientData.setOldPeriodStart(action.getOldDateFrom());
        sourceClientData.setOldPeriodStartName(oldPeriodStartName);
        sourceClientData.setOldPeriodEnd(action.getOldDateTo());
        sourceClientData.setOldPeriodEndName(oldPeriodEndName);

        sourceService.updateSources(logger, sourceClientData);
        result.setUuid(logEntryService.save(logger.getEntries()));
		return result;
    }

    @Override
    public void undo(UpdateCurrentAssignsAction action, UpdateCurrentAssignsResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
