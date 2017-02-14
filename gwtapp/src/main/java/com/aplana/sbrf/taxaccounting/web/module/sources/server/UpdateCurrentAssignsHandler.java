package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.source.SourceClientData;
import com.aplana.sbrf.taxaccounting.model.source.SourceMode;
import com.aplana.sbrf.taxaccounting.model.source.SourceObject;
import com.aplana.sbrf.taxaccounting.model.source.SourcePair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.UpdateCurrentAssignsAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.UpdateCurrentAssignsResult;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.CurrentAssign;
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
    private DepartmentService departmentService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private RefBookFactory rbFactory;

    @Autowired
    private SecurityService securityService;

    private static final Long PERIOD_CODE_REFBOOK = RefBook.Id.PERIOD_CODE.getId();

    public UpdateCurrentAssignsHandler() {
        super(UpdateCurrentAssignsAction.class);
    }

    @Override
    public UpdateCurrentAssignsResult execute(UpdateCurrentAssignsAction action, ExecutionContext context) throws ActionException {
        UpdateCurrentAssignsResult result = new UpdateCurrentAssignsResult();
        PeriodsInterval period = action.getNewPeriodsInterval();
        Logger logger = new Logger();

        if (!securityService.currentUserInfo().getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
            for (CurrentAssign assign : action.getCurrentAssigns()) {
                if (action.getTaxType() != assign.getTaxType()) {
                    throw new ActionException("Недостаточно прав на редактирование назначений: назначенные формы должны относится к текущему налогу!");
                }
            }
        }

        String leftDepartmentName = departmentService.getDepartment(action.getLeftDepartmentId()).getName();
        List<SourceClientData> sourceClientDataList = new ArrayList<SourceClientData>();
        for (CurrentAssign currentAssign : action.getCurrentAssigns()) {
            List<SourceObject> sourceObjects = new ArrayList<SourceObject>();
            List<SourcePair> sourcePairs = new ArrayList<SourcePair>();
            SourceClientData sourceClientData = new SourceClientData();
            SourcePair sourcePair;
            if (action.getMode() == SourceMode.SOURCES) {
                sourcePair = new SourcePair(currentAssign.getId(), action.getDepartmentAssign().getId());
                sourcePair.setSourceKind(currentAssign.getFormKind().getTitle());
                sourcePair.setSourceType(currentAssign.getFormType().getName());
                sourcePair.setSourceDepartmentName(currentAssign.getDepartmentName());
                sourcePair.setDestinationDepartmentName(leftDepartmentName);
                if (action.isDeclaration()) {
                    sourcePair.setDestinationType(sourceService.getDeclarationType(action.getDepartmentAssign().getTypeId()).getName());
                } else {
                    sourcePair.setDestinationKind(action.getDepartmentAssign().getKind().getTitle());
                    sourcePair.setDestinationType(sourceService.getFormType(action.getDepartmentAssign().getTypeId()).getName());
                }
            } else {
                sourcePair = new SourcePair(action.getDepartmentAssign().getId(), currentAssign.getId());
                sourcePair.setSourceType(sourceService.getFormType(action.getDepartmentAssign().getTypeId()).getName());
                sourcePair.setDestinationKind(currentAssign.getFormKind().getTitle());
                sourcePair.setSourceDepartmentName(leftDepartmentName);
                sourcePair.setDestinationDepartmentName(currentAssign.getDepartmentName());
                if (action.isDeclaration()) {
                    sourcePair.setDestinationType(currentAssign.getDeclarationType().getName());
                } else {
                    sourcePair.setSourceKind(action.getDepartmentAssign().getKind().getTitle());
                    sourcePair.setDestinationType(currentAssign.getFormType().getName());
                }
            }
            SourceObject sourceObject = new SourceObject(sourcePair, currentAssign.getStartDateAssign(), currentAssign.getEndDateAssign());
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
            calendarFrom.setTime(currentAssign.getStartDateAssign());
            calendarFrom.set(Calendar.YEAR, 1970);
            Date oldDateFrom = calendarFrom.getTime();

            Date oldDateTo = null;
            if (currentAssign.getEndDateAssign() != null) {
                Calendar calendarTo = Calendar.getInstance();
                calendarTo.setTime(currentAssign.getEndDateAssign());
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
                if (currentAssign.getEndDateAssign() != null && endDate.equals(oldDateTo)) {
                    oldPeriodEndName = record.get("NAME").getStringValue();
                }
            }
            sourceClientData.setOldPeriodStart(currentAssign.getStartDateAssign());
            sourceClientData.setOldPeriodStartName(oldPeriodStartName);
            sourceClientData.setOldPeriodEnd(currentAssign.getEndDateAssign());
            sourceClientData.setOldPeriodEndName(oldPeriodEndName);
            if (action.getMode() == SourceMode.SOURCES) {
                sourceClientData.setSourceDepartmentId(currentAssign.getDepartmentId());
                sourceClientData.setDestinationDepartmentId(action.getLeftDepartmentId());
            } else {
                sourceClientData.setSourceDepartmentId(action.getLeftDepartmentId());
                sourceClientData.setDestinationDepartmentId(currentAssign.getDepartmentId());
            }
            sourceClientDataList.add(sourceClientData);
        }

        sourceService.updateSources(logger, sourceClientDataList);
        if (logger.containsLevel(LogLevel.ERROR)) {
            logger.clear(LogLevel.INFO);
        }
        List<LogEntry> entries = new ArrayList<LogEntry>();
        for (LogEntry entry : logger.getEntries()) {
            if (!entries.contains(entry)) {
                entries.add(entry);
            }
        }
        logger.clear();
        logger.setEntries(new ArrayList<LogEntry>(entries));
        result.setUuid(logEntryService.save(logger.getEntries()));
		return result;
    }

    @Override
    public void undo(UpdateCurrentAssignsAction action, UpdateCurrentAssignsResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
