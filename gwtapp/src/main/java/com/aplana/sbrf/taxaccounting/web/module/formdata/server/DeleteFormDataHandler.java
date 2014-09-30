package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormToFormRelation;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

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

	public DeleteFormDataHandler() {
		super(DeleteFormDataAction.class);
	}

	@Override
    public DeleteFormDataResult execute(DeleteFormDataAction action, ExecutionContext context) throws ActionException {
        DeleteFormDataResult result = new DeleteFormDataResult();
        FormData formData = action.getFormData();

        if (!action.isManual()) {
            // проверка существования принятых форм-источников
            List<FormToFormRelation> formDataList = sourceService.getRelations(
                    formData.getDepartmentId(),
                    formData.getFormType().getId(),
                    formData.getKind(),
                    formData.getDepartmentReportPeriodId(),
                    formData.getPeriodOrder());
            Logger logger = new Logger();
            // TODO Левыкин: можно оптимизировать, если добавить специализированный метод в сервис
            for (FormToFormRelation item : formDataList) {
                if (item.isSource() && item.isCreated() && item.getState().equals(WorkflowState.ACCEPTED)) {
                    logger.error("Найдена форма-источник «%s», «%s» которая имеет статус \"Принята\"!",
                            item.getFormType().getName(), item.getFullDepartmentName());
                }
            }
            if (logger.containsLevel(LogLevel.ERROR)) {
                result.setUuid(logEntryService.save(logger.getEntries()));
                return result;
            }
        }

        formDataService.deleteFormData(new Logger(), securityService.currentUserInfo(), action.getFormDataId(), action.isManual());
        return result;
    }

	@Override
	public void undo(DeleteFormDataAction action, DeleteFormDataResult result,
			ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
