package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataAccessParams;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriodSpecificName;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.DiffService;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult.FormMode;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetFormDataHandler extends AbstractActionHandler<GetFormDataAction, GetFormDataResult> {

	private static final Log LOG = LogFactory.getLog(GetFormDataHandler.class);

	@Autowired
	private FormDataAccessService accessService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private FormTemplateService formTemplateService;

    @Autowired 
    private TAUserService taUserService;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private DataRowService dataRowService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private DiffService diffService;

    @Autowired
    private LockDataService lockDataService;

    private static final long REF_BOOK_ID = 8L;
    private static final String REF_BOOK_VALUE_NAME = "CODE";
    private final static String RESTRICT_EDIT_MESSAGE = "Нет прав на редактирование налоговой формы!";
    private final static String CLOSED_PERIOD_MESSAGE = "Отчетный период подразделения закрыт!";
    private final static String CORRECTION_EDIT_MESSAGE = "Нельзя открыть налоговую форму в режиме редактирования для представления «Корректировка»!";
    private final static String CORRECTION_EDIT_MESSAGE_2 = "Нельзя открыть налоговую форму для представления «Корректировка», если она редактируется!";
    private final static String CORRECTION_ERROR_MESSAGE = "Нельзя открыть налоговую форму, созданную в периоде, не являющемся корректирующим в режиме представления «Корректировка»!";
    private final static String PREVIOUS_FORM_NOT_FOUND_MESSAGE = "Форма ранее не была создана, данные о различиях не сформированы.";
    private final static String SUCCESS_CORRECTION_MESSAGE = "Корректировка отображена в результате сравнения с данными формы в периоде %s %s%s.";

	public GetFormDataHandler() {
		super(GetFormDataAction.class);
	}

	@Override
	public GetFormDataResult execute(GetFormDataAction action, ExecutionContext context) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        GetFormDataResult result = new GetFormDataResult();
        Logger logger = new Logger();

		// UNLOCK: Попытка разблокировать ту форму которая была открыта ранее
		try {
			if (action.getOldFormDataId() != null) {
                LockData lockDataEdit = formDataService.getObjectLock(action.getOldFormDataId(), userInfo);
                if (lockDataEdit != null && lockDataEdit.getUserId() == userInfo.getUser().getId()) {
                    // Если есть блокировка, то удаляем задачи и откатываем изменения
                    formDataService.restoreCheckPoint(action.getOldFormDataId(), action.isManual(), userInfo);
                }
			}
		} catch (Exception e){
			//
		}

        actionCheck(action);

		// LOCK: Попытка заблокировать форму которую хотим получить для редактирования
		if (!action.isReadOnly()) {
            try {
                LockData lockDataEdit = formDataService.getObjectLock(action.getFormDataId(), userInfo);
                if (lockDataEdit != null && lockDataEdit.getUserId() == userInfo.getUser().getId()) {
                    // Если есть блокировка, то удаляем задачи и откатываем изменения
                    formDataService.restoreCheckPoint(action.getFormDataId(), action.isManual(), userInfo);
                }
            } catch (Exception e){
            }
            Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(action.getFormDataId());
			try {
                // Защита от перехода в режим редактирования если нф заблокирована какой-либо операцией
                if (lockType == null) {
                    formDataService.lock(action.getFormDataId(), action.isManual(), userInfo);
                }
			} catch (Exception e){
			}
		}

        try {
            LockData lockDataEdit = formDataService.getObjectLock(action.getFormDataId(), userInfo);
            if (!action.isReadOnly() && lockDataEdit != null && lockDataEdit.getUserId() == userInfo.getUser().getId()) {
                FormData formData = formDataService.getFormData(userInfo, action.getFormDataId(), action.isManual(), logger);
				// Когда пользователь входит в режим редактирования, то создаем контрольную точку восстановления
                dataRowService.createCheckPoint(formData);
            }

            fillLockData(action, userInfo, result);
            fillFormAndTemplateData(action, userInfo, logger, result);
            fillFormDataAccessParams(action, userInfo, result);

            FormData formData = result.getFormData();
            FormTemplate formTemplate = formTemplateService.get(formData.getFormTemplateId());
            //Проверка статуса макета НФ при открытиии налоговой формы.
            if (formTemplate.getStatus() == VersionedObjectStatus.DRAFT) {
                logger.error("Форма выведена из действия!");
            }

            // Форма открывается для чтения если так и запросили или нет прав или период закрыт
            result.setReadOnly(action.isReadOnly() || !result.getFormDataAccessParams().isCanEdit()
                    || !result.getDepartmentReportPeriod().isActive());

            if (result.isReadOnly() != action.isReadOnly()) {
                // Запросили на редактирование, а вернули на чтение
                if (result.getLockedByUser() != null && !result.getLockedByUser().isEmpty()) {
                    throw new ActionException("Форма заблокирована и не может быть изменена. Попробуйте выполнить операцию позже.");
                }
                String msg = result.getFormDataAccessParams().isCanEdit() ? CLOSED_PERIOD_MESSAGE : RESTRICT_EDIT_MESSAGE;
                throw new ActionException("Нельзя открыть налоговую форму в режиме редактирования. " + msg);
            }

            if (action.getUuid() != null) {
                result.setUuid(logEntryService.update(logger.getEntries(), action.getUuid()));
            } else {
                result.setUuid(logEntryService.save(logger.getEntries()));
            }

            result.getFormData().initFormTemplateParams(formTemplate);
            return result;
        } catch (Exception e) {
			LOG.error(e.getMessage(), e);
            LockData lockDataEdit = formDataService.getObjectLock(action.getFormDataId(), userInfo);
            if (!action.isReadOnly() && lockDataEdit != null && lockDataEdit.getUserId() == userInfo.getUser().getId()) {
                // Удаляем контрольную точку восстановления
                FormData formData = formDataService.getFormData(userInfo, action.getFormDataId(), action.isManual(), logger);
                dataRowService.removeCheckPoint(formData);
                // Удаляем блокировку
                formDataService.unlock(action.getFormDataId(), userInfo);
            }
            if (e instanceof ActionException)
                throw (ActionException)e;
            throw new ActionException(e);
        }
	}

    /**
     * Проверки правильности параметров запроса
     */
    private void actionCheck(GetFormDataAction action) throws ActionException {
        if (!action.isReadOnly() && action.isCorrectionDiff()) {
            throw new ActionException(CORRECTION_EDIT_MESSAGE);
        } else if (action.isCorrectionDiff() &&
                formDataService.getObjectLock(action.getFormDataId(), securityService.currentUserInfo()) != null) {
            throw new ActionException(CORRECTION_EDIT_MESSAGE_2);
        }
    }

    @Override
	public void undo(GetFormDataAction action, GetFormDataResult result,
			ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}

	/**
	 * Получает/создает данные налоговой формы
	 */
	private void fillFormAndTemplateData(GetFormDataAction action, TAUserInfo userInfo, Logger logger,
                                         GetFormDataResult result) throws ActionException {

		FormData formData = formDataService.getFormData(userInfo, action.getFormDataId(), action.isManual(), logger);

        checkManualMode(formData, action.isManual());

        FormTemplate formTemplate = formTemplateService.get(formData.getFormTemplateId());

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(
                formData.getDepartmentReportPeriodId());

        // http://jira.aplana.com/browse/SBRFACCTAX-6399
        if ((formData.getKind() == FormDataKind.PRIMARY || formData.getKind() == FormDataKind.CONSOLIDATED)
                && departmentReportPeriod.getReportPeriod().getTaxPeriod().getTaxType() == TaxType.INCOME) {
            RefBookDataProvider dataProvider = refBookFactory.getDataProvider(REF_BOOK_ID);
            Map<String, RefBookValue> refBookValueMap = dataProvider.getRecordData(
                    departmentReportPeriod.getReportPeriod().getDictTaxPeriodId());
            Integer code = Integer.parseInt(refBookValueMap.get(REF_BOOK_VALUE_NAME).getStringValue());
            departmentReportPeriod.getReportPeriod().setName(ReportPeriodSpecificName.fromId(code).getName());
        }
        result.setDepartmentReportPeriod(departmentReportPeriod);
        result.setComparativePeriod(formData.getComparativePeriodId() != null ? departmentReportPeriodService.get(
                formData.getComparativePeriodId()) : null);
		result.setDepartmentName(departmentService.getDepartment(formData.getDepartmentId()).getName());
        result.setDepartmentFullName(departmentService.getParentsHierarchy(formData.getDepartmentId()));
		result.setAllStyles(formTemplate.getStyles());
		result.setFixedRows(formTemplate.isFixedRows());
		result.setTemplateFormName(formTemplate.getName());
        formData.setHeaders(formDataService.getHeaders(formData, userInfo, logger));
		result.setFormData(formData);
        result.setBankSummaryForm(true);
        result.setCorrectionDiff(action.isCorrectionDiff());
        result.setExistManual(formDataService.existManual(action.getFormDataId()));
        result.setUpdating(formTemplate.isUpdating());

        // Если клиент запросил режим сравнения НФ, то нужно заполнить временный срез результатом сравнения
        if (action.isCorrectionDiff()) {
            fillDiffData(formData, departmentReportPeriod, logger);
        }

        //Является ли форма последней перед декларацией
        List<DepartmentDeclarationType> declarationDestinations = sourceService.getDeclarationDestinations(
                formData.getDepartmentId(), formData.getFormType().getId(), formData.getKind(),
                departmentReportPeriod.getReportPeriod().getCalendarStartDate(),
                departmentReportPeriod.getReportPeriod().getEndDate());
        result.setCanCreatedManual(formData.getState() == WorkflowState.ACCEPTED
                && (formData.getKind().equals(FormDataKind.CONSOLIDATED) || formData.getKind().equals(FormDataKind.SUMMARY))
                && !declarationDestinations.isEmpty() && departmentReportPeriod.getCorrectionDate() == null);
	}

    /**
     * Заполнение временного среза результатом сравнения
     */
    private void fillDiffData(FormData formData, DepartmentReportPeriod departmentReportPeriod, Logger logger) throws ActionException {
        // Если период не является корректирующим
        if (departmentReportPeriod.getCorrectionDate() == null) {
            throw new ActionException(CORRECTION_ERROR_MESSAGE);
        }

        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setDepartmentIdList(Arrays.asList(departmentReportPeriod.getDepartmentId()));
        filter.setReportPeriodIdList(Arrays.asList(departmentReportPeriod.getReportPeriod().getId()));
        // Список всех отчетных периодов для пары отчетный период-подразделение
        List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.getListByFilter(filter);

        Collections.sort(departmentReportPeriodList, new Comparator<DepartmentReportPeriod>() {
            @Override
            public int compare(DepartmentReportPeriod o1, DepartmentReportPeriod o2) {
                if (o1.getCorrectionDate() == null) {
                    return -1;
                }
                if (o2.getCorrectionDate() == null) {
                    return 1;
                }
                return o1.getCorrectionDate().compareTo(o2.getCorrectionDate());
            }
        });

        FormData prevFormData = formDataService.getPreviousFormDataCorrection(formData,
                departmentReportPeriodList, departmentReportPeriod);

        if (prevFormData == null) {
            logger.error(PREVIOUS_FORM_NOT_FOUND_MESSAGE);
            return;
        }

        // Шаблон НФ
        FormTemplate formTemplate = formTemplateService.get(prevFormData.getFormTemplateId());
        prevFormData.initFormTemplateParams(formTemplate);
        DepartmentReportPeriod prevDepartmentReportPeriod = departmentReportPeriodService.get(prevFormData.getDepartmentReportPeriodId());
        ReportPeriod prevReportPeriod = prevDepartmentReportPeriod.getReportPeriod();
        StringBuilder correctionString = new StringBuilder();
        if (prevDepartmentReportPeriod.getCorrectionDate() != null) {
            correctionString.append(", корр. (").append(new SimpleDateFormat("dd.MM.yyyy").format(departmentReportPeriod.getCorrectionDate())).append(")");
        }
        logger.info(String.format(SUCCESS_CORRECTION_MESSAGE, prevReportPeriod.getName(), prevReportPeriod.getTaxPeriod().getYear(), correctionString));

        List<DataRow<Cell>> original = dataRowService.getSavedRows(prevFormData);
        List<DataRow<Cell>> revised = dataRowService.getSavedRows(formData);
        List<DataRow<Cell>> diffRows = diffService.getDiff(original, revised);

        // Сохранение результата сравнения во временном срезе
        dataRowService.saveTempRows(formData, diffRows);
    }

    /**
	 * Заполняет параметры доступа для формы
	 */
	private void fillFormDataAccessParams(GetFormDataAction action, TAUserInfo userInfo,
			GetFormDataResult result) {
		FormDataAccessParams accessParams;
		if (action.getFormDataId() == Long.MAX_VALUE) {
			accessParams = new FormDataAccessParams();
			accessParams.setCanDelete(false);
			accessParams.setCanEdit(true);
			accessParams.setCanRead(true);
			accessParams.setAvailableWorkflowMoves(new ArrayList<WorkflowMove>(0));
		} else {
			accessParams = accessService.getFormDataAccessParams(userInfo, result
					.getFormData().getId(),
                    result.getFormData().isManual());
        }
		result.setFormDataAccessParams(accessParams);
	}

	/**
	 * Блокирует форму при необходимости, заполняет состояние блокировки
	 */
	private void fillLockData(GetFormDataAction action, TAUserInfo userInfo, GetFormDataResult result) {
		FormMode formMode = FormMode.READ_LOCKED;

		LockData lockInformation = formDataService.getObjectLock(action.getFormDataId(),
                securityService.currentUserInfo());
        // Защита от перехода в режим редактирования, если запущена какая-либо операция
        Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(action.getFormDataId());
        LockData lockTask = null;
        if (lockType != null && !ReportType.EDIT_FD.equals(lockType.getFirst())) {
            lockTask = lockType.getSecond();
        }
		if (lockInformation != null || lockTask != null) {
            LockData lockData;
            boolean forcedLock = false;
            if (lockTask != null) {
                lockData = lockTask;
                //Надо заблокировать даже от автора блокировки
                forcedLock = true;
            } else {
                lockData = lockInformation;
            }
			// Если данная форма уже заблокирована другим пользотелем
			result.setLockedByUser(taUserService.getUser(lockData.getUserId()).getName());
			result.setLockDate(getFormedDate(lockData.getDateLock()));
			if (lockData.getUserId() == userInfo.getUser().getId() && !forcedLock) {
				if (action.isReadOnly()) {
					formMode = FormMode.READ_UNLOCKED;
				} else {
					formMode = FormMode.EDIT;
				}
			}
		} else {
				if (action.isReadOnly()) {
					formMode = FormMode.READ_UNLOCKED;
				} else {
					formMode = FormMode.EDIT;
				}
		}
		result.setFormMode(formMode);
	}

	private static String getFormedDate(Date dateToForm) {
		// Преобразуем Date в строку вида "dd.mm.yyyy hh:mm"
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm z");
		formatter.format(dateToForm);
		return (formatter.format(dateToForm));
	}

    /**
     * Фикс для только что созданной версии ручного ввода
     * http://jira.aplana.com/browse/SBRFACCTAX-7676
     *
     * @param formData экземпляр НФ
     * @param isManual признак ручного ввода
     */
    private void checkManualMode(FormData formData, boolean isManual) {
		if (isManual) {
        	int rowCount = dataRowService.getRowCount(formData.getId(), true, true);
			if (rowCount == 0) {
				formData.setManual(true);
			}
		}
    }
}
