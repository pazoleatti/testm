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
		
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();
		GetFormDataResult result = new GetFormDataResult();
		Logger logger = new Logger();

		FormData formData;
		if (action.getWorkFlowMove() != null) {
			formDataService.doMove(action.getFormDataId(), userId, action.getWorkFlowMove(), logger);
		}
		if(action.getFormDataId() == Long.MAX_VALUE){
			formData = formDataService.createFormData(logger, userId, formTemplateService.getActiveFormTemplateId(action.getFormDataTypeId().intValue()), action.getDepartmentId(),
					FormDataKind.fromId(action.getFormDataKind().intValue()));

			result.setReportPeriod(reportPeriodDao.get(formData.getReportPeriodId()).getName());
			result.setDepartmenName(departmentDao.getDepartment(action.getDepartmentId()).getName());

			FormDataAccessParams accessParams = new FormDataAccessParams();
			accessParams.setCanDelete(false);
			accessParams.setCanEdit(true);
			accessParams.setCanRead(true);
			accessParams.setAvailableWorkflowMoves(new ArrayList<WorkflowMove>(0));
			result.setFormDataAccessParams(accessParams);
		} else{
			if(action.isLockFormData()){
				//Если пользователь открывает НФ для редактирования, то блокируем ее
				formDataService.lock(action.getFormDataId(), userId);
			}

			//Собираем информацию о блокировке НФ
			setLockInformation(userId, action.getFormDataId(), result);

			formData = formDataService.getFormData(userId, action.getFormDataId(), logger);
			result.setReportPeriod(reportPeriodDao.get(formData.getReportPeriodId()).getName());
			result.setDepartmenName(departmentDao.getDepartment(formData.getDepartmentId()).getName());
			Long formDataId = formData.getId();
			FormDataAccessParams accessParams = accessService.getFormDataAccessParams(userId, formDataId);
			result.setFormDataAccessParams(accessParams);
		}
		result.setNumberedHeader(formTemplateService.get(formData.getFormTemplateId()).isNumberedColumns());
		result.setAllStyles(formTemplateService.get(formData.getFormTemplateId()).getStyles());
		result.setLogEntries(logger.getEntries());
		result.setFormData(formData);

		return result;
	}

	@Override
	public void undo(GetFormData action, GetFormDataResult result, ExecutionContext context) throws ActionException {
		// Ничего не делаем
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

	private void setLockInformation(int userId, long formDataId, GetFormDataResult result){
		ObjectLock lockInformation = formDataService.getObjectLock(formDataId);
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
			//Если данная форма никем не заблокирована
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
