package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult.FormMode;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetFormDataHandler extends AbstractActionHandler<GetFormDataAction, GetFormDataResult> {

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

    private static final long REF_BOOK_ID = 8L;
    private static final String REF_BOOK_VALUE_NAME = "CODE";
    private final static String RESTRICT_EDIT_MESSAGE = "Нет прав на редактирование налоговой формы!";
    private final static String CLOSED_PERIOD_MESSAGE = "Отчетный период подразделения закрыт!";
    private final static String CORRECTION_EDIT_MESSAGE = "Нельзя открыть налоговую форму в режиме редактирования для представления «Корректировка»!";
    private final static String CORRECTION_ERROR_MESSAGE = "Нельзя открыть налоговую форму, созданную в периоде, не являющемся корректирующим в режиме представления «Корректировка»!";
    private final static String PREVIOUS_FORM_NOT_FOUND_MESSAGE = "Не найдена ранее созданная форма в текущем периоде. Данные о различиях не сформированы.";
    private final static String SUCCESS_CORRECTION_MESSAGE = "Корректировка отображена в результате сравнения с данными формы в периоде %s %s%s.";
    private final static String MANUAL_USED_MESSAGE = "Для формирования декларации в корректируемом периоде используются данные версии ручного ввода, созданной в форме «%s», %s, «%s»!";

    private TAUserInfo userInfo;

	public GetFormDataHandler() {
		super(GetFormDataAction.class);
	}

	@Override
	public GetFormDataResult execute(GetFormDataAction action, ExecutionContext context) throws ActionException {

		userInfo = securityService.currentUserInfo();

        actionCheck(action);
		
		// UNLOCK: Попытка разблокировать ту форму которая была открыта ранее
		try {
			if (action.getOldFormDataId() != null) {
				formDataService.unlock(action.getOldFormDataId(), userInfo);
			}
		} catch (Exception e){
			//
		}

		// LOCK: Попытка заблокировать форму которую хотим получить для редактирования
		if (!action.isReadOnly()) {
			try {
				formDataService.lock(action.getFormDataId(), userInfo);
			} catch (Exception e){
				//
			}
		}

		GetFormDataResult result = new GetFormDataResult();
		Logger logger = new Logger();

		fillLockData(action, userInfo, result);
		fillFormAndTemplateData(action, userInfo, logger, result);
		fillFormDataAccessParams(action, userInfo, result);
        // Форма открывается для чтения если так и запросили или нет прав или период закрыт
        result.setReadOnly(action.isReadOnly() || !result.getFormDataAccessParams().isCanEdit()
                || !result.getDepartmentReportPeriod().isActive());

        if (result.isReadOnly() != action.isReadOnly()) {
            // Запросили на редактирование, а вернули на чтение
            String msg = result.getFormDataAccessParams().isCanEdit() ? CLOSED_PERIOD_MESSAGE : RESTRICT_EDIT_MESSAGE;
            throw new ActionException("Нельзя открыть налоговую форму в режиме редактирования. " + msg);
        }

        if (action.getUuid() != null) {
            result.setUuid(logEntryService.update(logger.getEntries(), action.getUuid()));
        } else {
            result.setUuid(logEntryService.save(logger.getEntries()));
        }

		return result;
	}

    /**
     * Ппрверки правильности параметров запроса
     */
    private void actionCheck(GetFormDataAction action) throws ActionException {
        if (!action.isReadOnly() && action.isCorrectionDiff()) {
            throw new ActionException(CORRECTION_EDIT_MESSAGE);
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

        FormTemplate formTemplate = formTemplateService.getFullFormTemplate(formData.getFormTemplateId());

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
		result.setDepartmentName(departmentService.getDepartment(formData.getDepartmentId()).getName());
        result.setDepartmentFullName(departmentService.getParentsHierarchy(formData.getDepartmentId()));
		result.setAllStyles(formTemplate.getStyles());
		result.setFixedRows(formTemplate.isFixedRows());
		result.setTemplateFormName(formTemplate.getName());
		result.setFormData(formData);
        result.setBankSummaryForm(true);
        result.setCorrectionDiff(action.isCorrectionDiff());
        result.setExistManual(formDataService.existManual(action.getFormDataId()));

        // Если клиент запросил режим сравнения НФ, то нужно заполнить временный срез результатом сравнения
        FormData prevFormData = null;
        if (action.isCorrectionDiff()) {
            prevFormData = fillDiffData(formData, departmentReportPeriod, logger);
        }

        //Является ли форма последней перед декларацией
        List<DepartmentDeclarationType> declarationDestinations = sourceService.getDeclarationDestinations(
                formData.getDepartmentId(), formData.getFormType().getId(), formData.getKind(),
                departmentReportPeriod.getReportPeriod().getCalendarStartDate(),
                departmentReportPeriod.getReportPeriod().getEndDate());
        result.setCanCreatedManual(formData.getState() == WorkflowState.ACCEPTED
                && (formData.getKind().equals(FormDataKind.CONSOLIDATED) || formData.getKind().equals(FormDataKind.SUMMARY))
                && !declarationDestinations.isEmpty());
        // Если декларация является приемником и есть форма ручного ввода в корректируемом периоде
        if (action.isCorrectionDiff() && !declarationDestinations.isEmpty() && prevFormData != null && formDataService.existManual(prevFormData.getId())) {
            logger.info(String.format(MANUAL_USED_MESSAGE, formData.getFormType().getName(), formData.getKind().getName(), result.getDepartmentName()));
        }
	}

    /**
     * Заполнение временного среза результатом сравнения
     */
    private FormData fillDiffData(FormData formData, DepartmentReportPeriod departmentReportPeriod, Logger logger) throws ActionException {
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
            dataRowService.saveRows(formData, new ArrayList<DataRow<Cell>>(0));
            return prevFormData;
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
        dataRowService.saveRows(formData, diffRows);
        return prevFormData;
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

		if (lockInformation != null) {
			// Если данная форма уже заблокирована другим пользотелем
			result.setLockedByUser(taUserService.getUser(lockInformation.getUserId()).getName());
			result.setLockDate(getFormedDate(lockInformation.getDateBefore()));
			if (lockInformation.getUserId() == userInfo.getUser().getId()) {
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
        int rowCount = dataRowService.getRowCount(formData.getId(), true, true);
        if (isManual && rowCount == 0) {
            formData.setManual(true);
        }
    }
}
