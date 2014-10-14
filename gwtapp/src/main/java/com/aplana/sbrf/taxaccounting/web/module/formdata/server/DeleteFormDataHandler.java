package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
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

import java.util.Arrays;
import java.util.List;

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

	public DeleteFormDataHandler() {
		super(DeleteFormDataAction.class);
	}

    @Override
    public DeleteFormDataResult execute(DeleteFormDataAction action, ExecutionContext context) throws ActionException {
        // Нажатие на кнопку "Удалить" http://conf.aplana.com/pages/viewpage.action?pageId=11384485
        DeleteFormDataResult result = new DeleteFormDataResult();
        Logger logger = new Logger();

        // Версия ручного ввода удаляется без проверок
        if (action.isManual()) {
            formDataService.deleteFormData(logger, securityService.currentUserInfo(), action.getFormDataId(), true);
            return result;
        }

        FormData formData = action.getFormData();
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
                // Есть принятый источник
                String str = departmentReportPeriod.getCorrectionDate() == null ? "" :
                        " в текущем корректирующем периоде";
                logger.error("Найдена форма-источник «%s», «%s»%s, которая имеет статус \"Принята\"!",
                        sourceFormData.getFormType().getName(), departmentName, str);
            }
        }

        if (logger.containsLevel(LogLevel.ERROR)) {
            result.setUuid(logEntryService.save(logger.getEntries()));
            return result;
        }

        // Удаление при отсутствии ошибок
        formDataService.deleteFormData(logger, securityService.currentUserInfo(), action.getFormDataId(), false);
        return result;
    }

	@Override
	public void undo(DeleteFormDataAction action, DeleteFormDataResult result,
			ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
