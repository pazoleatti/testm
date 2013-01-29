package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
// TODO: добавить учёт ролей пользователя, но для прототипа достаточно только привязки к депаратаментам
public class FormDataAccessServiceImpl implements FormDataAccessService {
	private Log logger = LogFactory.getLog(getClass());	
	
	@Autowired
	private TAUserDao userDao;
	@Autowired
	private FormDataDao formDataDao;
	@Autowired
	private DepartmentDao departmentDao;
	@Autowired
	private ReportPeriodDao reportPeriodDao;
	
	@Override
	public boolean canRead(int userId, long formDataId) {
		TAUser user = userDao.getUser(userId);
		FormData formData = formDataDao.getWithoutRows(formDataId);
		Department userDepartment = departmentDao.getDepartment(user.getDepartmentId());
		Department formDataDepartment = departmentDao.getDepartment(formData.getDepartmentId());
		return canRead(user, userDepartment, formData, formDataDepartment);
	}

	private boolean canRead(TAUser user, Department userDepartment, FormData formData, Department formDataDepartment) {
		final boolean isControllerOfCurrentLevel = user.hasRole(TARole.ROLE_CONTROL)
				&& (user.getDepartmentId() == formDataDepartment.getId());
		final boolean isControllerOfUpLevel = formDataDepartment.getParentId() != null && user.hasRole(TARole.ROLE_CONTROL)
				&& (userDepartment.getId() == formDataDepartment.getParentId());
		final boolean isControllerOfUNP = user.hasRole(TARole.ROLE_CONTROL_UNP);
		final boolean isBankLevelFormData = formDataDepartment.getType() == DepartmentType.ROOT_BANK;

		if(isBankLevelFormData && formData.getKind() == FormDataKind.ADDITIONAL){
			/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и не передаваемых на вышестоящий уровень (Выходные формы уровня БАНК)
			 Логика, согласно Бизнес-требованиям:
             ------------------------------------------------------------------
             |              |                Пользователь                     |
             |  Состояние   |-------------------------------------------------|
             |              |Оператор|Контролер ТУ|Контролер ВСУ|Контролер УНП|
             ------------------------------------------------------------------
             | Создана      |   +    |     +      |      +      |      +      |
             ---------------|-------------------------------------------------|
             | Подготовлена |   +    |     +      |      +      |      +      |
             ---------------|-------------------------------------------------|
             | Принята      |   +    |     +      |      +      |      +      |
             ---------------|--------------------------------------------------
			 */
			return true;
		} else if (isBankLevelFormData && formData.getKind() == FormDataKind.SUMMARY){
			/*Жизненный цикл налоговых форм, формируемых автоматически
			 и не передаваемых на вышестоящий уровень (Сводные формы уровня БАНК)
			 			 Логика, согласно Бизнес-требованиям:
		     ------------------------------------------------------------------
             |              |                Пользователь                     |
             |  Состояние   |-------------------------------------------------|
             |              |Оператор|Контролер ТУ|Контролер ВСУ|Контролер УНП|
             ------------------------------------------------------------------
             | Создана      |   -    |     +      |      +      |      +      |
             ---------------|-------------------------------------------------|
             | Принята      |   -    |     +      |      +      |      +      |
             ---------------|--------------------------------------------------
			 */

			if(isControllerOfCurrentLevel || isControllerOfUpLevel || isControllerOfUNP){
				return true;
			}
		} else if (!isBankLevelFormData && formData.getKind() == FormDataKind.SUMMARY){
			/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень (Сводные формы (кроме уровня БАНК)
            Логика, согласно Бизнес-требованиям:
             ------------------------------------------------------------------
             |              |                Пользователь                     |
             |  Состояние   |-------------------------------------------------|
             |              |Оператор|Контролер ТУ|Контролер ВСУ|Контролер УНП|
             ------------------------------------------------------------------
             | Создана      |   -    |     +      |      +      |      +      |
             ---------------|-------------------------------------------------|
             | Утверждена   |   -    |     +      |      +      |      +      |
             ---------------|-------------------------------------------------|
             | Принята      |   -    |     +      |      +      |      +      |
             ---------------|--------------------------------------------------
			*/
			if(isControllerOfCurrentLevel || isControllerOfUpLevel || isControllerOfUNP){
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean canEdit(int userId, long formDataId) {
		TAUser user = userDao.getUser(userId);
		FormData formData = formDataDao.getWithoutRows(formDataId);
		Department userDepartment = departmentDao.getDepartment(user.getDepartmentId());
		Department formDataDepartment = departmentDao.getDepartment(formData.getDepartmentId());
		return canEdit(user, userDepartment, formData, formDataDepartment, reportPeriodDao.get(formData.getReportPeriodId()));
	}

	private boolean canEdit(TAUser user, Department userDepartment, FormData formData, Department formDataDepartment,
	                        ReportPeriod formDataReportPeriod) {
		WorkflowState state = formData.getState();
		if(!formDataReportPeriod.isActive() || state == WorkflowState.ACCEPTED){
			//Нельзя редактировать НФ для неактивного налогового периода или если НФ находится в состоянии "Принята"
			return false;
		}

		final boolean isControllerOfCurrentLevel = user.hasRole(TARole.ROLE_CONTROL)
				&& (user.getDepartmentId() == formDataDepartment.getId());
		final boolean isControllerOfUpLevel = formDataDepartment.getParentId() != null && user.hasRole(TARole.ROLE_CONTROL)
				&& (userDepartment.getId() == formDataDepartment.getParentId());
		final boolean isControllerOfUNP = user.hasRole(TARole.ROLE_CONTROL_UNP);
		final boolean isBankLevelFormData = formDataDepartment.getType() == DepartmentType.ROOT_BANK;

		if(isBankLevelFormData && formData.getKind() == FormDataKind.ADDITIONAL){
			/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и не передаваемых на вышестоящий уровень (Выходные формы уровня БАНК)
             Логика, согласно Бизнес-требованиям:
             ------------------------------------------------------------------
             |              |                Пользователь                     |
             |  Состояние   |-------------------------------------------------|
             |              |Оператор|Контролер ТУ|Контролер ВСУ|Контролер УНП|
             ------------------------------------------------------------------
             | Создана      |   +    |     +      |      +      |      +      |
             ---------------|-------------------------------------------------|
             | Подготовлена |   -    |     +      |      +      |      +      |
             ---------------|-------------------------------------------------|
             | Принята      |   -    |     -      |      -      |      -      |
             ---------------|--------------------------------------------------
             *Контролер ТУ - Контролер текущего уровня
             *Контролер ВСУ - Контролер вышестоящего уровня
			 */
			switch (formData.getState()){
				case CREATED:
					return true;
				case PREPARED:
					if(isControllerOfCurrentLevel || isControllerOfUpLevel || isControllerOfUNP){
						return true;
					} else {
						return false;
					}
				default:
					logger.warn("Bank-level formData with " + formData.getKind().getName() + " kind, couldn't be in "
							+ state.getName() +" state!");
			}
		} else if (isBankLevelFormData && formData.getKind() == FormDataKind.SUMMARY){
			/*Жизненный цикл налоговых форм, формируемых автоматически
			 и не передаваемых на вышестоящий уровень (Сводные формы уровня БАНК)
			 Логика, согласно Бизнес-требованиям:
		     ------------------------------------------------------------------
             |              |                Пользователь                     |
             |  Состояние   |-------------------------------------------------|
             |              |Оператор|Контролер ТУ|Контролер ВСУ|Контролер УНП|
             ------------------------------------------------------------------
             | Создана      |   -    |     +      |      +      |      +      |
             ---------------|-------------------------------------------------|
             | Принята      |   -    |     -      |      -      |      -      |
             ---------------|--------------------------------------------------
			 */
			if(isControllerOfCurrentLevel || isControllerOfUpLevel || isControllerOfUNP){
				return true;
			}
		} else if (!isBankLevelFormData && formData.getKind() == FormDataKind.SUMMARY){
			/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень (Сводные формы (кроме уровня БАНК)
			 Логика, согласно Бизнес-требованиям:
             ------------------------------------------------------------------
             |              |                Пользователь                     |
             |  Состояние   |-------------------------------------------------|
             |              |Оператор|Контролер ТУ|Контролер ВСУ|Контролер УНП|
             ------------------------------------------------------------------
             | Создана      |   -    |     -      |      -      |      -      |
             ---------------|-------------------------------------------------|
             | Утверждена   |   -    |     -      |      -      |      -      |
             ---------------|-------------------------------------------------|
             | Принята      |   -    |     -      |      -      |      -      |
             ---------------|--------------------------------------------------
			*/
			return false;
		}
		return false;
	}

	@Override
	public boolean canCreate(int userId, int formTemplateId, FormDataKind kind, int departmentId) {
		TAUser user = userDao.getUser(userId);
		Department userDepartment = departmentDao.getDepartment(user.getDepartmentId());
		return canCreate(user, userDepartment, formTemplateId, kind, departmentId);
	}
	
	private boolean canCreate(TAUser user, Department userDepartment, int formTemplateId, FormDataKind kind, int departmentId) {
		if (departmentId == user.getDepartmentId()) {
			return true;
		}
		return userDepartment.getType() == DepartmentType.ROOT_BANK && user.hasRole(TARole.ROLE_CONTROL);
	}

	@Override
	public boolean canDelete(int userId, long formDataId) {
		FormData formData = formDataDao.getWithoutRows(formDataId);
		TAUser user = userDao.getUser(userId);
		Department userDepartment = departmentDao.getDepartment(user.getDepartmentId());
		Department formDataDepartment = departmentDao.getDepartment(formData.getDepartmentId());
		return canDelete(user, userDepartment, formData, formDataDepartment);
	}
	
	private boolean canDelete(TAUser user, Department userDepartment, FormData formData, Department formDataDepartment) {
        return formData.getState() == WorkflowState.CREATED && canEdit(user, userDepartment, formData, formDataDepartment,
                reportPeriodDao.get(formData.getReportPeriodId()));
    }
	
	
	@Override
	public List<WorkflowMove> getAvailableMoves(int userId, long formDataId) {
		TAUser user = userDao.getUser(userId);		
		Department userDepartment = departmentDao.getDepartment(user.getDepartmentId());
		FormData formData = formDataDao.getWithoutRows(formDataId);
		Department formDataDepartment = departmentDao.getDepartment(formData.getDepartmentId());
		return getAvailableMoves(user, userDepartment, formData, formDataDepartment,
                reportPeriodDao.get(formData.getReportPeriodId()));
	}

	private List<WorkflowMove> getAvailableMoves(TAUser user, Department userDepartment, FormData formData,
	                                             Department formDataDepartment, ReportPeriod formDataReportPeriod) {
		List<WorkflowMove> result = new ArrayList<WorkflowMove>();
		// Для того, чтобы иметь возможность изменить статус, у пользователя должны быть права
		// на чтение соответствующей карточки данных и отчетный период должен быть активным
		if (!canRead(user, userDepartment, formData, formDataDepartment) || !formDataReportPeriod.isActive()) {
			return result;
		}
		WorkflowState state = formData.getState();

		final boolean isBankLevelFormData = formDataDepartment.getType() == DepartmentType.ROOT_BANK;
		final boolean isControllerOfCurrentLevel = user.hasRole(TARole.ROLE_CONTROL)
				&& (user.getDepartmentId() == formDataDepartment.getId());
		final boolean isControllerOfUpLevel = formDataDepartment.getParentId() != null && user.hasRole(TARole.ROLE_CONTROL)
				&& (userDepartment.getId() == formDataDepartment.getParentId());
		final boolean isControllerOfUNP = user.hasRole(TARole.ROLE_CONTROL_UNP);

		if(isBankLevelFormData && formData.getKind() == FormDataKind.ADDITIONAL){
			/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и не передаваемых на вышестоящий уровень (Выходные формы уровня БАНК)*/
			switch (state){
				case CREATED:
					result.add(WorkflowMove.CREATED_TO_PREPARED);
					break;
				case PREPARED:
					if(isControllerOfCurrentLevel || isControllerOfUpLevel || isControllerOfUNP){
						result.add(WorkflowMove.PREPARED_TO_CREATED);
						result.add(WorkflowMove.PREPARED_TO_ACCEPTED);
					}
					break;
				case ACCEPTED:
					if(isControllerOfUpLevel || isControllerOfUNP){
						result.add(WorkflowMove.ACCEPTED_TO_PREPARED);
					}
					break;
				default:
					logger.warn("Bank-level formData with " + formData.getKind().getName() + " kind, couldn't be in "
							+ state.getName() +" state!");
			}
		} else if (isBankLevelFormData && formData.getKind() == FormDataKind.SUMMARY){
			/*Жизненный цикл налоговых форм, формируемых автоматически
			 и не передаваемых на вышестоящий уровень (Сводные формы уровня БАНК)*/
			switch (state){
				case CREATED:
					if(isControllerOfCurrentLevel || isControllerOfUpLevel || isControllerOfUNP){
						result.add(WorkflowMove.CREATED_TO_ACCEPTED);
					}
					break;
				case ACCEPTED:
					if(isControllerOfUpLevel || isControllerOfUNP){
						result.add(WorkflowMove.ACCEPTED_TO_CREATED);
					}
					break;
				default:
					logger.warn("Bank-level formData with " + formData.getKind().getName() + " kind, couldn't be in "
							+ state.getName() +" state!");
			}
		} else if (!isBankLevelFormData && formData.getKind() == FormDataKind.SUMMARY){
			/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень (Сводные формы (кроме уровня БАНК)*/
			switch (state){
				case CREATED:
					if(isControllerOfCurrentLevel || isControllerOfUpLevel || isControllerOfUNP){
						result.add(WorkflowMove.CREATED_TO_APPROVED);
					}
					break;
				case APPROVED:
					if(isControllerOfUpLevel || isControllerOfUNP){
						result.add(WorkflowMove.APPROVED_TO_ACCEPTED);
						result.add(WorkflowMove.APPROVED_TO_CREATED);
					}
					break;
				case ACCEPTED:
					if(isControllerOfUpLevel || isControllerOfUNP){
						result.add(WorkflowMove.ACCEPTED_TO_APPROVED);
					}
					break;
				default:
					logger.warn("FormData with " + formData.getKind().getName() + " kind, couldn't be in "
							+ state.getName() +" state!");
			}
		}
		return result;
	}

	@Override
	public FormDataAccessParams getFormDataAccessParams(int userId,	long formDataId) {
		TAUser user = userDao.getUser(userId);
		FormData formData = formDataDao.getWithoutRows(formDataId);
		Department userDepartment = departmentDao.getDepartment(user.getDepartmentId());
		Department formDataDepartment = departmentDao.getDepartment(formData.getDepartmentId());
		ReportPeriod reportPeriod = reportPeriodDao.get(formData.getReportPeriodId());

		FormDataAccessParams result = new FormDataAccessParams();
		result.setCanRead(canRead(user, userDepartment, formData, formDataDepartment));
		result.setCanEdit(canEdit(user, userDepartment, formData, formDataDepartment, reportPeriod));
		result.setCanDelete(canDelete(user, userDepartment, formData, formDataDepartment));
		result.setAvailableWorkflowMoves(getAvailableMoves(user, userDepartment, formData, formDataDepartment, reportPeriod));
		return result;
	}
}