package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;

@Service
public class FormDataAccessServiceImpl implements FormDataAccessService {

	public static final String LOG_EVENT_AVAILABLE_MOVES = "LOG_EVENT_AVAILABLE_MOVES";
	public static final String LOG_EVENT_READ = "READ";
	public static final String LOG_EVENT_EDIT = "EDIT";
	public static final String LOG_EVENT_CREATE = "CREATE";

	private Log logger = LogFactory.getLog(getClass());

	private final static String FORMDATA_KIND_STATE_ERROR = "Event type: \"%s\". Unsuppotable case for formData with \"%s\" kind and \"%s\" state!";

	@Autowired
	private FormDataDao formDataDao;
	@Autowired
	private DepartmentService departmentService;
	@Autowired
	private ReportPeriodDao reportPeriodDao;
	@Autowired
	private FormTemplateDao formTemplateDao;
	@Autowired
	private DepartmentFormTypeDao departmentFormTypeDao;

	@Override
	public boolean canRead(TAUserInfo userInfo, long formDataId) {
		FormData formData = formDataDao.getWithoutRows(formDataId);
		FormDataAccessRoles formDataAccess = getFormDataUserAccess(userInfo.getUser(), formData.getDepartmentId(),
				formData.getFormType().getId(), formData.getKind());
		boolean result = canRead(formDataAccess, formData);
		if (logger.isDebugEnabled()) {
			logger.debug("canRead: " + result);
		}
		return result;
	}

	private boolean canRead(FormDataAccessRoles formDataAccess, FormData formData) {
		if((formData.getKind() == FormDataKind.ADDITIONAL || formData.getKind() == FormDataKind.PRIMARY) &&
				formDataAccess.isFormDataHasDestinations()){
			/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и передаваемых на вышестоящий уровень.

			 Виды форм:
				 Первичная налоговая форма;
				 Выходная налоговая форма;

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
		}
		if((formData.getKind() == FormDataKind.ADDITIONAL || formData.getKind() == FormDataKind.PRIMARY ||
				formData.getKind() == FormDataKind.UNP) && !formDataAccess.isFormDataHasDestinations()){
			/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и НЕ передаваемых на вышестоящий уровень.

			 Виды форм:
				 Первичная налоговая форма;
				 Выходная налоговая форма;
				 Форма УНП.

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
		} else if ((formData.getKind() == FormDataKind.SUMMARY || formData.getKind() == FormDataKind.CONSOLIDATED)
				&& !formDataAccess.isFormDataHasDestinations()){
			/*Жизненный цикл налоговых форм, формируемых автоматически
			 и НЕ передаваемых на вышестоящий уровень

			 Виды форм:
             	Консолидированная налоговая форма;
				Сводная налоговая форма подразделения;
				Сводная налоговая форма Банка.

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
			return formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
					formDataAccess.isControllerOfUNP();
		} else if ((formData.getKind() == FormDataKind.SUMMARY || formData.getKind() == FormDataKind.CONSOLIDATED)
				&& formDataAccess.isFormDataHasDestinations()){
			/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень

             Виды форм:
             	Консолидированная налоговая форма;
				Сводная налоговая форма подразделения.

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
			return formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
					formDataAccess.isControllerOfUNP();
		}
		logger.warn(String.format(FORMDATA_KIND_STATE_ERROR, LOG_EVENT_READ, formData.getKind().getName(), formData.getState().getName()));
		return false;
	}

	@Override
	public boolean canEdit(TAUserInfo userInfo, long formDataId) {
		FormData formData = formDataDao.getWithoutRows(formDataId);
		FormDataAccessRoles formDataAccess = getFormDataUserAccess(userInfo.getUser(), formData.getDepartmentId(),
				formData.getFormType().getId(), formData.getKind());
		boolean result = canEdit(formDataAccess, formData, reportPeriodDao.get(formData.getReportPeriodId()));
		if (logger.isDebugEnabled()) {
			logger.debug("canEdit: " + result);
		}
		return result;
	}

	private boolean canEdit(FormDataAccessRoles formDataAccess, FormData formData, ReportPeriod formDataReportPeriod) {
		WorkflowState state = formData.getState();
		if(!formDataReportPeriod.isActive()){
			//Нельзя редактировать НФ для неактивного налогового периода
			return false;
		}
		if (formDataReportPeriod.isBalancePeriod()) {
			/* В отчетных периодах для ввода остатков редактирование возможно только контролерами для
			 НФ в статусе "Создана" */
			switch (state){
				case CREATED:
					return formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
							formDataAccess.isControllerOfUNP();
				case ACCEPTED:
					return false; //Нельзя редактировать НФ в состоянии "Принята"
			}
			logger.warn(String.format(FORMDATA_KIND_STATE_ERROR, LOG_EVENT_EDIT, formData.getKind().getName(), state.getName()));
		} else if((formData.getKind() == FormDataKind.ADDITIONAL || formData.getKind() == FormDataKind.PRIMARY) &&
				formDataAccess.isFormDataHasDestinations()){
			/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и передаваемых на вышестоящий уровень.

			 Виды форм:
				 Первичная налоговая форма;
				 Выходная налоговая форма;
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
					return formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
							formDataAccess.isControllerOfUNP();
				case ACCEPTED:
					return false; //Нельзя редактировать НФ в состоянии "Принята"
			}
			logger.warn(String.format(FORMDATA_KIND_STATE_ERROR, LOG_EVENT_EDIT, formData.getKind().getName(), state.getName()));
		}else if((formData.getKind() == FormDataKind.ADDITIONAL || formData.getKind() == FormDataKind.PRIMARY ||
				formData.getKind() == FormDataKind.UNP) && !formDataAccess.isFormDataHasDestinations()){
			/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и НЕ передаваемых на вышестоящий уровень.

			 Виды форм:
				 Первичная налоговая форма;
				 Выходная налоговая форма;
				 Форма УНП.
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
					return formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
							formDataAccess.isControllerOfUNP();
				case ACCEPTED:
					return false; //Нельзя редактировать НФ в состоянии "Принята"
			}
			logger.warn(String.format(FORMDATA_KIND_STATE_ERROR, LOG_EVENT_EDIT, formData.getKind().getName(), state.getName()));
		} else if ((formData.getKind() == FormDataKind.SUMMARY || formData.getKind() == FormDataKind.CONSOLIDATED) &&
				!formDataAccess.isFormDataHasDestinations()){
			/*Жизненный цикл налоговых форм, формируемых автоматически
			 и НЕ передаваемых на вышестоящий уровень

			 Виды форм:
             	Консолидированная налоговая форма;
				Сводная налоговая форма подразделения;
				Сводная налоговая форма Банка.
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
			switch (state){
				case CREATED:
					return formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
							formDataAccess.isControllerOfUNP(); //TODO (Marat Fayzullin 20.03.2013) временно до появления первичных форм
				case ACCEPTED:
					return false; //Нельзя редактировать НФ в состоянии "Принята"
			}
			logger.warn(String.format(FORMDATA_KIND_STATE_ERROR, LOG_EVENT_EDIT, formData.getKind().getName(), state.getName()));
		} else if ((formData.getKind() == FormDataKind.SUMMARY || formData.getKind() == FormDataKind.CONSOLIDATED) &&
				formDataAccess.isFormDataHasDestinations()){
			/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень

             Виды форм:
             	Консолидированная налоговая форма;
				Сводная налоговая форма подразделения.
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
					return formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
							formDataAccess.isControllerOfUNP();
				case APPROVED:
					return false;
				case ACCEPTED:
					return false; //Нельзя редактировать НФ в состоянии "Принята"
			}
			logger.warn(String.format(FORMDATA_KIND_STATE_ERROR, LOG_EVENT_EDIT, formData.getKind().getName(), state.getName()));
		}
		logger.warn(String.format(FORMDATA_KIND_STATE_ERROR, LOG_EVENT_EDIT, formData.getKind().getName(), state.getName()));
		return false;
	}

	@Override
	public boolean canCreate(TAUserInfo userInfo, int formTemplateId, FormDataKind kind, int departmentId, int reportPeriodId) {
		Department formDataDepartment = departmentService.getDepartment(departmentId);
		FormTemplate formTemplate = formTemplateDao.get(formTemplateId);
		int formTypeId = formTemplate.getType().getId();
		FormDataAccessRoles formDataAccess = getFormDataUserAccess(userInfo.getUser(), formDataDepartment.getId(), formTypeId, kind);
		boolean result = canCreate(formDataAccess, formTypeId, kind, formDataDepartment, reportPeriodDao.get(reportPeriodId));
		if (logger.isDebugEnabled()) {
			logger.debug("canCreate: " + result);
		}
		return result;
	}
	
	private boolean canCreate(FormDataAccessRoles formDataAccess, int formTypeId, FormDataKind kind, Department formDataDepartment, ReportPeriod reportPeriod) {
		if(!reportPeriod.isActive()){
			//Нельзя создавать НФ для неактивного налогового периода
			return false;
		}

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
		if (reportPeriod.isBalancePeriod()) {
			return formDataAccess.isControllerOfCurrentLevel() ||
					formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP();
		} else if((kind == FormDataKind.ADDITIONAL || kind == FormDataKind.PRIMARY) &&
				formDataAccess.isFormDataHasDestinations()){
			 /* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и передаваемых на вышестоящий уровень.

			 Виды форм:
				 Первичная налоговая форма;
				 Выходная налоговая форма; */
			return formDataAccess.isOperatorOfCurrentLevel() || formDataAccess.isControllerOfCurrentLevel() ||
					formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP();
		}else if((kind == FormDataKind.ADDITIONAL || kind == FormDataKind.PRIMARY ||
				kind == FormDataKind.UNP) && !formDataAccess.isFormDataHasDestinations()){
			 /* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и НЕ передаваемых на вышестоящий уровень.

			 Виды форм:
				 Первичная налоговая форма;
				 Выходная налоговая форма;
				 Форма УНП.*/
			return formDataAccess.isOperatorOfCurrentLevel() || formDataAccess.isControllerOfCurrentLevel() ||
				formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP();
		} else if ((kind == FormDataKind.SUMMARY || kind == FormDataKind.CONSOLIDATED) && !formDataAccess.isFormDataHasDestinations()){
			/*Жизненный цикл налоговых форм, формируемых автоматически
			 и НЕ передаваемых на вышестоящий уровень

			 Виды форм:
             	Консолидированная налоговая форма;
				Сводная налоговая форма подразделения;
				Сводная налоговая форма Банка.*/
			return formDataAccess.isControllerOfCurrentLevel() ||
					formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP();
			//return false; //TODO (Marat Fayzullin 21.03.2013) временно до появления первичных форм
		} else if ((kind == FormDataKind.SUMMARY || kind == FormDataKind.CONSOLIDATED) && formDataAccess.isFormDataHasDestinations()){
			/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень

             Виды форм:
             	Консолидированная налоговая форма;
				Сводная налоговая форма подразделения.*/
			return formDataAccess.isControllerOfCurrentLevel() ||
					formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP();
			//return false; //TODO: (Marat Fayzullin 19.02.2013) расскомментить после реализации первичных форм, так как автоматически создаваемые НФ создает система
		}
		logger.warn(String.format(FORMDATA_KIND_STATE_ERROR, LOG_EVENT_CREATE, kind.getName(), WorkflowState.CREATED.getName()));
		return false;
	}

	@Override
	public boolean canDelete(TAUserInfo userInfo, long formDataId) {
		FormData formData = formDataDao.getWithoutRows(formDataId);
		FormDataAccessRoles formDataAccess = getFormDataUserAccess(userInfo.getUser(), formData.getDepartmentId(),
				formData.getFormType().getId(), formData.getKind());
		boolean result = canDelete(formDataAccess, formData, reportPeriodDao.get(formData.getReportPeriodId()));
		if (logger.isDebugEnabled()) {
			logger.debug("canDelete: " + result);
		}
		return result;
	}
	
	private boolean canDelete(FormDataAccessRoles formDataAccess, FormData formData, ReportPeriod formDataReportPeriod) {
        return formData.getState() == WorkflowState.CREATED && canEdit(formDataAccess, formData, formDataReportPeriod);
    }

	@Override
	public List<WorkflowMove> getAvailableMoves(TAUserInfo userInfo, long formDataId) {
		FormData formData = formDataDao.getWithoutRows(formDataId);
		FormDataAccessRoles formDataAccess = getFormDataUserAccess(userInfo.getUser(), formData.getDepartmentId(),
				formData.getFormType().getId(), formData.getKind());
		List<WorkflowMove> result = getAvailableMoves(formDataAccess,  formData, reportPeriodDao.get(formData.getReportPeriodId()));
		if (logger.isDebugEnabled()) {
			logger.debug(LOG_EVENT_AVAILABLE_MOVES + ": " + result.toString());
		}
		return result;
	}

	private List<WorkflowMove> getAvailableMoves(FormDataAccessRoles formDataAccess, FormData formData,
			ReportPeriod formDataReportPeriod) {
		List<WorkflowMove> result = new ArrayList<WorkflowMove>();
		// Для того, чтобы иметь возможность изменить статус, у пользователя должны быть права
		// на чтение соответствующей карточки данных и отчетный период должен быть активным
		if (!canRead(formDataAccess, formData) || !formDataReportPeriod.isActive()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Report period is closed");
			}
			return result;
		}
		WorkflowState state = formData.getState();
		if (formDataReportPeriod.isBalancePeriod()) {
			// Если отчетный период для ввода остатков, то сокращаем жц до Создана - Принята
			switch (state) {
				case CREATED:
					result.add(WorkflowMove.CREATED_TO_ACCEPTED);
					break;
				case ACCEPTED:
					result.add(WorkflowMove.APPROVED_TO_CREATED);
					break;
				default:
					logger.warn(String.format(FORMDATA_KIND_STATE_ERROR, LOG_EVENT_AVAILABLE_MOVES, formData.getKind().getName(), state.getName()));
			}
		} else if((formData.getKind() == FormDataKind.ADDITIONAL || formData.getKind() == FormDataKind.PRIMARY) &&
				formDataAccess.isFormDataHasDestinations()){
			/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и передаваемых на вышестоящий уровень.

			 Виды форм:
				 Первичная налоговая форма;
				 Выходная налоговая форма*/
			switch (state){
				case CREATED:
					if (formDataAccess.isOperatorOfCurrentLevel() || formDataAccess.isControllerOfCurrentLevel() ||
							formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP()) {
						result.add(WorkflowMove.CREATED_TO_PREPARED);
					}
					break;
				case PREPARED:
					if (formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
							formDataAccess.isControllerOfUNP()) {
						result.add(WorkflowMove.PREPARED_TO_CREATED);
						result.add(WorkflowMove.PREPARED_TO_APPROVED);
					}
					break;
				case APPROVED:
					if (formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP()) {
						result.add(WorkflowMove.APPROVED_TO_PREPARED);
						result.add(WorkflowMove.APPROVED_TO_ACCEPTED);
					}
					break;
				case ACCEPTED:
					if(formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP()){
						result.add(WorkflowMove.ACCEPTED_TO_APPROVED);
					}
					break;
				default:
					logger.warn(String.format(FORMDATA_KIND_STATE_ERROR, LOG_EVENT_AVAILABLE_MOVES, formData.getKind().getName(), state.getName()));
			}
		} else if((formData.getKind() == FormDataKind.ADDITIONAL || formData.getKind() == FormDataKind.PRIMARY ||
				formData.getKind() == FormDataKind.UNP) && !formDataAccess.isFormDataHasDestinations()){
			/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и НЕ передаваемых на вышестоящий уровень.

			 Виды форм:
				 Первичная налоговая форма;
				 Выходная налоговая форма;
				 Форма УНП.*/
			switch (state){
				case CREATED:
					if (formDataAccess.isOperatorOfCurrentLevel() || formDataAccess.isControllerOfCurrentLevel() ||
							formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP()) {
						result.add(WorkflowMove.CREATED_TO_PREPARED);
					}
					break;
				case PREPARED:
					if (formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel()
							|| formDataAccess.isControllerOfUNP()) {
						result.add(WorkflowMove.PREPARED_TO_CREATED);
						result.add(WorkflowMove.PREPARED_TO_ACCEPTED);
					}
					break;
				case ACCEPTED:
					if(formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP()){
						result.add(WorkflowMove.ACCEPTED_TO_PREPARED);
					}
					break;
				default:
					logger.warn(String.format(FORMDATA_KIND_STATE_ERROR, LOG_EVENT_AVAILABLE_MOVES, formData.getKind().getName(), state.getName()));
			}
		} else if ((formData.getKind() == FormDataKind.SUMMARY || formData.getKind() == FormDataKind.CONSOLIDATED) &&
				!formDataAccess.isFormDataHasDestinations()){
			/*Жизненный цикл налоговых форм, формируемых автоматически
			 и НЕ передаваемых на вышестоящий уровень

			 Виды форм:
             	Консолидированная налоговая форма;
				Сводная налоговая форма подразделения;
				Сводная налоговая форма Банка.*/
			switch (state){
				case CREATED:
					if(formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
							formDataAccess.isControllerOfUNP()){
						result.add(WorkflowMove.CREATED_TO_ACCEPTED);
					}
					break;
				case ACCEPTED:
					if(formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
							formDataAccess.isControllerOfUNP()){
						result.add(WorkflowMove.ACCEPTED_TO_CREATED);
					}
					break;
				default:
					logger.warn(String.format(FORMDATA_KIND_STATE_ERROR, LOG_EVENT_AVAILABLE_MOVES, formData.getKind().getName(), state.getName()));
			}
		} else if ((formData.getKind() == FormDataKind.SUMMARY || formData.getKind() == FormDataKind.CONSOLIDATED) &&
				formDataAccess.isFormDataHasDestinations()){
			/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень

             Виды форм:
             	Консолидированная налоговая форма;
				Сводная налоговая форма подразделения.*/
			switch (state){
				case CREATED:
					if(formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel()
							|| formDataAccess.isControllerOfUNP()){
						result.add(WorkflowMove.CREATED_TO_APPROVED);
					}
					break;
				case APPROVED:
					if(formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP()){
						result.add(WorkflowMove.APPROVED_TO_ACCEPTED);
						result.add(WorkflowMove.APPROVED_TO_CREATED);
					}
					break;
				case ACCEPTED:
					if(formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP()){
						result.add(WorkflowMove.ACCEPTED_TO_APPROVED);
					}
					break;
				default:
					logger.warn(String.format(FORMDATA_KIND_STATE_ERROR, LOG_EVENT_AVAILABLE_MOVES, formData.getKind().getName(), state.getName()));
			}
		} else
			logger.warn(String.format(FORMDATA_KIND_STATE_ERROR, LOG_EVENT_AVAILABLE_MOVES, formData.getKind().getName(), state.getName()));
		return result;
	}

	@Override
	public FormDataAccessParams getFormDataAccessParams(TAUserInfo userInfo, long formDataId) {
		FormData formData = formDataDao.getWithoutRows(formDataId);
		ReportPeriod reportPeriod = reportPeriodDao.get(formData.getReportPeriodId());

		FormDataAccessRoles formDataAccess = getFormDataUserAccess(userInfo.getUser(), formData.getDepartmentId(),
			formData.getFormType().getId(), formData.getKind());

		FormDataAccessParams result = new FormDataAccessParams();
		result.setCanRead(canRead(formDataAccess, formData));
		result.setCanEdit(canEdit(formDataAccess, formData, reportPeriod));
		result.setCanDelete(canDelete(formDataAccess, formData, reportPeriod));
		result.setAvailableWorkflowMoves(getAvailableMoves(formDataAccess, formData, reportPeriod));
		if (logger.isDebugEnabled()) {
			logger.debug("FormDataAccessParams: " + result.toString());
		}
		return result;
	}

	/**
	 * Возвращает описание прав доступа текущего пользователя по отношению в НФ
	 * @param user текущий пользователь
	 * @param formDataDepartmentId код подразделения НФ
	 * @param formDataTypeId вид НФ
	 * @param formDataKind тип НФ
	 * @return описание прав доступа
	 */
	private FormDataAccessRoles getFormDataUserAccess(TAUser user, int formDataDepartmentId, int formDataTypeId, FormDataKind formDataKind) {
		Department userDepartment = departmentService.getDepartment(user.getDepartmentId());
		FormDataAccessRoles formDataAccessRoles = new FormDataAccessRoles();

		// оператор текущего уровня
		formDataAccessRoles.setOperatorOfCurrentLevel(user.hasRole(TARole.ROLE_OPERATOR) && user.getDepartmentId() == formDataDepartmentId);

		// контроллер текущего уровня: имеет роль контроллера и текущая форма относится к его подразделению
		formDataAccessRoles.setControllerOfCurrentLevel(
			user.hasRole(TARole.ROLE_CONTROL) && user.getDepartmentId() == formDataDepartmentId);

		// контроллер вышестоящего уровня: имеет роль контроллера и текущая форма является источником для
		// одной или нескольких форм текущего подразделения
		List<DepartmentFormType> formDestinations =
				departmentFormTypeDao.getFormDestinations(formDataDepartmentId, formDataTypeId, formDataKind);
		boolean isCurrentDepartmentIsDestination = false;
		// TODO: если будут проблемы с производительностью, то можно вместо этого цикла сделать отдельный Dao-метод со
		// специализированным запросом
		for (DepartmentFormType destination : formDestinations) {
			if (userDepartment.getId() == destination.getDepartmentId()) {
				isCurrentDepartmentIsDestination = true;
				break;
			}
		}
		// TODO: обсуждался случай, когда форма в чужом подразделении является источником для
		// другой формы в этом же подразделении, а уже эта вторая форма
		// является источником для одной из форм подразделения, к которому относится контролёр
		formDataAccessRoles.setControllerOfUpLevel(user.hasRole(TARole.ROLE_CONTROL) && isCurrentDepartmentIsDestination);

		// контроллер УНП: имеет роль контроллера УНП
		formDataAccessRoles.setControllerOfUNP(user.hasRole(TARole.ROLE_CONTROL_UNP));

		// передается ли форма на вышестоящий уровень:
		// вначале проверяем связи НФ->НФ
		boolean sendToNextLevel = false;
		for (DepartmentFormType destination : formDestinations) {
			if (formDataDepartmentId != destination.getDepartmentId()) {
				sendToNextLevel = true;
				break;
			}
		}
		if (!sendToNextLevel) {
			// затем проверяем связи НФ->Декларация
			List<DepartmentDeclarationType> declarationDestinations =
					departmentFormTypeDao.getDeclarationDestinations(formDataDepartmentId, formDataTypeId, formDataKind);
			for (DepartmentDeclarationType destination : declarationDestinations) {
				if (formDataDepartmentId != destination.getDepartmentId()) {
					sendToNextLevel = true;
					break;
				}
			}
		}
		formDataAccessRoles.setFormDataHasDestinations(sendToNextLevel);

		// автоматически создаваемая форма:
		// считается, что форма создается автоматически, если есть формы-источники для данной формы
		List<DepartmentFormType> formSources =
				departmentFormTypeDao.getFormSources(formDataDepartmentId, formDataTypeId, formDataKind);
		formDataAccessRoles.setAutoCreatingForm(formSources.size() > 0);

		return formDataAccessRoles;
	}

	/**
	 * Описывает роль пользователя по отношению к НФ: <ul>
	 *     <li>оператор</li>
	 *     <li>контроллер ТУ</li>
	 *     <li>контроллер ВСУ</li>
	 *     <li>контроллер УНП</li>
	 * </ul>
	 * Для НФ указывает передается ли она на вышестоящий уровень. <br />
	 * Используется для проверки матрицы ролей и прав доступа.
	 *
	 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
	 * @since 12.02.13 16:00
	 * @see <a href="http://conf.aplana.com/pages/viewpage.action?pageId=8784305">Роли и права доступа</a>
	 */
	private class FormDataAccessRoles {

		private boolean operatorOfCurrentLevel;
		private boolean controllerOfCurrentLevel;
		private boolean controllerOfUpLevel;
		private boolean controllerOfUNP;
		/** передается ли форма на вышестоящий уровень? */
		private boolean formDataHasDestinations;
		/** автоматически создаваемая форма */
		private boolean autoCreatingForm;

		public boolean isOperatorOfCurrentLevel() {
			return operatorOfCurrentLevel;
		}

		public void setOperatorOfCurrentLevel(boolean operatorOfCurrentLevel) {
			this.operatorOfCurrentLevel = operatorOfCurrentLevel;
		}

		public boolean isFormDataHasDestinations() {
			return formDataHasDestinations;
		}

		public void setFormDataHasDestinations(boolean formDataHasDestinations) {
			this.formDataHasDestinations = formDataHasDestinations;
		}

		public boolean isControllerOfCurrentLevel() {
			return controllerOfCurrentLevel;
		}

		public void setControllerOfCurrentLevel(boolean controllerOfCurrentLevel) {
			this.controllerOfCurrentLevel = controllerOfCurrentLevel;
		}

		public boolean isControllerOfUpLevel() {
			return controllerOfUpLevel;
		}

		public void setControllerOfUpLevel(boolean controllerOfUpLevel) {
			this.controllerOfUpLevel = controllerOfUpLevel;
		}

		public boolean isControllerOfUNP() {
			return controllerOfUNP;
		}

		public void setControllerOfUNP(boolean controllerOfUNP) {
			this.controllerOfUNP = controllerOfUNP;
		}

		@SuppressWarnings("unused")
		public boolean isAutoCreatingForm() {
			return autoCreatingForm;
		}

		public void setAutoCreatingForm(boolean autoCreatingForm) {
			this.autoCreatingForm = autoCreatingForm;
		}

		@Override
		public String toString() {
			return "FormDataAccessRoles{" +
					"operatorOfCurrentLevel=" + operatorOfCurrentLevel +
					", controllerOfCurrentLevel=" + controllerOfCurrentLevel +
					", controllerOfUpLevel=" + controllerOfUpLevel +
					", controllerOfUNP=" + controllerOfUNP +
					", formDataHasDestinations=" + formDataHasDestinations +
					", autoCreatingForm=" + autoCreatingForm +
					'}';
		}
	}
}