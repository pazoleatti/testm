package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataWorkflowDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.ObjectLockDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;

/**
 * Сервис для работы с {@link FormData данными по налоговым формам}.
 *
 * @author Vitalii Samolovskikh
 */
@Service("unlockFormData")
@Transactional
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
	@Autowired
	private ObjectLockDao lockDao;
	@Autowired
	private LogBusinessService logBusinessService;

	/**
	 * Создать налоговую форму заданного типа При создании формы выполняются
	 * следующие действия: 1) создаётся пустой объект 2) если в объявлении формы
	 * заданы строки по-умолчанию (начальные данные), то эти строки копируются в
	 * созданную форму 3) если в объявлении формы задан скрипт создания, то этот
	 * скрипт выполняется над создаваемой формой
	 *
	 * @param logger
	 *            логгер-объект для фиксации диагностических сообщений
	 * @param userId
	 *            идентификатор пользователя, запросившего операцию
	 * @param formTemplateId
	 *            идентификатор шаблона формы, по которой создавать объект
	 * @param departmentId
	 *            идентификатор
	 *            {@link com.aplana.sbrf.taxaccounting.model.Department
	 *            подразделения}, к которому относится форма
	 * @param kind
	 *            {@link com.aplana.sbrf.taxaccounting.model.FormDataKind тип
	 *            налоговой формы} (первичная, сводная, и т.д.), это поле
	 *            необходимо, так как некоторые виды налоговых форм в одном и
	 *            том же подразделении могут существовать в нескольких вариантах
	 *            (например один и тот же РНУ на уровне ТБ - в виде первичной и
	 *            консолидированной)
	 * @param reportPeriod отчетный период в котором создается форма
	 * @return созданный и проинициализированный объект данных.
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException
	 *             если у пользователя нет прав создавать налоговую форму с
	 *             такими параметрами
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.ServiceException
	 *             если при создании формы произошли ошибки, вызванные
	 *             несоблюдением каких-то бизнес-требований, например
	 *             отсутствием обязательных параметров
	 */
	@Override
	public FormData createFormData(Logger logger, int userId,
			int formTemplateId, int departmentId, FormDataKind kind, ReportPeriod reportPeriod) {
		if (formDataAccessService.canCreate(userId, formTemplateId, kind,
				departmentId, reportPeriod.getId())) {
			return createFormDataWithoutCheck(logger, userDao.getUser(userId),
					formTemplateId, departmentId, kind, reportPeriod.getId());
		} else {
			throw new AccessDeniedException(
					"Недостаточно прав для создания налоговой формы с указанными параметрами");
		}
	}

	@Override
	public FormData createFormDataWithoutCheck(Logger logger, TAUser user,
			int formTemplateId, int departmentId, FormDataKind kind, int reportPeriodId) {
		FormTemplate form = formTemplateDao.get(formTemplateId);
		FormData result = new FormData(form);

		result.setState(WorkflowState.CREATED);
		result.setDepartmentId(departmentId);
		result.setKind(kind);
		result.setReportPeriodId(reportPeriodId);
		result.setCreationDate(new Date());

		for (DataRow<Cell> predefinedRow : form.getRows()) {
			DataRow<Cell> dataRow = result.appendDataRow(predefinedRow.getAlias());
			for (Map.Entry<String, Object> entry : predefinedRow.entrySet()) {
				String columnAlias = entry.getKey();
				dataRow.put(columnAlias, entry.getValue());
				Cell cell = dataRow.getCell(columnAlias);
				Cell predefinedCell = predefinedRow.getCell(columnAlias);
				cell.setColSpan(predefinedCell.getColSpan());
				cell.setRowSpan(predefinedCell.getRowSpan());
				cell.setStyleAlias(predefinedCell.getStyleAlias());
				cell.setEditable(predefinedCell.isEditable());
			}
		}

		// Execute scripts for the form event CREATE
		formDataScriptingService.executeScript(user, result,
				FormDataEvent.CREATE, logger,null);
		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Произошли ошибки в скрипте создания налоговой формы",
					logger.getEntries());
		}

		addLogBusiness(result.getId(), user, FormDataEvent.CREATE, null);

		return result;
	}

	/**
	 * Добавляет строку в форму и выполняет соответствующие скрипты.
	 *
	 * @param logger
	 *            логгер для регистрации ошибок
	 * @param userId
	 *            идентификатор пользователя
	 * @param formData
	 *            данные формы
	 */
	@Override
	public void addRow(Logger logger, int userId, FormData formData, DataRow<Cell> currentDataRow) {
		boolean canDo = false;
		FormTemplate formTemplate = formTemplateDao.get(formData.getFormTemplateId());
		if (!formTemplate.isFixedRows()) { // если строки в НФ не фиксированы
		if (formData.getId() == null) {
			canDo = formDataAccessService.canCreate(userId,
					formData.getFormTemplateId(), formData.getKind(),
					formData.getDepartmentId(), formData.getReportPeriodId());
		} else {
			canDo = formDataAccessService.canEdit(userId, formData.getId());
		}
		}

		if (canDo) {
			if (formDataScriptingService.hasScripts(formData,
					FormDataEvent.ADD_ROW)
					&& !reportPeriodDao.get(formData.getReportPeriodId()).isBalancePeriod()) {
				TAUser user = userDao.getUser(userId);
				Map<String, Object> additionalParameters = new HashMap<String, Object>();
				additionalParameters.put("currentDataRow", currentDataRow);
				formDataScriptingService.executeScript(user, formData,
						FormDataEvent.ADD_ROW, logger, additionalParameters);
				if (logger.containsLevel(LogLevel.ERROR)) {
					throw new ServiceLoggerException(
							"Произошли ошибки в скрипте добавления новой строки",
							logger.getEntries());
				}
			} else {
				formData.appendDataRow();
			}
		} else {
			throw new AccessDeniedException(
					"Недостаточно прав для добавления строки к налоговой форме");
		}

	}

	/**
	 * Выполнить расчёты по налоговой форме
	 *
	 * @param logger
	 *            логгер-объект для фиксации диагностических сообщений
	 * @param userId
	 *            идентификатор пользователя, запросившего операцию
	 * @param formData
	 *            объект с данными по налоговой форме
	 */
	@Override
	public void doCalc(Logger logger, int userId, FormData formData) {
		boolean canDo;
		if (formData.getId() == null) {
			canDo = formDataAccessService.canCreate(userId,
					formData.getFormTemplateId(), formData.getKind(),
					formData.getDepartmentId(), formData.getReportPeriodId());
		} else {
			canDo = formDataAccessService.canEdit(userId, formData.getId());
		}

		if (canDo) {
			TAUser user = userDao.getUser(userId);
			formDataScriptingService.executeScript(user, formData,
					FormDataEvent.CALCULATE, logger, null);

			// Проверяем ошибки при пересчете
			if (logger.containsLevel(LogLevel.ERROR)) {
				throw new ServiceLoggerException(
						"Найдены ошибки при выполнении расчета формы",
						logger.getEntries());
			} else {
				logger.info("Расчет завершен, ошибок не обнаружено");
			}

		} else {
			throw new AccessDeniedException(
					"Недостаточно прав для выполенения расчёта по налоговой форме");
		}
	}

	@Override
	public void doCheck(Logger logger, int userId, FormData formData) {
		TAUser user = userDao.getUser(userId);
		formDataScriptingService.executeScript(user, formData,
				FormDataEvent.CHECK, logger, null);

		// Проверяем ошибки при пересчете
		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Найдены ошибки при выполнении проверки формы",
					logger.getEntries());
		} else {
			logger.info("Проверка завершена, ошибок не обнаружено");
		}

	}

	/**
	 * Сохранить данные по налоговой форме
	 *
	 * @param userId
	 *            идентификатор пользователя, выполняющего операцию
	 * @param formData
	 *            объект с данными налоговой формы
	 * @return идентификатор сохранённой записи
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException
	 *             если у пользователя нет прав редактировать налоговую форму с
	 *             такими параметрами или форма заблокирована другим
	 *             пользователем
	 */
	@Override
	@Transactional
	public long saveFormData(Logger logger, int userId, FormData formData) {
		checkLockedByAnotherUser(formData.getId(), userId);
		boolean canDo;
		if (formData.getId() == null) {
			canDo = formDataAccessService.canCreate(userId,
					formData.getFormTemplateId(), formData.getKind(),
					formData.getDepartmentId(), formData.getReportPeriodId());
		} else {
			canDo = formDataAccessService.canEdit(userId, formData.getId());
		}

		if (canDo) {
			TAUser user = userDao.getUser(userId);
			formDataScriptingService.executeScript(user, formData,
					FormDataEvent.SAVE, logger, null);

			boolean needLock = formData.getId() == null;
			long id = formDataDao.save(formData);
			if (needLock) {
				lock(id, userId);
			}

			addLogBusiness(formData.getId(), user, FormDataEvent.SAVE, null);

			return id;
		} else {
			throw new AccessDeniedException(
					"Недостаточно прав для изменения налоговой формы");
		}
	}

	/**
	 * Получить данные по налоговой форме
	 *
	 * @param userId
	 *            идентификатор пользователя, выполняющего операцию
	 * @param formDataId
	 *            идентификатор записи, которую необходимо считать
	 * @param logger
	 *            логгер-объект для фиксации диагностических сообщений
	 * @return объект с данными по налоговой форме
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException
	 *             если у пользователя нет прав просматривать налоговую форму с
	 *             такими параметрами
	 */
	@Override
	@Transactional
	public FormData getFormData(int userId, long formDataId, Logger logger) {
		if (formDataAccessService.canRead(userId, formDataId)) {

			FormData formData = formDataDao.get(formDataId);

			formDataScriptingService.executeScript(userDao.getUser(userId),
					formData, FormDataEvent.AFTER_LOAD, logger, null);

			if (logger.containsLevel(LogLevel.ERROR)) {
				throw new ServiceLoggerException(
						"Произошли ошибки в скрипте, который выполняется после загрузки формы",
						logger.getEntries());
			}

			return formData;
		} else {
			throw new AccessDeniedException(
					"Недостаточно прав на просмотр данных налоговой формы",
					userId, formDataId);
		}
	}

	/**
	 * Удалить данные по налоговой форме
	 *
	 * @param userId
	 *            идентификатор пользователя, выполняющего операцию
	 * @param formDataId
	 *            идентификатор записи, котрую нужно удалить
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException
	 *             если у пользователя недостаточно прав для удаления записи
	 */
	@Override
	@Transactional
	public void deleteFormData(int userId, long formDataId) {
		checkLockedByAnotherUser(formDataId, userId);
		if (formDataAccessService.canDelete(userId, formDataId)) {
			formDataDao.delete(formDataId);
		} else {
			throw new AccessDeniedException(
					"Недостаточно прав для удаления налоговой формы");
		}
	}

	/**
	 * Перемещает форму из одного состояния в другое.
	 *
	 * @param formDataId
	 *            идентификатор налоговой формы
	 * @param userId
	 *            идентификатор текщуего пользователя
	 * @param workflowMove
	 *            переход
	 */
	@Override
	public void doMove(long formDataId, int userId, WorkflowMove workflowMove, String note, Logger logger) {
		checkLockedByAnotherUser(formDataId, userId);
		List<WorkflowMove> availableMoves = formDataAccessService
				.getAvailableMoves(userId, formDataId);
		if (!availableMoves.contains(workflowMove)) {
			throw new ServiceException(
					"Переход \""
							+ workflowMove
							+ "\" из текущего состояния невозможен, или пользователя с id = "
							+ userId
							+ " не хватает полномочий для его осуществления");
		}

		FormData formData = formDataDao.get(formDataId);
		formDataScriptingService.executeScript(userDao.getUser(userId),
				formData, workflowMove.getEvent(), logger, null);
		if (!logger.containsLevel(LogLevel.ERROR)) {
			formDataWorkflowDao
					.changeFormDataState(
							formDataId,
							workflowMove.getToState(),
							workflowMove.getToState().equals(
									WorkflowState.ACCEPTED) ? new Date() : null);

			TAUser user = userDao.getUser(userId);

			if (workflowMove.getAfterEvent() != null) {
				formDataScriptingService.executeScript(
						user, formData,
						workflowMove.getAfterEvent(), logger, null);
				if (logger.containsLevel(LogLevel.ERROR)) {
					throw new ServiceLoggerException(
							"Произошли ошибки в скрипте, который выполняется после перехода",
							logger.getEntries());
				}
			}

			addLogBusiness(formData.getId(), user, workflowMove.getEvent(), note);

		} else {
			throw new ServiceLoggerException(
					"Произошли ошибки в скрипте, который выполняется перед переходом",
					logger.getEntries());
		}
	}

	/**
	 * Проверяет, не заблокирована ли форма другим пользователем
	 *
	 * @param formDataId
	 * @param userId
	 */
	private void checkLockedByAnotherUser(Long formDataId, int userId) {
		if (formDataId != null) {
			ObjectLock<Long> objectLock = getObjectLock(formDataId);
			if (objectLock != null && objectLock.getUserId() != userId) {
				throw new AccessDeniedException(
						"Форма заблокирована другим пользователем");
			}
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean lock(long formDataId, int userId) {
		ObjectLock<Long> objectLock = getObjectLock(formDataId);
		if (objectLock != null && objectLock.getUserId() != userId) {
			return false;
		} else {
			lockDao.lockObject(formDataId, FormData.class, userId);
			return true;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean unlock(long formDataId, int userId) {
		ObjectLock<Long> objectLock = getObjectLock(formDataId);
		if (objectLock != null && objectLock.getUserId() != userId) {
			return false;
		} else {
			lockDao.unlockObject(formDataId, FormData.class, userId);
			return true;
		}

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean unlockAllByUserId(int userId) {
		lockDao.unlockAllObjectByUserId(userId);
		return true;// TODO обработать возможные ошибки
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ObjectLock<Long> getObjectLock(long formDataId) {
		return lockDao.getObjectLock(formDataId, FormData.class);
	}

	@Override
	public void deleteRow(Logger logger, int userId, FormData formData, DataRow<Cell> currentDataRow) {
		boolean canDo = false;
		FormTemplate formTemplate = formTemplateDao.get(formData.getFormTemplateId());
		if (!formTemplate.isFixedRows()) { // если строки в НФ не фиксированы
		if (formData.getId() == null) {
			canDo = formDataAccessService.canCreate(userId,
					formData.getFormTemplateId(), formData.getKind(),
					formData.getDepartmentId(), formData.getReportPeriodId());
		} else {
				canDo = formDataAccessService.canEdit(userId, formData.getId());
			}
		}

		if (canDo) {
			if (formDataScriptingService.hasScripts(formData,
					FormDataEvent.DELETE_ROW)
					&& !reportPeriodDao.get(formData.getReportPeriodId()).isBalancePeriod()) {
				TAUser user = userDao.getUser(userId);
				Map<String, Object> additionalParameters = new HashMap<String, Object>();
				additionalParameters.put("currentDataRow", currentDataRow);
				formDataScriptingService.executeScript(user, formData,
						FormDataEvent.DELETE_ROW, logger, additionalParameters);
				if (logger.containsLevel(LogLevel.ERROR)) {
					throw new ServiceLoggerException(
							"Произошли ошибки в скрипте удаление строки",
							logger.getEntries());
				}

			}else{
				formData.deleteDataRow(currentDataRow);
			}
		}else {
			throw new AccessDeniedException(
					"Недостаточно прав для удаления строки из налоговой формы");
		}
	}

	private void addLogBusiness(Long formDataId, TAUser user, FormDataEvent event, String note) {
		LogBusiness log = new LogBusiness();
		log.setFormId(formDataId);
		log.setEventId(event.getCode());
		log.setUserId(user.getId());
		log.setLogDate(new Date());
		log.setNote(note);

		StringBuilder roles = new StringBuilder();
		for (TARole role : user.getRoles()) {
			roles.append(role.getName());
		}
		log.setRoles(roles.toString());

		logBusinessService.add(log);
	}

}
