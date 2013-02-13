package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataAccessParams;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;

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
	@Autowired
	private FormTemplateDao formTemplateDao;
	@Autowired
	private DepartmentFormTypeDao departmentFormTypeDao;
	
	
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
             | Принята      |   -    |     -      |      -      |      -      |-> это состояние хэндлится в начале функции
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
             | Создана      |   -    |     -      |      -      |      -      |
             ---------------|-------------------------------------------------|
             | Принята      |   -    |     -      |      -      |      -      |
             ---------------|--------------------------------------------------
			 */
			return false;
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
             | Утверждена   |   -    |     -      |      -      |      -      |
             ---------------|-------------------------------------------------|
             | Принята      |   -    |     -      |      -      |      -      |-> это состояние хэндлится в начале функции
             ---------------|--------------------------------------------------
			*/
			switch (state){
				case CREATED:
					if(isControllerOfCurrentLevel || isControllerOfUpLevel || isControllerOfUNP){
						return true;
					}
					break;
				case APPROVED:
					return false;
				default:
					logger.warn("Bank-level formData with " + formData.getKind().getName() + " kind, couldn't be in "
							+ state.getName() +" state!");
			}
		}
		return false;
	}

	@Override
	public boolean canCreate(int userId, int formTemplateId, FormDataKind kind, int departmentId) {
		TAUser user = userDao.getUser(userId);
		Department userDepartment = departmentDao.getDepartment(user.getDepartmentId());
		Department formDataDepartment = departmentDao.getDepartment(departmentId);
		FormTemplate formTemplate = formTemplateDao.get(formTemplateId);
		return canCreate(user, userDepartment, formTemplate, kind, formDataDepartment);
	}
	
	private boolean canCreate(TAUser user, Department userDepartment, FormTemplate formTemplate, FormDataKind kind, Department formDataDepartment) {
		int formDataDepartmentId = formDataDepartment.getId();
		FormType formType = formTemplate.getType();
		int formTypeId = formType.getId();
		int userDepartmentId = user.getDepartmentId();
		
		// Проверяем, что в подразделении вообще можно работать с формами такого вида и типа
		boolean found = false;
		for (DepartmentFormType dft: formDataDepartment.getDepartmentFormTypes()) {
			if (dft.getFormTypeId() == formTypeId && dft.getKind() == kind) {
				found = true;
				break;
			}
		}
		if (!found) {
			return false;
		}
		
		// Контролёр УНП может создать любую форму в любом подразделении
		if (user.hasRole(TARole.ROLE_CONTROL_UNP)) {
			return true;
		}
		
		// Оператор может создать форму, только в своём подразделении
		// Причём только первичные и выходные
		if (user.hasRole(TARole.ROLE_OPERATOR) && userDepartmentId == formDataDepartmentId && (kind == FormDataKind.PRIMARY || kind == FormDataKind.ADDITIONAL)) {
			return true;
		}
		
		// Контролёр может создавать любую налоговую форму в своём подразделении
		// контролёр может создавать налоговые формы в "чужих" подразделениях, при условии, 
		// что созданные им там формы будут источником данных для форм подразделения, к которому 
		// относится контролёр
		if (user.hasRole(TARole.ROLE_CONTROL)) {
			if (userDepartmentId == formDataDepartmentId) {
				return true;
			} else {
				// TODO: если будут проблемы с производительностью,
				// то можно вместо этого цикла сделать отдельный Dao-метод со 
				// специализированным запросом
				List<DepartmentFormType> destanations = departmentFormTypeDao.getFormDestinations(
					formDataDepartmentId, 
					formTypeId, 
					kind
				);
				for (DepartmentFormType dft: destanations) {
					if (dft.getDepartmentId() == userDepartmentId) {
						return true;
					}
				}
				
				// TODO: обсуждался случай, когда форма в чужом подразделении является источником для 
				// другой формы в этом же подразделении, а уже эта вторая форма
				// является источником для одной из форм подразделения, к которому относится контролёр				
			}
		}
		return false;
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