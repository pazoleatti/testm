package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 
 * @author Eugene Stetsenko Обработчик запроса для удаления формы.
 * 
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class DeleteFormDataHandler extends AbstractActionHandler<DeleteFormDataAction, DeleteFormDataResult> {

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private PeriodService reportPeriodService;

    @Autowired
    private FormDataScriptingService scriptingService;

    @Autowired
    private LockDataService lockDataService;

	public DeleteFormDataHandler() {
		super(DeleteFormDataAction.class);
	}

    @Override
    public DeleteFormDataResult execute(DeleteFormDataAction action, ExecutionContext context) throws ActionException {
        // Нажатие на кнопку "Удалить" http://conf.aplana.com/pages/viewpage.action?pageId=11384485
        DeleteFormDataResult result = new DeleteFormDataResult();
        Logger logger = new Logger();

        //Проверяем не заблокирована ли нф какой либо операцией по ее изменению - редактирование, формирование отчетов
        boolean locked = lockDataService.isLockExists(LockData.LockObjects.FORM_DATA + "_" + action.getFormData().getId(), true);
        //Проверяем не заблокирована ли нф операцией загрузки в нее
        boolean lockedByImport = lockDataService.isLockExists(LockData.LockObjects.FORM_DATA_IMPORT + "_" + action.getFormData().getId(), true);
        if (!locked && !lockedByImport) {
            // Версия ручного ввода удаляется без проверок
            if (action.isManual()) {
                formDataService.deleteFormData(logger, securityService.currentUserInfo(), action.getFormDataId(), true);
                return result;
            }

            FormData formData = action.getFormData();

            /** Проверяем в скрипте источники-приемники для особенных форм */
            Map<String, Object> params = new HashMap<String, Object>();
            FormSources sources = new FormSources();
            sources.setSourceList(new ArrayList<FormToFormRelation>());
            sources.setSourcesProcessedByScript(false);
            params.put("sources", sources);
            scriptingService.executeScript(securityService.currentUserInfo(), formData, FormDataEvent.GET_SOURCES, logger, params);

            if (sources.isSourcesProcessedByScript()) {
                //Скрипт возвращает все необходимые источники-приемники
                if (sources.getSourceList() != null) {
                    for (FormToFormRelation source : sources.getSourceList()) {
                        if (source.getState() == WorkflowState.ACCEPTED) {
                            if (source.getMonth() != null && !source.getMonth().isEmpty()) {
                                logger.error("Найдена форма-источник «%s» «%s» в текущем периоде в месяце «%s», которая имеет статус \"Принята\"!",
                                        source.getFormType().getName(), source.getFullDepartmentName(), source.getMonth());
                            } else {
                                logger.error("Найдена форма-источник «%s» «%s» в текущем периоде, которая имеет статус \"Принята\"!",
                                        source.getFormType().getName(), source.getFullDepartmentName());
                            }
                        }
                    }
                }
            } else {
                // Отчетный период подразделения НФ
                DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(
                        formData.getDepartmentReportPeriodId());

                // Назначения источников
                List<DepartmentFormType> sourceList = sourceService.getDFTSourcesByDFT(
                        departmentReportPeriod.getDepartmentId().intValue(), formData.getFormType().getId(), formData.getKind(),
                        departmentReportPeriod.getReportPeriod().getCalendarStartDate(),
                        departmentReportPeriod.getReportPeriod().getEndDate());

                // По назначениям
                for (DepartmentFormType departmentFormType : sourceList) {
                    if (!formTemplateService.existFormTemplate(departmentFormType.getFormTypeId(), formData.getReportPeriodId())) {
                        //не проверяем формы с неактивными макетами(или с макетами у которых изменили период актуальноси)
                        continue;
                    }
                    DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
                    filter.setDepartmentIdList(Arrays.asList(departmentFormType.getDepartmentId()));
                    filter.setReportPeriodIdList(Arrays.asList(formData.getReportPeriodId()));
                    filter.setIsCorrection(departmentReportPeriod.getCorrectionDate() != null);
                    filter.setCorrectionDate(departmentReportPeriod.getCorrectionDate());

                    List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.getListByFilter(filter);
                    if (departmentReportPeriodList == null || departmentReportPeriodList.isEmpty()) {
                        // Подходящий отчетный период подразделения не найден
                        continue;
                    }

                    if (departmentReportPeriodList.size() != 1) {
                        throw new ServiceException("Найдено более одного отчетного периода подразделения!");
                    }

                    // Отчетный период подразделения НФ-источника
                    DepartmentReportPeriod sourceDepartmentReportPeriod = departmentReportPeriodList.get(0);

                    Integer formTemplateId = formTemplateService.getActiveFormTemplateId(departmentFormType.getFormTypeId(), sourceDepartmentReportPeriod.getReportPeriod().getId());

                    if (!formTemplateService.isMonthly(formTemplateId)) {
                        // Экземпляр НФ-источника
                        FormData sourceFormData = formDataService.findFormData(departmentFormType.getFormTypeId(),
                                departmentFormType.getKind(), sourceDepartmentReportPeriod.getId(),
                                action.getFormData().getPeriodOrder());

                        if (sourceFormData == null) {
                            // Экземпляр НФ не найден
                            continue;
                        }

                        // Полное наименование подразделения
                        String departmentName = departmentService.getParentsHierarchy(departmentFormType.getDepartmentId());

                        if (sourceFormData.getState() == WorkflowState.ACCEPTED) {
                            logger.error("Найдена форма-источник «%s» «%s» в текущем периоде, которая имеет статус \"Принята\"!",
                                    sourceFormData.getFormType().getName(), departmentName);
                        }
                    } else {
                        List<Months> availableMonthList;
                        if (action.getFormData().getPeriodOrder() == null) {
                            availableMonthList = reportPeriodService.getAvailableMonthList(departmentReportPeriod.getReportPeriod().getId());
                        } else {
                            availableMonthList = new ArrayList<Months>();
                            availableMonthList.add(Months.fromId(action.getFormData().getPeriodOrder()));
                        }

                        for (Months months: availableMonthList) {
                            if (months == null)
                                continue;

                            // Экземпляр НФ-источника
                            FormData sourceFormData = formDataService.findFormData(departmentFormType.getFormTypeId(),
                                    departmentFormType.getKind(), sourceDepartmentReportPeriod.getId(),
                                    months.getId());

                            if (sourceFormData == null) {
                                // Экземпляр НФ не найден
                                continue;
                            }

                            // Полное наименование подразделения
                            String departmentName = departmentService.getParentsHierarchy(departmentFormType.getDepartmentId());

                            if (sourceFormData.getState() == WorkflowState.ACCEPTED) {
                                // Есть принятый источник
                                logger.error("Найдена форма-источник «%s» «%s» в текущем периоде в месяце «%s», которая имеет статус \"Принята\"!",
                                        sourceFormData.getFormType().getName(), departmentName, months.getTitle());
                            }
                        }
                    }
                }
            }

            if (logger.containsLevel(LogLevel.ERROR)) {
                result.setUuid(logEntryService.save(logger.getEntries()));
                return result;
            }

            // Удаление при отсутствии ошибок
            formDataService.deleteFormData(logger, securityService.currentUserInfo(), action.getFormDataId(), false);
            return result;
        } else {
            throw new ActionException("Форма заблокирована и не может быть изменена. Попробуйте выполнить операцию позже.");
        }
    }

	@Override
	public void undo(DeleteFormDataAction action, DeleteFormDataResult result,
			ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
