package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockCoreService;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.impl.eventhandler.EventLauncher;
import com.aplana.sbrf.taxaccounting.service.shared.FormDataCompositionService;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с {@link FormData данными по налоговым формам}.
 *
 * @author Vitalii Samolovskikh
 */
@Service("unlockFormData")
@Transactional
public class FormDataServiceImpl implements FormDataService {

	public static final String IGNORE_URL = "http://ignore/";

    private final Log log = LogFactory.getLog(getClass());

	@Autowired
	private FormDataDao formDataDao;
	@Autowired
	private FormTemplateDao formTemplateDao;
	@Autowired
	private FormDataAccessService formDataAccessService;
	@Autowired
	private FormDataScriptingService formDataScriptingService;
	@Autowired
	private LockCoreService lockCoreService;
	@Autowired
	private LogBusinessService logBusinessService;
	@Autowired
	private AuditService auditService;
	@Autowired
	private DataRowDao dataRowDao;
    @Autowired
    private DepartmentFormTypeDao departmentFormTypeDao;
    @Autowired
    private FormDataCompositionService formDataCompositionService;
    @Autowired
    private PeriodService reportPeriodService;
    @Autowired
    private EventLauncher eventHandlerLauncher;
	@Autowired
	private SignService signService;
	@Autowired(required = false)
	private URL signDataPath;

	/**
	 * Создать налоговую форму заданного типа При создании формы выполняются
	 * следующие действия: 1) создаётся пустой объект 2) если в объявлении формы
	 * заданы строки по-умолчанию (начальные данные), то эти строки копируются в
	 * созданную форму 3) если в объявлении формы задан скрипт создания, то этот
	 * скрипт выполняется над создаваемой формой
	 *
	 * @param logger
	 *            логгер-объект для фиксации диагностических сообщений
	 * @param userInfo
	 *            информация о пользователе, запросившего операцию
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
	public long createFormData(Logger logger, TAUserInfo userInfo,
			int formTemplateId, int departmentId, FormDataKind kind, ReportPeriod reportPeriod) {
		if (formDataAccessService.canCreate(userInfo, formTemplateId, kind,
				departmentId, reportPeriod.getId())) {
			return createFormDataWithoutCheck(logger, userInfo,
					formTemplateId, departmentId, kind, reportPeriod.getId(), false);
		} else {
			throw new AccessDeniedException(
					"Недостаточно прав для создания налоговой формы с указанными параметрами");
		}
	}
	
	
	@Override
	@Deprecated
	public void importFormDataTest(Logger logger, TAUserInfo userInfo, int formTemplateId, int departmentId, FormDataKind kind, int reportPeriodId) {
		Date serviceStart = new Date();
		long formDataId =  createFormDataWithoutCheck(logger, userInfo, formTemplateId, departmentId, kind, reportPeriodId, true);
		Date getDate = new Date();
		FormData fd = formDataDao.get(formDataId);
		logger.info("Старт сервиса: " + serviceStart);
		logger.info("Получение: " + getDate);
		logger.info("Текущая: " + new Date());
		logBusinessService.add(formDataId, null, userInfo, FormDataEvent.IMPORT, null);
		auditService.add(FormDataEvent.IMPORT, userInfo, fd.getDepartmentId(), fd.getReportPeriodId(),
				null, fd.getFormType().getId(), fd.getKind().getId(), null);
	}

    @Override
    public void importFormData(Logger logger, TAUserInfo userInfo, long formDataId, InputStream inputStream, String fileName) {
        loadFormData(logger, userInfo, formDataId, inputStream, fileName, FormDataEvent.IMPORT);
    }

    @Override
    @Transactional(timeout = 900) // 15 min // TODO Вынести в глобальные настройки
    public void migrationFormData(Logger logger, TAUserInfo userInfo, long formDataId, InputStream inputStream, String fileName) {
        loadFormData(logger, userInfo, formDataId, inputStream, fileName, FormDataEvent.MIGRATION);
    }

    private void loadFormData(Logger logger, TAUserInfo userInfo, long formDataId, InputStream inputStream, String fileName, FormDataEvent formDataEvent) {
		// Поскольку импорт используется как часть редактирования НФ, т.е. иморт только строк (форма уже существует) то все проверки должны 
    	// соответствовать редактированию (добавление, удаление, пересчет)
    	// Форма должна быть заблокирована текущим пользователем для редактирования
		lockCoreService.checkLockedMe(FormData.class, formDataId, userInfo);
		
        if (!formDataAccessService.canEdit(userInfo, formDataId)) {
            throw new AccessDeniedException(
                    "Недостаточно прав для импорта данных в налоговую форму");
        }

        boolean checkSuccess = true;
        File signFileName = null;
        File dataFile = null;
        if ((signDataPath != null) && !signDataPath.toString().equals(IGNORE_URL)) { //TODO временное решение с IGNORE_URL
	        try {
                log.info("Validate signature.");
                dataFile = File.createTempFile("dataFile", ".original");
                signFileName = File.createTempFile("signature", ".sign");
		        OutputStream outputStream =
				        new FileOutputStream(dataFile);
		        IOUtils.copy(inputStream, outputStream);
		        OutputStream outputSignStream =
				        new FileOutputStream(signFileName);
		        IOUtils.copy(signDataPath.openStream(), outputSignStream);
		        checkSuccess = signService.checkSign(dataFile.getAbsolutePath(), signFileName.getAbsolutePath(), 0);
                inputStream = new FileInputStream(dataFile);
                log.info("Temporary files: " + dataFile + " : " + signFileName);
	        } catch (Exception e) {
		        throw new ServiceException("Произошла ошибка при проверке подписи.", e);
	        } finally {
                if(signFileName != null){
                    signFileName.delete();
                }
            }
        }
        if (!checkSuccess) {
	        throw new ServiceException("Электронная подпись некорректна.");
        }
        
        FormData fd = formDataDao.get(formDataId);

        Map<String, Object> additionalParameters = new HashMap<String, Object>();
        additionalParameters.put("ImportInputStream", inputStream);
        additionalParameters.put("UploadFileName", fileName);
        formDataScriptingService.executeScript(userInfo, fd, formDataEvent, logger, additionalParameters);
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(
                    "Есть критические ошибки при выполнения скрипта",
                    logger.getEntries());
        }  else {
            logger.info("Данные загружены");
        }
        
        logBusinessService.add(formDataId, null, userInfo, formDataEvent, null);
        auditService.add(formDataEvent, userInfo, fd.getDepartmentId(), fd.getReportPeriodId(),
                null, fd.getFormType().getId(), fd.getKind().getId(), fileName);
        
        IOUtils.closeQuietly(inputStream);
        
        if (dataFile != null){
            dataFile.delete();
        }
    }

    @Override
	public long createFormDataWithoutCheck(Logger logger, TAUserInfo userInfo,
			int formTemplateId, int departmentId, FormDataKind kind, int reportPeriodId, boolean importFormData) {
		FormTemplate formTemplate = formTemplateDao.get(formTemplateId);
		FormData formData = new FormData(formTemplate);
		
		formData.setState(WorkflowState.CREATED);
		formData.setDepartmentId(departmentId);
		formData.setKind(kind);
		formData.setReportPeriodId(reportPeriodId);
		
		// Execute scripts for the form event CREATE
		formDataScriptingService.executeScript(userInfo, formData,
				importFormData ? FormDataEvent.IMPORT : FormDataEvent.CREATE, logger,null);

		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Произошли ошибки в скрипте создания налоговой формы",
					logger.getEntries());
		}

		formDataDao.save(formData);
		// Заполняем начальные строки (но не сохраняем)
		dataRowDao.saveRows(formData, formTemplate.getRows());

		dataRowDao.commit(formData.getId());
		
		logBusinessService.add(formData.getId(), null, userInfo, FormDataEvent.CREATE, null);
		auditService.add(FormDataEvent.CREATE, userInfo, formData.getDepartmentId(), formData.getReportPeriodId(),
				null, formData.getFormType().getId(), formData.getKind().getId(), null);

		return formData.getId();
	}

	/**
	 * Добавляет строку в форму и выполняет соответствующие скрипты.
	 *
	 * @param logger
	 *            логгер для регистрации ошибок
	 * @param userInfo
	 *            информация о пользователе
	 * @param formData
	 *            данные формы
	 */
	@Override
	public void addRow(Logger logger, TAUserInfo userInfo, FormData formData, DataRow<Cell> currentDataRow) {
		// Форма должна быть заблокирована текущим пользователем для редактирования
		lockCoreService.checkLockedMe(FormData.class, formData.getId(), userInfo);

		FormTemplate formTemplate = formTemplateDao.get(formData.getFormTemplateId());
		
		if (formTemplate.isFixedRows()) {
			throw new ServiceException("Нельзя добавить строку в НФ с фиксированным количеством строк");
		}
		
		if (!formDataAccessService.canEdit(userInfo, formData.getId())) {
			throw new AccessDeniedException(
					"Недостаточно прав для добавления строки к налоговой форме");
		}

		Map<String, Object> additionalParameters = new HashMap<String, Object>();
		additionalParameters.put("currentDataRow", currentDataRow);
		formDataScriptingService.executeScript(userInfo, formData,
				FormDataEvent.ADD_ROW, logger, additionalParameters);
		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Произошли ошибки в скрипте добавления новой строки",
					logger.getEntries());
		}

	}
	
	@Override
	public void deleteRow(Logger logger, TAUserInfo userInfo, FormData formData, DataRow<Cell> currentDataRow) {
		// Форма должна быть заблокирована текущим пользователем для редактирования
		lockCoreService.checkLockedMe(FormData.class, formData.getId(), userInfo);

		FormTemplate formTemplate = formTemplateDao.get(formData.getFormTemplateId());
		
		if (formTemplate.isFixedRows()) {
			throw new ServiceException("Нельзя удалить строку в НФ с фиксированным количеством строк");
		}
		
		if (!formDataAccessService.canEdit(userInfo, formData.getId())) {
			throw new AccessDeniedException(
					"Недостаточно прав для удаления строки из налоговой формы");
		}
		
		Map<String, Object> additionalParameters = new HashMap<String, Object>();
		additionalParameters.put("currentDataRow", currentDataRow);
		formDataScriptingService.executeScript(userInfo, formData,
				FormDataEvent.DELETE_ROW, logger, additionalParameters);
		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Произошли ошибки в скрипте удаление строки",
					logger.getEntries());
		}

	}

	/**
	 * Выполнить расчёты по налоговой форме
	 *
	 * @param logger
	 *            логгер-объект для фиксации диагностических сообщений
	 * @param userInfo
	 *            информация о пользователе, запросившего операцию
	 * @param formData
	 *            объект с данными по налоговой форме
	 */
	@Override
	public void doCalc(Logger logger, TAUserInfo userInfo, FormData formData) {
		// Форма должна быть заблокирована текущим пользователем для редактирования
		lockCoreService.checkLockedMe(FormData.class, formData.getId(), userInfo);
		
		if (!formDataAccessService.canEdit(userInfo, formData.getId())) {
			throw new AccessDeniedException(
					"Недостаточно прав для выполенения расчёта по налоговой форме");
		}

		formDataScriptingService.executeScript(userInfo, formData,
				FormDataEvent.CALCULATE, logger, null);

		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Найдены ошибки при выполнении расчета формы",
					logger.getEntries());
		} else {
			logger.info("Расчет завершен, фатальных ошибок не обнаружено");
		}

	}

	@Override
	public void doCheck(Logger logger, TAUserInfo userInfo, FormData formData) {
		// Форма не должна быть заблокирована для редактирования другим пользователем
		lockCoreService.checkNoLockedAnother(FormData.class, formData.getId(), userInfo);
		// Временный срез формы должен быть в актуальном состоянии
		// Если не заблокировано то откат среза на всякий случай
		if (getObjectLock(formData.getId(), userInfo)==null){
			dataRowDao.rollback(formData.getId());
		}
		
		if (!formDataAccessService.canRead(userInfo, formData.getId())) {
			throw new AccessDeniedException(
					"Недостаточно прав чтения формы");
		}
		
		formDataScriptingService.executeScript(userInfo, formData, FormDataEvent.CHECK, logger, null);

		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Найдены ошибки при выполнении проверки формы", logger.getEntries());
		} else {
			// Ошибка для отката транзакции
			logger.info("Проверка завершена, фатальных ошибок не обнаружено");
			throw new ServiceLoggerException(
					"Ошибок не обнаружено", logger.getEntries());
		}
	}

	/**
	 * Сохранить данные по налоговой форме
	 *
	 * @param userInfo
	 *            информация о пользователе, выполняющего операцию
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
	public long saveFormData(Logger logger, TAUserInfo userInfo, FormData formData) {
		// Форма должна быть заблокирована текущим пользователем для редактирования
		lockCoreService.checkLockedMe(FormData.class, formData.getId(), userInfo);
		
		if (!formDataAccessService.canEdit(userInfo, formData.getId())) {
			throw new AccessDeniedException(
					"Недостаточно прав для изменения налоговой формы");
		}

		formDataScriptingService.executeScript(userInfo, formData,
				FormDataEvent.SAVE, logger, null);

		formDataDao.save(formData);
		
		dataRowDao.commit(formData.getId());

		logBusinessService.add(formData.getId(), null, userInfo, FormDataEvent.SAVE, null);
		auditService.add(FormDataEvent.SAVE, userInfo, formData.getDepartmentId(), formData.getReportPeriodId(),
				null, formData.getFormType().getId(), formData.getKind().getId(), null);

		return formData.getId();

	}

	/**
	 * Получить данные по налоговой форме
	 *
	 * @param userInfo
	 *            информация о пользователе, выполняющего операцию
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
	public FormData getFormData(TAUserInfo userInfo, long formDataId, Logger logger) {
		if (!formDataAccessService.canRead(userInfo, formDataId)) {
			throw new AccessDeniedException(
					"Недостаточно прав на просмотр данных налоговой формы",
					userInfo.getUser().getId(), formDataId);
		}

		FormData formData = formDataDao.get(formDataId);

		formDataScriptingService.executeScript(userInfo,
				formData, FormDataEvent.AFTER_LOAD, logger, null);

		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Произошли ошибки в скрипте, который выполняется после загрузки формы",
					logger.getEntries());
		}

		return formData;

	}

	/**
	 * Удалить данные по налоговой форме
	 *
	 * @param userInfo
	 *            информация о пользователе, выполняющего операцию
	 * @param formDataId
	 *            идентификатор записи, котрую нужно удалить
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException
	 *             если у пользователя недостаточно прав для удаления записи
	 */
	@Override
	@Transactional
	public void deleteFormData(TAUserInfo userInfo, long formDataId) {
		// Форма не должна быть заблокирована для редактирования другим пользователем
		lockCoreService.checkNoLockedAnother(FormData.class, formDataId, userInfo);
		
		if (!formDataAccessService.canDelete(userInfo, formDataId)) {
			throw new AccessDeniedException(
					"Недостаточно прав для удаления налоговой формы");
		}

		FormData formData = formDataDao.get(formDataId);
		auditService.add(FormDataEvent.DELETE, userInfo, formData.getDepartmentId(), formData.getReportPeriodId(),
				null, formData.getFormType().getId(), formData.getKind().getId(), null);
		formDataDao.delete(formDataId);

	}

	/**
	 * Перемещает форму из одного состояния в другое.
	 *
	 * @param formDataId
	 *            идентификатор налоговой формы
	 * @param userInfo
	 *            информация о текущем пользователе
	 * @param workflowMove
	 *            переход
	 */
	@Override
	public void doMove(long formDataId, TAUserInfo userInfo, WorkflowMove workflowMove, String note, Logger logger) {
		// Форма не должна быть заблокирована даже текущим пользователем;
		lockCoreService.checkUnlocked(FormData.class, formDataId, userInfo);
   		// Временный срез формы должен быть в актуальном состоянии
		dataRowDao.rollback(formDataId);
		
		List<WorkflowMove> availableMoves = formDataAccessService.getAvailableMoves(userInfo, formDataId);
		if (!availableMoves.contains(workflowMove)) {
			throw new ServiceException(
					"Переход \""
							+ workflowMove
							+ "\" из текущего состояния невозможен, или пользователя с id = "
							+ userInfo.getUser().getId()
							+ " не хватает полномочий для его осуществления");
		}

		FormData formData = formDataDao.get(formDataId);
				
        checkDestinations(formData);

		formDataScriptingService.executeScript(userInfo,formData, workflowMove.getEvent(), logger, null);
		
		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Произошли ошибки в скрипте, который выполняется перед переходом",
					logger.getEntries());
		}
	
		eventHandlerLauncher.process(userInfo, formData, workflowMove.getEvent(), logger, null);

		if (workflowMove.getAfterEvent() != null) {
			formDataScriptingService.executeScript(
					userInfo, formData,
					workflowMove.getAfterEvent(), logger, null);
			if (logger.containsLevel(LogLevel.ERROR)) {
				throw new ServiceLoggerException(
						"Произошли ошибки в скрипте, который выполняется после перехода",
						logger.getEntries());
			} else {
                compose(workflowMove, formData, userInfo, logger);
            }
		}

		logBusinessService.add(formData.getId(), null, userInfo, workflowMove.getEvent(), note);
		auditService.add(workflowMove.getEvent(), userInfo, formData.getDepartmentId(), formData.getReportPeriodId(),
				null, formData.getFormType().getId(), formData.getKind().getId(), note);
	}

    /**
     * Логика консолидации при переходе жц
     *
     * Функция выполняет консолидацию форм приемников для текущей формы, при ее переходе
     * со статуса принята, либо в принята.
     *
     * Консолидация выполняется следующим образом.
     * Вызваеся метод compose сервиса formDataCompositionService (formDataCompositionService.compose)
     * для тех форм-приемников, у которых есть формы в статусе принята. Если у приемника нет форм в
     * статусе принята то форма приемник удаляется.
     *
     * Важно. Консолидация не выполняется в периоде ввода остатков
     *
     * <b>Замечение.</b> При поиске формы мы использум getFormDestinations котоый возвращает список
     * моделей DepartmentFormType. Для получения модели FormData используется поиск через
     * formDataDao.find, где одним из параметров подставляется formData.getReportPeriodId, и это нормально,
     * так как источник и приемник у нас находятся в одном отчетном периоде.
     *
     *  http://conf.aplana.com/pages/viewpage.action?pageId=8788114
     */
    private void compose(WorkflowMove workflowMove, FormData formData, TAUserInfo userInfo, Logger logger){
        // Проверка перехода ЖЦ. Принятие либо отмена принятия
        if (workflowMove.getToState() == WorkflowState.ACCEPTED || workflowMove.getFromState() == WorkflowState.ACCEPTED) {
            // признак периода ввода остатков
            if (!reportPeriodService.isBalancePeriod(formData.getReportPeriodId(), formData.getDepartmentId())) {
                // получение списка приемников для текущей формы
                List<DepartmentFormType> departmentFormTypes = departmentFormTypeDao.getFormDestinations(formData.getDepartmentId(), formData.getFormType().getId(), formData.getKind());
                // Если найдены приемники то обработаем их
                if (departmentFormTypes != null && !departmentFormTypes.isEmpty()) {
                    for (DepartmentFormType i: departmentFormTypes) {
                        FormData destinationForm = formDataDao.find(i.getFormTypeId(), i.getKind(), i.getDepartmentId(), formData.getReportPeriodId());
                        // получение источников для текущего приемника i
                        List<DepartmentFormType> sourceFormTypes = departmentFormTypeDao.getFormSources(i.getDepartmentId(), i.getFormTypeId(), i.getKind());
                        // количество источников в статусе принята
                        boolean existAcceptedSources = false;
                        for (DepartmentFormType s: sourceFormTypes){
                            FormData sourceForm = formDataDao.find(s.getFormTypeId(), s.getKind(), s.getDepartmentId(), formData.getReportPeriodId());
                            if (sourceForm !=null && sourceForm.getState().equals(WorkflowState.ACCEPTED)){
                                existAcceptedSources = true;
                                break;
                            }
                        }
                        // если текущая форма приемник имеет один и более источников в статусе принята то консолидируем ее, иначе удаляем
                        if (existAcceptedSources){
                            ScriptComponentContextImpl scriptComponentContext = new ScriptComponentContextImpl();
                            scriptComponentContext.setUserInfo(userInfo);
                            scriptComponentContext.setLogger(logger);
                            ((ScriptComponentContextHolder)formDataCompositionService).setScriptComponentContext(scriptComponentContext);

                            formDataCompositionService.compose(formData, i.getDepartmentId(),
                                    i.getFormTypeId(), i.getKind());
                        }else{
                            deleteFormData(userInfo, destinationForm.getId());
                        }
                    }
                }
            }
        }
    }

	@Override
	@Transactional
	public void lock(long formDataId, TAUserInfo userInfo) {
		lockCoreService.lock(FormData.class, formDataId, userInfo);
		dataRowDao.rollback(formDataId);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void unlock(long formDataId, TAUserInfo userInfo) {
			lockCoreService.unlock(FormData.class, formDataId, userInfo);
			dataRowDao.rollback(formDataId);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean unlockAllByUser(TAUserInfo userInfo) {
		//Это зло
		//lockDao.unlockAllObjectByUserId(userInfo.getUser().getId());
		return true;// TODO обработать возможные ошибки
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ObjectLock<Long> getObjectLock(long formDataId, TAUserInfo userInfo) {
		return lockCoreService.getLock(FormData.class, formDataId, userInfo);
	}

    /**
     * Проверка наличия и статуса приемника при осуществлении перевода формы
     * в статус "Подготовлена"/"Утверждена"/"Принята".
     */
    private void checkDestinations(FormData formData) {
        List<DepartmentFormType> departmentFormTypes =
                departmentFormTypeDao.getFormDestinations(formData.getDepartmentId(),
                        formData.getFormType().getId(), formData.getKind());
        if (departmentFormTypes != null) {
            for (DepartmentFormType department: departmentFormTypes) {
                FormData form = formDataDao.find(department.getFormTypeId(), department.getKind(),
                        department.getDepartmentId(), formData.getReportPeriodId());
                // если форма существует и статус отличен от "создана"
                if (form != null && form.getState() != WorkflowState.CREATED) {
                    throw new ServiceException("Переход невозможен, т.к. уже подготовлена/утверждена/принята вышестоящая налоговая форма.");
                }
                if (!reportPeriodService.isActivePeriod(formData.getReportPeriodId(), department.getDepartmentId())){
                	throw new ServiceException("Переход невозможен, т.к. у одного из приемников период не отрыт.");
                }
            }
        }
    }
}
