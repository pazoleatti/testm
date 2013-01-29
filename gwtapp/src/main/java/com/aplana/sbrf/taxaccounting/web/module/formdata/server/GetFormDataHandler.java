package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.WrongInputDataServiceException;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Каноничный пример антипаттерна "Волшебный хандлер".
 * TODO: Разделить класс на 3: показ, создание формы, изменение статуса формы. Result можно оставить общий.
 *       Непонятно. При изменении статуса и переходе по воркфлоу нужно получать форму обратно. Логика одинаковая, 
 *       только добавляется переход. 
 */
@Service
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
	private DepartmentDao departmentDao;
	
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
			formData = formDataService.getFormData(userId, action.getFormDataId(), logger);
		}
		result.setReportPeriod(reportPeriodDao.get(formData.getReportPeriodId()).getName());
		result.setDepartmenName(departmentDao.getDepartment(formData.getDepartmentId()).getName());
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
		if(action.isLockFormData() && action.getFormDataId() != Long.MAX_VALUE){
			// Если пользователь открывает НФ для редактирования, и это не новая форма, то пытаемся блокировать её
			if (!formDataService.lock(action.getFormDataId(), userId)){
				throw new WrongInputDataServiceException("форма уже редактируется другим пользователем");
			}
		}

		ObjectLock<Long> lockInformation = formDataService.getObjectLock(action.getFormDataId());
		if(lockInformation != null){
			//Если данная форма уже заблокирована другим пользотелем
			result.setFormDataLocked(true);
			result.setLockedByUser(securityService.getUserById(lockInformation.getUserId()).getName());
			result.setLockDate(getFormedDate(lockInformation.getLockTime()));
			if(lockInformation.getUserId() == userId){
				result.setLockedByCurrentUser(true);
			} else {
				result.setLockedByCurrentUser(false);
			}
		} else {
			//Если данная форма никем не заблокирована или это новая форма
			result.setFormDataLocked(false);
		}
	}

	private static String getFormedDate(Date dateToForm){
		//Преобразуем Date в строку вида "dd.mm.yyyy hh:mm"
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		formatter.format(dateToForm);
		return (formatter.format(dateToForm));
	}
}
