package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataWorkflowDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.security.TAUserDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.log.impl.RowScriptMessageDecorator;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.service.exception.ServiceException;

/**
 * Сервис для работы с {@link FormData данными по налоговым формам}.
 *
 * @author Vitalii Samolovskikh
 */
@Service
public class FormDataServiceImpl implements FormDataService {

	@Autowired
	private FormDataDao formDataDao;
	@Autowired
	private TAUserDao userDao;
	@Autowired
	private FormTemplateDao formTemplateDao;
	@Autowired
	private FormDataWorkflowDao formDataWorkflowDao;
	@Autowired
	private ReportPeriodDao reportPeriodDao;
	@Autowired
	private FormDataAccessService formDataAccessService;
	@Autowired
	private FormDataScriptingService formDataScriptingService;

	/**
	 * Создать налоговую форму заданного типа
	 * При создании формы выполняются следующие действия:
	 * 1) создаётся пустой объект
	 * 2) если в объявлении формы заданы строки по-умолчанию (начальные данные), то эти строки копируются в созданную форму
	 * 3) если в объявлении формы задан скрипт создания, то этот скрипт выполняется над создаваемой формой
	 *
	 * @param logger         логгер-объект для фиксации диагностических сообщений
	 * @param userId         идентификатор пользователя, запросившего операцию
	 * @param formTemplateId идентификатор шаблона формы, по которой создавать объект
	 * @param departmentId   идентификатор {@link com.aplana.sbrf.taxaccounting.model.Department подразделения}, к которому относится форма
	 * @param kind           {@link com.aplana.sbrf.taxaccounting.model.FormDataKind тип налоговой формы} (первичная, сводная, и т.д.), это поле необходимо, так как некоторые виды
	 *                       налоговых форм в одном и том же подразделении могут существовать в нескольких вариантах (например один и тот же РНУ  на уровне ТБ
	 *                       - в виде первичной и консолидированной)
	 * @return созданный и проинициализированный объект данных.
	 * @throws com.aplana.sbrf.taxaccounting.service.exception.AccessDeniedException
	 *          если у пользователя нет прав создавать налоговую форму с такими параметрами
	 * @throws com.aplana.sbrf.taxaccounting.service.exception.ServiceException
	 *          если при создании формы произошли ошибки, вызванные несоблюдением каких-то бизнес-требований, например отсутствием
	 *          обязательных параметров
	 */
	@Override
	public FormData createFormData(Logger logger, int userId, int formTemplateId, int departmentId, FormDataKind kind) {
		if (formDataAccessService.canCreate(userId, formTemplateId, kind, departmentId)) {
			return createFormDataWithoutCheck(logger, userDao.getUser(userId), formTemplateId, departmentId, kind);
		} else {
			throw new AccessDeniedException("Недостаточно прав для создания налоговой формы с указанными параметрами");
		}
	}

	@Override
	public FormData createFormDataWithoutCheck(Logger logger, TAUser user, int formTemplateId, int departmentId, FormDataKind kind) {
		FormTemplate form = formTemplateDao.get(formTemplateId);
		FormData result = new FormData(form);

		result.setState(WorkflowState.CREATED);
		result.setDepartmentId(departmentId);
		// TODO: сюда хорошо бы добавить проверку, что данный тип формы соответствует
		// виду формы (FormType) и уровню подразделения (например сводные нельзя делать на уровне ниже ТБ)
		result.setKind(kind);
		result.setReportPeriodId(reportPeriodDao.getCurrentPeriod(form.getType().getTaxType()).getId());

		for (DataRow predefinedRow : form.getRows()) {
			DataRow dataRow = result.appendDataRow(predefinedRow.getAlias());
			for (Map.Entry<String, Object> entry : predefinedRow.entrySet()) {
				dataRow.put(entry.getKey(), entry.getValue());
			}
		}

		// Execute scripts for the form event CREATE
		formDataScriptingService.executeScripts(user, result, FormDataEvent.CREATE, logger);
		return result;
	}

	/**
	 * Выполнить расчёты по налоговой форме
	 *
	 * @param logger   логгер-объект для фиксации диагностических сообщений
	 * @param userId   идентификатор пользователя, запросившего операцию
	 * @param formData объект с данными по налоговой форме
	 */
	@Override
	public void doCalc(Logger logger, int userId, FormData formData) {
		boolean canDo;
		if (formData.getId() == null) {
			canDo = formDataAccessService.canCreate(userId, formData.getFormTemplateId(), formData.getKind(), formData.getDepartmentId());
		} else {
			canDo = formDataAccessService.canEdit(userId, formData.getId());
		}
		
		if (canDo) {
			TAUser user = userDao.getUser(userId);
			formDataScriptingService.executeScripts(user, formData, FormDataEvent.CALCULATE, logger);
			checkMandatoryColumns(formData, formTemplateDao.get(formData.getFormTemplateId()), logger);
		} else {
			throw new AccessDeniedException("Недостаточно прав для выполенения расчёта по налоговой форме");
		}
	}

	/**
	 * Сохранить данные по налоговой форме
	 *
	 * @param userId   идентификатор пользователя, выполняющего операцию
	 * @param formData объект с данными налоговой формы
	 * @return идентификатор сохранённой записи
	 * @throws com.aplana.sbrf.taxaccounting.service.exception.AccessDeniedException
	 *          если у пользователя нет прав редактировать налоговую форму с такими параметрами
	 */
	@Override
	@Transactional
	public long saveFormData(int userId, FormData formData) {
		boolean canDo;
		if (formData.getId() == null) {
			canDo = formDataAccessService.canCreate(userId, formData.getFormTemplateId(), formData.getKind(), formData.getDepartmentId());
		} else {
			canDo = formDataAccessService.canEdit(userId, formData.getId());
		}
		
		if (canDo) {
			return formDataDao.save(formData);
		} else {
			throw new AccessDeniedException("Недостаточно прав для изменения налоговой формы");
		}
	}

	/**
	 * Получить данные по налоговой форме
	 *
	 * @param userId     идентификатор пользователя, выполняющего операцию
	 * @param formDataId идентификатор записи, которую необходимо считать
	 * @return объект с данными по налоговой форме
	 * @throws com.aplana.sbrf.taxaccounting.service.exception.AccessDeniedException
	 *          если у пользователя нет прав просматривать налоговую форму с такими параметрами
	 */
	@Override
	@Transactional
	public FormData getFormData(int userId, long formDataId) {
		if (formDataAccessService.canRead(userId, formDataId)) {
			return formDataDao.get(formDataId);
		} else {
			throw new AccessDeniedException("Недостаточно прав на просмотр данных налоговой формы",
				userId, formDataId
			);
		}
	}

	/**
	 * Удалить данные по налоговой форме
	 *
	 * @param userId     идентификатор пользователя, выполняющего операцию
	 * @param formDataId идентификатор записи, котрую нужно удалить
	 * @throws com.aplana.sbrf.taxaccounting.service.exception.AccessDeniedException
	 *          если у пользователя недостаточно прав для удаления записи
	 */
	@Override
	@Transactional
	public void deleteFormData(int userId, long formDataId) {
		if (formDataAccessService.canDelete(userId, formDataId)) {
			formDataDao.delete(formDataId);
		} else {
			throw new AccessDeniedException("Недостаточно прав для удаления налоговой формы");
		}
	}

	@Override
	public void checkMandatoryColumns(FormData formData, FormTemplate formTemplate, Logger logger) {
		List<Column> columns = formTemplate.getColumns();
		RowScriptMessageDecorator messageDecorator = new RowScriptMessageDecorator();
		messageDecorator.setScriptName("Проверка обязательных полей");
		logger.setMessageDecorator(messageDecorator);
		int rowIndex = 0;
		for (DataRow row : formData.getDataRows()) {
			++rowIndex; // Для пользователя нумерация строк должна начинаться с 1
			messageDecorator.setRowIndex(rowIndex);
			List<String> columnNames = new ArrayList<String>();
			for (Column col : columns) {
				if (col.isMandatory() && row.get(col.getAlias()) == null) {
					columnNames.add(col.getName());
				}
			}
			if (!columnNames.isEmpty()) {
				logger.error("Не заполнены столбцы %s", columnNames.toString());
			}
		}
		logger.setMessageDecorator(null);
	}

	/**
	 * Перемещает форму из одного состояния в другое.
	 *
	 * @param formDataId   идентификатор налоговой формы
	 * @param userId       идентификатор текщуего пользователя
	 * @param workflowMove переход
	 */
	@Override
	public boolean doMove(long formDataId, int userId, WorkflowMove workflowMove, Logger logger) {
		List<WorkflowMove> availableMoves = formDataAccessService.getAvailableMoves(userId, formDataId);
		if (!availableMoves.contains(workflowMove)) {
			throw new ServiceException("Переход \"" + workflowMove + "\" из текущего состояния невозможен, или пользователя с id = " + userId + " не хватает полномочий для его осуществления");
		}

		FormData formData = formDataDao.get(formDataId);
		formDataScriptingService.executeScripts(userDao.getUser(userId), formData, workflowMove.getEvent(), logger);
		checkMandatoryColumns(formData, formTemplateDao.get(formData.getFormTemplateId()), logger);
		if (!logger.containsLevel(LogLevel.ERROR)) {
			formDataWorkflowDao.changeFormDataState(formDataId, workflowMove.getToState());

			if(workflowMove.getAfterEvent()!=null){
				formDataScriptingService.executeScripts(userDao.getUser(userId), formData, workflowMove.getAfterEvent(), logger);
			}

			return true;
		} else {
			return false;
		}
	}
}
