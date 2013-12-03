package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FormDataAccessServiceImpl implements FormDataAccessService {

	public static final String LOG_EVENT_AVAILABLE_MOVES = "LOG_EVENT_AVAILABLE_MOVES";
	public static final String LOG_EVENT_READ = "READ";
	public static final String LOG_EVENT_EDIT = "EDIT";
	public static final String LOG_EVENT_CREATE = "CREATE";

    public static final String LOG_EVENT_READ_RU = "чтение";
    public static final String LOG_EVENT_EDIT_RU = "редактирование";

	private static final Log logger = LogFactory.getLog(FormDataAccessServiceImpl.class);

	private static final String FORMDATA_KIND_STATE_ERROR_LOG = "Event type: \"%s\". Unsuppotable case for formData with \"%s\" kind and \"%s\" state!";
	private static final String REPORT_PERIOD_IS_CLOSED_LOG = "Report period (%d) is closed!";
	private static final String REPORT_PERIOD_IS_CLOSED = "Невозможно создать форму, неактивный налоговый период!";
	private static final String INCORRECT_DEPARTMENT_FORM_TYPE_LOG = "Form type(%d) and form kind(%d) is not applicated for department(%d)";
	private static final String INCORRECT_DEPARTMENT_FORM_TYPE = "Форма не назначена подразделению!";
	private static final String CREATE_FORM_DATA_ERROR_ONLY_CONTROL = "Только контролер имеет право создавать формы с перидом ввода остатков.";
	private static final String CREATE_FORM_DATA_ERROR_ACCESS_DENIED = "Недостаточно прав для создания налоговой формы с указанными параметрами";
    private static final String FORM_DATA_ERROR_ACCESS_DENIED = "Недостаточно прав на %s формы с типом \"%s\" в статусе \"%s\" и %s передаваемой на вышестоящий уровень";

    // id формы "Согласование организаций"
    private static final int ORGANIZATION_FORM_TYPE = 410;

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
	@Autowired
	private PeriodService reportPeriodService;
	@Autowired
	private SourceService sourceService;
	@Autowired
	private FormTypeDao formTypeDao;

	@Override
	public void canRead(TAUserInfo userInfo, long formDataId) {
		FormData formData = formDataDao.getWithoutRows(formDataId);
		FormDataAccessRoles formDataAccess = getFormDataUserAccess(userInfo.getUser(), formData.getDepartmentId(),
				formData.getFormType().getId(), formData.getKind());
		canRead(formDataAccess, formData);
	}

	private void canRead(FormDataAccessRoles formDataAccess, FormData formData) {
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
            return;

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
            return;

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
			if (!(formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
					formDataAccess.isControllerOfUNP())){
                throw new AccessDeniedException(
                        String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_READ_RU, formData.getKind().getName(), formData.getState().getName(), "НЕ") +
                                " Чтение формы возможно только пользователями контролерами текущего или вышестоящего уровня.");
            }
            return;
		} else if (formData.getKind() == FormDataKind.SUMMARY || formData.getKind() == FormDataKind.CONSOLIDATED){
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
            if (!(formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
                    formDataAccess.isControllerOfUNP())){
                throw new AccessDeniedException(
                        String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_READ_RU, formData.getKind().getName(), formData.getState().getName(), "") +
                                " Чтение формы возможно только пользователями контролерами текущего или вышестоящего уровня.");
            }
			return;
		}
		logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_READ, formData.getKind().getName(), formData.getState().getName()));
        throw new AccessDeniedException(String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_READ_RU, formData.getKind().getName(), formData.getState().getName(),formDataAccess.isFormDataHasDestinations()?"":"НЕ"));
	}

	@Override
	public void canEdit(TAUserInfo userInfo, long formDataId) {
		FormData formData = formDataDao.getWithoutRows(formDataId);
		FormDataAccessRoles formDataAccess = getFormDataUserAccess(userInfo.getUser(), formData.getDepartmentId(),
				formData.getFormType().getId(), formData.getKind());
		canEdit(formDataAccess, formData, reportPeriodDao.get(formData.getReportPeriodId()));
	}

	private void canEdit(FormDataAccessRoles formDataAccess, FormData formData, ReportPeriod formDataReportPeriod) {
		WorkflowState state = formData.getState();
		if (!reportPeriodService.isActivePeriod(formDataReportPeriod.getId(), formData.getDepartmentId())){
			throw new AccessDeniedException( String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU, formData.getKind().getName(), formData.getState().getName(), formDataAccess.isFormDataHasDestinations()?"":"НЕ") +
                    " Период неактивен.");
		}
        if(formData.getFormType().getId() == ORGANIZATION_FORM_TYPE){
			/* Жизненный цикл формы "Согласование организации"
             ------------------------------------------------------------------
             |              |                Пользователь                     |
             |  Состояние   |-------------------------------------------------|
             |              |Оператор|Контролер ТУ|Контролер ВСУ|Контролер УНП|
             ------------------------------------------------------------------
             | Создана      |   +    |     +      |      +      |      +      |
             ---------------|-------------------------------------------------|
             | Подготовлена |   -    |     +      |      +      |      +      |
             ---------------|-------------------------------------------------|
             | Утверждена   |   -    |     -      |      -      |      +      |
             ---------------|-------------------------------------------------|
             | Принята      |   -    |     -      |      -      |      -      |
             ---------------|--------------------------------------------------
             *Контролер ТУ - Контролер текущего уровня
             *Контролер ВСУ - Контролер вышестоящего уровня
			 */
            switch (formData.getState()){
                case CREATED:
                    return;
                case PREPARED:
                    if (!(formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
                            formDataAccess.isControllerOfUNP())){
                        throw new AccessDeniedException(
                                String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU, formData.getKind().getName(), formData.getState().getName(), formDataAccess.isFormDataHasDestinations()?"":"НЕ") +
                                        " Чтение формы возможно только пользователями контролерами текущего или вышестоящего уровня.");
                    }
                    return;
                case APPROVED:
                    if (!formDataAccess.isControllerOfUNP()) {
                        throw new AccessDeniedException(
                                String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU, formData.getKind().getName(), formData.getState().getName(), formDataAccess.isFormDataHasDestinations()?"":"НЕ") +
                                        " Чтение формы возможно только пользователем \"Контролер УНП.\"");
                    }
                    return;
                case ACCEPTED:
                    throw new AccessDeniedException("Нельзя редактировать НФ в состоянии \"Принята\"");
                    //Нельзя редактировать в состоянии "Принята"
            }
            logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_EDIT, formData.getKind().getName(), state.getName()));
        } else if (reportPeriodService.isBalancePeriod(formDataReportPeriod.getId(), formData.getDepartmentId())) {
			/* В отчетных периодах для ввода остатков редактирование возможно только контролерами для
			 НФ в статусе "Создана" */
			switch (state){
				case CREATED:
                    if (!(formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
                            formDataAccess.isControllerOfUNP())){
                        throw new AccessDeniedException(
                                String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU, formData.getKind().getName(), formData.getState().getName(), formDataAccess.isFormDataHasDestinations()?"":"НЕ") +
                                        " Чтение формы возможно только пользователями контролерами текущего или вышестоящего уровня.");
                    }
					return;
				case ACCEPTED:
                    throw new AccessDeniedException("Нельзя редактировать НФ в состоянии \"Принята\"");
					//Нельзя редактировать НФ в состоянии "Принята"
			}
			logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_EDIT, formData.getKind().getName(), state.getName()));
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
             | Утверждена   |   -    |     -      |      +      |      +      |
             ---------------|-------------------------------------------------|
             | Принята      |   -    |     -      |      -      |      -      |
             ---------------|--------------------------------------------------
             *Контролер ТУ - Контролер текущего уровня
             *Контролер ВСУ - Контролер вышестоящего уровня
			 */
			switch (formData.getState()){
				case CREATED:
					return;
				case PREPARED:
                    if (!(formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
                            formDataAccess.isControllerOfUNP())){
                        throw new AccessDeniedException(
                                String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU, formData.getKind().getName(), formData.getState().getName(), "") +
                                        " Чтение формы возможно только пользователями контролерами текущего или вышестоящего уровня.");
                    }
					return;
                case APPROVED:
                    if (!(formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP())){
                        throw new AccessDeniedException(
                                String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU, formData.getKind().getName(), formData.getState().getName(), "") +
                                        " Чтение формы возможно только пользователями вышестоящего уровня или котролером УНП.");
                    }
                    return;
				case ACCEPTED:
                    throw new AccessDeniedException("Нельзя редактировать НФ в состоянии \"Принята\"");
					//Нельзя редактировать НФ в состоянии "Принята"
			}
			logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_EDIT, formData.getKind().getName(), state.getName()));
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
					return;
				case PREPARED:
                    if (!(formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
                            formDataAccess.isControllerOfUNP())){
                        throw new AccessDeniedException(
                                String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU, formData.getKind().getName(), formData.getState().getName(), "НЕ") +
                                        " Чтение формы возможно только пользователями контролерами текущего или вышестоящего уровня.");
                    }
					return;
				case ACCEPTED:
                    throw new AccessDeniedException("Нельзя редактировать НФ в состоянии \"Принята\"");
					//Нельзя редактировать НФ в состоянии "Принята"
			}
			logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_EDIT, formData.getKind().getName(), state.getName()));
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
                    if (!(formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
                            formDataAccess.isControllerOfUNP())){
                        throw new AccessDeniedException(
                                String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU, formData.getKind().getName(), formData.getState().getName(), formDataAccess.isFormDataHasDestinations()?"":"НЕ") +
                                        " Чтение формы возможно только пользователями контролерами текущего или вышестоящего уровня.");
                    }
					return; //TODO (Marat Fayzullin 20.03.2013) временно до появления первичных форм
				case ACCEPTED:
                    throw new AccessDeniedException("Нельзя редактировать НФ в состоянии \"Принята\"");//Нельзя редактировать НФ в состоянии "Принята"
			}
			logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_EDIT, formData.getKind().getName(), state.getName()));
		} else if (formData.getKind() == FormDataKind.SUMMARY || formData.getKind() == FormDataKind.CONSOLIDATED){
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
                    if (!(formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
                            formDataAccess.isControllerOfUNP())){
                        throw new AccessDeniedException(
                                String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU, formData.getKind().getName(), formData.getState().getName(), "") +
                                        " Чтение формы возможно только пользователями контролерами текущего или вышестоящего уровня.");
                    }
					return;
				case APPROVED:
                    throw new AccessDeniedException(
                            String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU, formData.getKind().getName(), formData.getState().getName(), "")
                    );
				case ACCEPTED:
                    //Нельзя редактировать НФ в состоянии "Принята"
                    throw new AccessDeniedException("Нельзя редактировать НФ в состоянии \"Принята\"");
			}
			logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_EDIT, formData.getKind().getName(), state.getName()));
		}
		logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_EDIT, formData.getKind().getName(), state.getName()));
		throw new AccessDeniedException(String.format(FORM_DATA_ERROR_ACCESS_DENIED, LOG_EVENT_EDIT_RU, formData.getKind().getName(), formData.getState().getName(), formDataAccess.isFormDataHasDestinations()?"":"НЕ"));
		/*return false;*/
	}

	@Override
	public void canCreate(TAUserInfo userInfo, int formTemplateId, FormDataKind kind, int departmentId, int reportPeriodId) {
		Department formDataDepartment = departmentService.getDepartment(departmentId);
		FormTemplate formTemplate = formTemplateDao.get(formTemplateId);
		int formTypeId = formTemplate.getType().getId();
		FormDataAccessRoles formDataAccess = getFormDataUserAccess(userInfo.getUser(), formDataDepartment.getId(), formTypeId, kind);
		canCreate(formDataAccess, formTypeId, kind, formDataDepartment, reportPeriodDao.get(reportPeriodId));
	}

	private void canCreate(FormDataAccessRoles formDataAccess, int formTypeId, FormDataKind kind, Department formDataDepartment, ReportPeriod reportPeriod) {
		if(!reportPeriodService.isActivePeriod(reportPeriod.getId(), formDataDepartment.getId())){
			//Нельзя создавать НФ для неактивного налогового периода
			logger.warn(String.format(REPORT_PERIOD_IS_CLOSED_LOG, reportPeriod.getId()));
			throw new ServiceException(REPORT_PERIOD_IS_CLOSED);
		}

		FormType formType = formTypeDao.get(formTypeId);

		// Проверяем, что в подразделении вообще можно работать с формами такого вида и типа
		boolean found = false;
		for (DepartmentFormType dft: sourceService.getDFTByDepartment(formDataDepartment.getId(), formType.getTaxType())) {
			if (dft.getFormTypeId() == formTypeId && dft.getKind() == kind) {
				found = true;
				break;
			}
		}
		if (!found) {
			logger.warn(String.format(INCORRECT_DEPARTMENT_FORM_TYPE_LOG, formTypeId, kind.getId(), formDataDepartment.getId()));
			throw new AccessDeniedException(INCORRECT_DEPARTMENT_FORM_TYPE);
		}

		if (reportPeriodService.isBalancePeriod(reportPeriod.getId(), formDataDepartment.getId())){
			// период ввода остатков
            if(!(formDataAccess.isControllerOfCurrentLevel() ||
                    formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP())) {
                throw new AccessDeniedException(CREATE_FORM_DATA_ERROR_ONLY_CONTROL);
            }
		} else if((kind == FormDataKind.ADDITIONAL || kind == FormDataKind.PRIMARY) &&
				formDataAccess.isFormDataHasDestinations()){
			 /* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и передаваемых на вышестоящий уровень.

			 Виды форм:
				 Первичная налоговая форма;
				 Выходная налоговая форма; */
            if(!(formDataAccess.isOperatorOfCurrentLevel() || formDataAccess.isControllerOfCurrentLevel() ||
                    formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP())) {
                throw new AccessDeniedException(CREATE_FORM_DATA_ERROR_ACCESS_DENIED);
            }
		}else if((kind == FormDataKind.ADDITIONAL || kind == FormDataKind.PRIMARY ||
				kind == FormDataKind.UNP) && !formDataAccess.isFormDataHasDestinations()){
			 /* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и НЕ передаваемых на вышестоящий уровень.

			 Виды форм:
				 Первичная налоговая форма;
				 Выходная налоговая форма;
				 Форма УНП.*/
            if(!(formDataAccess.isOperatorOfCurrentLevel() || formDataAccess.isControllerOfCurrentLevel() ||
				formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP())){
                throw new AccessDeniedException(CREATE_FORM_DATA_ERROR_ACCESS_DENIED);
            }
		} else if ((kind == FormDataKind.SUMMARY || kind == FormDataKind.CONSOLIDATED) && !formDataAccess.isFormDataHasDestinations()){
			/*Жизненный цикл налоговых форм, формируемых автоматически
			 и НЕ передаваемых на вышестоящий уровень

			 Виды форм:
             	Консолидированная налоговая форма;
				Сводная налоговая форма подразделения;
				Сводная налоговая форма Банка.*/
            if(!(formDataAccess.isControllerOfCurrentLevel() ||
					formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP())){
                throw new AccessDeniedException(CREATE_FORM_DATA_ERROR_ACCESS_DENIED);
            }
			//return false; //TODO (Marat Fayzullin 21.03.2013) временно до появления первичных форм
		} else if ((kind == FormDataKind.SUMMARY || kind == FormDataKind.CONSOLIDATED) && formDataAccess.isFormDataHasDestinations()){
			/*Жизненный цикл налоговых форм, формируемых автоматически
			и передаваемых на вышестоящий уровень

             Виды форм:
             	Консолидированная налоговая форма;
				Сводная налоговая форма подразделения.*/
            if(!(formDataAccess.isControllerOfCurrentLevel() ||
					formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP())){
                throw new AccessDeniedException(CREATE_FORM_DATA_ERROR_ACCESS_DENIED);
            }
			//return false; //TODO: (Marat Fayzullin 19.02.2013) расскомментить после реализации первичных форм, так как автоматически создаваемые НФ создает система
		}
		logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_CREATE, kind.getName(), WorkflowState.CREATED.getName()));
	}

	@Override
	public void canDelete(TAUserInfo userInfo, long formDataId) {
		FormData formData = formDataDao.getWithoutRows(formDataId);
		FormDataAccessRoles formDataAccess = getFormDataUserAccess(userInfo.getUser(), formData.getDepartmentId(),
				formData.getFormType().getId(), formData.getKind());
		canDelete(formDataAccess, formData, reportPeriodDao.get(formData.getReportPeriodId()));
	}

	private void canDelete(FormDataAccessRoles formDataAccess, FormData formData, ReportPeriod formDataReportPeriod) {
        canEdit(formDataAccess, formData, formDataReportPeriod);
        if (formData.getState() == WorkflowState.CREATED)
            return;
        throw new AccessDeniedException(String.format(FORM_DATA_ERROR_ACCESS_DENIED, "удаление",formData.getKind().getName(), formData.getState().getName(), formDataAccess.isFormDataHasDestinations()?"":"НЕ") );
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
		boolean activePeriod = reportPeriodService.isActivePeriod(formDataReportPeriod.getId(), formData.getDepartmentId());
		boolean balancePeriod = reportPeriodService.isBalancePeriod(formDataReportPeriod.getId(), formData.getDepartmentId());

		// Для того, чтобы иметь возможность изменить статус, у пользователя должны быть права
		// на чтение соответствующей карточки данных и отчетный период должен быть активным
        canRead(formDataAccess, formData);
		if (!activePeriod) {
			logger.warn(String.format(REPORT_PERIOD_IS_CLOSED_LOG, formDataReportPeriod.getId()));
			return result;
		}
		WorkflowState state = formData.getState();
		if (balancePeriod) {
			// Если отчетный период для ввода остатков, то сокращаем жц до Создана - Принята
			switch (state) {
				case CREATED:
					result.add(WorkflowMove.CREATED_TO_ACCEPTED);
					break;
				case ACCEPTED:
					result.add(WorkflowMove.APPROVED_TO_CREATED);
					break;
				default:
					logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_AVAILABLE_MOVES, formData.getKind().getName(), state.getName()));
			}
		} else if((formData.getKind() == FormDataKind.ADDITIONAL || formData.getKind() == FormDataKind.PRIMARY) &&
				formDataAccess.isFormDataHasDestinations() || formData.getFormType().getId() == ORGANIZATION_FORM_TYPE){
			/* Жизненный цикл налоговых форм, формируемых пользователем с ролью «Оператор»
			 и передаваемых на вышестоящий уровень.

			 Так же для формы "Согласование организаций", у которой нет связей,
			 но её необходимо прогонять через статус "Утверждена"

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
					if(formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP() &&  formData.getFormType().getId() != ORGANIZATION_FORM_TYPE){
						result.add(WorkflowMove.ACCEPTED_TO_APPROVED);
					}
					break;
				default:
					logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_AVAILABLE_MOVES, formData.getKind().getName(), state.getName()));
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
					logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_AVAILABLE_MOVES, formData.getKind().getName(), state.getName()));
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
					logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_AVAILABLE_MOVES, formData.getKind().getName(), state.getName()));
			}
		} else if (formData.getKind() == FormDataKind.SUMMARY || formData.getKind() == FormDataKind.CONSOLIDATED){
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
					if(formDataAccess.isControllerOfCurrentLevel() || formDataAccess.isControllerOfUpLevel() ||
							formDataAccess.isControllerOfUNP()){
						result.add(WorkflowMove.APPROVED_TO_CREATED);
					}
					if(formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP()){
						result.add(WorkflowMove.APPROVED_TO_ACCEPTED);
					}
					break;
				case ACCEPTED:
					if(formDataAccess.isControllerOfUpLevel() || formDataAccess.isControllerOfUNP()){
						result.add(WorkflowMove.ACCEPTED_TO_APPROVED);
					}
					break;
				default:
					logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_AVAILABLE_MOVES, formData.getKind().getName(), state.getName()));
			}
		} else
			logger.warn(String.format(FORMDATA_KIND_STATE_ERROR_LOG, LOG_EVENT_AVAILABLE_MOVES, formData.getKind().getName(), state.getName()));
		return result;
	}

	@Override
	public FormDataAccessParams getFormDataAccessParams(TAUserInfo userInfo, long formDataId) {
		FormData formData = formDataDao.getWithoutRows(formDataId);
		ReportPeriod reportPeriod = reportPeriodDao.get(formData.getReportPeriodId());

		FormDataAccessRoles formDataAccess = getFormDataUserAccess(userInfo.getUser(), formData.getDepartmentId(),
			formData.getFormType().getId(), formData.getKind());

		FormDataAccessParams result = new FormDataAccessParams();
        try {
            canRead(formDataAccess, formData);
            result.setCanRead(true);
        }catch (AccessDeniedException e){
            result.setCanRead(false);
        }
        try {
            canEdit(formDataAccess, formData, reportPeriod);
            result.setCanEdit(true);
        }catch (AccessDeniedException e){
            result.setCanEdit(false);
        }
        try {
            canDelete(formDataAccess, formData, reportPeriod);
            result.setCanDelete(true);
        }catch (AccessDeniedException e){
            result.setCanDelete(false);
        }
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
		formDataAccessRoles.setOperatorOfCurrentLevel(user.hasRole(TARole.ROLE_OPER) && user.getDepartmentId() == formDataDepartmentId);

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