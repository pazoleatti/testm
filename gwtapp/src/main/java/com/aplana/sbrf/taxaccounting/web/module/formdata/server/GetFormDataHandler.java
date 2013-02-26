package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataAccessParams;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.ObjectLock;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult.FormMode;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.WrongInputDataServiceException;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;


@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetFormDataHandler extends AbstractActionHandler<GetFormData, GetFormDataResult>{

	@Autowired
	private FormDataAccessService accessService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private ReportPeriodDao reportPeriodDao;
	
	@Autowired
	@Qualifier("departmentService")
	private DepartmentService departmentService;
	
	@Autowired
	FormTemplateService formTemplateService;

	public GetFormDataHandler() {
		super(GetFormData.class);
	}
	
	@Override
	public GetFormDataResult execute(GetFormData action, ExecutionContext context) throws ActionException, WrongInputDataServiceException {
		checkAction(action);

		Integer userId = securityService.currentUser().getId();
		GetFormDataResult result = new GetFormDataResult();
		Logger logger = new Logger();
	
		fillLockData(action, userId, logger, result);
		workFlowMove(action, userId, logger);
		fillFormAndTemplateData(action, userId, logger, result);
		fillFormDataAccessParams(action, userId, logger, result);
		
		result.setLogEntries(logger.getEntries());
		return result;
	}

	@Override
	public void undo(GetFormData action, GetFormDataResult result, ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
	
	/**
	 * Выполняет переход по workflow, если это необходимо
	 *  
	 * @param action
	 * @param userId
	 * @param logger
	 */
	private void workFlowMove(GetFormData action, int userId, Logger logger){
		if (action.getWorkFlowMove() != null) {
			// Если необходимо выполнить переход, то выполняем его
			formDataService.doMove(action.getFormDataId(), userId, action.getWorkFlowMove(), logger); 
		}
	}
	
	
	/**
	 * Получает/создает данные налоговой формы
	 * 
	 * @param action
	 * @param userId
	 * @param logger
	 * @param result
	 */
	private void fillFormAndTemplateData(GetFormData action, int userId, Logger logger, GetFormDataResult result){
		FormData formData;
		if(action.getFormDataId() == Long.MAX_VALUE){
			formData = formDataService.createFormData(logger, userId, formTemplateService.getActiveFormTemplateId(action.getFormDataTypeId().intValue()), action.getDepartmentId(),
					FormDataKind.fromId(action.getFormDataKind().intValue()));
		} else{
			if (!action.isReadOnly()){
				formDataService.lock(action.getFormDataId(), userId);
			}
			formData = formDataService.getFormData(userId, action.getFormDataId(), logger);
		}
		result.setReportPeriod(reportPeriodDao.get(formData.getReportPeriodId()).getName());
		result.setDepartmenName(departmentService.getDepartment(formData.getDepartmentId()).getName());
		result.setNumberedHeader(formTemplateService.get(formData.getFormTemplateId()).isNumberedColumns());
		result.setAllStyles(formTemplateService.get(formData.getFormTemplateId()).getStyles());
		result.setFormData(formData);
	}
	
	/**
	 * Заполняет параметры доступа для формы
	 * 
	 * @param action
	 * @param userId
	 * @param logger
	 * @param result
	 */
	private void fillFormDataAccessParams(GetFormData action, int userId, Logger logger, GetFormDataResult result){
		FormDataAccessParams accessParams;
		if(action.getFormDataId() == Long.MAX_VALUE){
			accessParams = new FormDataAccessParams();
			accessParams.setCanDelete(false);
			accessParams.setCanEdit(true);
			accessParams.setCanRead(true);
			accessParams.setAvailableWorkflowMoves(new ArrayList<WorkflowMove>(0));
		} else {
		    accessParams = accessService.getFormDataAccessParams(userId, result.getFormData().getId());
		}
		result.setFormDataAccessParams(accessParams);
	}
	
	private void checkAction(GetFormData action) throws WrongInputDataServiceException {
		String errorMessage = "";
		if (action.getFormDataId() == Long.MAX_VALUE) {
			if (action.getFormDataTypeId() == null) {
				errorMessage += "не указан вид формы";
			}
			if (action.getFormDataKind() == null) {
				errorMessage += ", не указан тип формы";
			}
			if (action.getDepartmentId() == null) {
				errorMessage += ", не указано подразделение";
			}
			if (!errorMessage.isEmpty()) {
				errorMessage =  errorMessage.startsWith(",") ? errorMessage.substring(1): errorMessage;
				throw new WrongInputDataServiceException(errorMessage);
			}
		}
	}
	
	/**
	 * Блокирует форму при необходимости, заполняет состояние блокировки
	 * 
	 * @param action
	 * @param userId
	 * @param logger
	 * @param result
	 * @throws WrongInputDataServiceException
	 */
	private void fillLockData(GetFormData action, int userId, Logger logger, GetFormDataResult result) throws WrongInputDataServiceException{		
		FormMode formMode = FormMode.READ_LOCKED;
		
		ObjectLock<Long> lockInformation = formDataService.getObjectLock(action.getFormDataId());
		if(lockInformation != null){
			//Если данная форма уже заблокирована другим пользотелем

			result.setLockedByUser(securityService.getUserById(lockInformation.getUserId()).getName());
			result.setLockDate(getFormedDate(lockInformation.getLockTime()));
			if(lockInformation.getUserId() == userId){
				if (action.isReadOnly()){
					formMode = FormMode.READ_UNLOCKED;
				} else {
					formMode = FormMode.EDIT;
				}
			} 
		} else {
			//Если данная форма никем не заблокирована или это новая форма
			 if  (action.getFormDataId() == Long.MAX_VALUE){
				 formMode = FormMode.EDIT;
			 } else {
				 if (action.isReadOnly()){
					 formMode = FormMode.READ_UNLOCKED;
				 } else {
					 formMode = FormMode.EDIT; 
				 }
			 }
		}
		result.setFormMode(formMode);
	}

	private static String getFormedDate(Date dateToForm){
		//Преобразуем Date в строку вида "dd.mm.yyyy hh:mm"
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		formatter.format(dateToForm);
		return (formatter.format(dateToForm));
	}
}
