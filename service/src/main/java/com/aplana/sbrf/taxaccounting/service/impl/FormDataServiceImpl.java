package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.ConfigurationProvider;
import com.aplana.sbrf.taxaccounting.core.api.LockCoreService;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.impl.eventhandler.EventLauncher;
import com.aplana.sbrf.taxaccounting.service.shared.FormDataCompositionService;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
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

    private static final String XLSX_EXT = "xlsx";
    private static final String XLS_EXT = "xls";
    private static final String ERROR_PERIOD = "Переход невозможен, т.к. у одного из приемников период не открыт.";

    @Autowired
	private FormDataDao formDataDao;
    @Autowired
    private DeclarationDataDao declarationDataDao;
	@Autowired
	private FormTemplateDao formTemplateDao;
    @Autowired
    private FormTemplateService formTemplateService;
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
    private DepartmentDeclarationTypeDao departmentDeclarationTypeDao;
    @Autowired
    private PeriodService reportPeriodService;
    @Autowired
    private EventLauncher eventHandlerLauncher;
	@Autowired
	private SignService signService;
	@Autowired
	private ConfigurationProvider configurationProvider;
	@Autowired
	private ApplicationContext applicationContext;
    @Autowired
    private LogEntryService logEntryService;

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
     * @param periodOrder номер месяца для ежемесячных форм (для остальных параметр отсутствует)
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
			int formTemplateId, int departmentId, FormDataKind kind, ReportPeriod reportPeriod, Integer periodOrder) {
        formDataAccessService.canCreate(userInfo, formTemplateId, kind,
                departmentId, reportPeriod.getId());
        return createFormDataWithoutCheck(logger, userInfo, formTemplateId, departmentId, kind, reportPeriod.getId(),
                periodOrder, false);
	}

    @Override
    public void importFormData(Logger logger, TAUserInfo userInfo, long formDataId, InputStream inputStream, String fileName) {
        loadFormData(logger, userInfo, formDataId, inputStream, fileName, FormDataEvent.IMPORT);
    }

    @Override
    @Transactional
    public void migrationFormData(Logger logger, TAUserInfo userInfo, long formDataId, InputStream inputStream, String fileName) {
        loadFormData(logger, userInfo, formDataId, inputStream, fileName, FormDataEvent.MIGRATION);
    }

    private void loadFormData(Logger logger, TAUserInfo userInfo, long formDataId, InputStream inputStream, String fileName, FormDataEvent formDataEvent) {
		// Поскольку импорт используется как часть редактирования НФ, т.е. иморт только строк (форма уже существует) то все проверки должны 
    	// соответствовать редактированию (добавление, удаление, пересчет)
    	// Форма должна быть заблокирована текущим пользователем для редактирования
		lockCoreService.checkLockedMe(FormData.class, formDataId, userInfo);

        formDataAccessService.canEdit(userInfo, formDataId);

        File dataFile = null;
        File pKeyFile = null;
        OutputStream dataFileOutputStream = null;
        InputStream dataFileInputStream = null;

        try {

            dataFile = File.createTempFile("dataFile", ".original");
            dataFileOutputStream = new BufferedOutputStream(new FileOutputStream(dataFile));
            IOUtils.copy(inputStream, dataFileOutputStream);
            IOUtils.closeQuietly(dataFileOutputStream);

            String ext = getFileExtention(fileName);
            if(!ext.equals(XLS_EXT) && !ext.equals(XLSX_EXT)){

                String pKeyFileUrl = configurationProvider.getString(ConfigurationParam.FORM_DATA_KEY_FILE);
                if (pKeyFileUrl != null) { // Необходимо проверить подпись
                    InputStream pKeyFileInputStream = null;

                    pKeyFile = File.createTempFile("signature", ".sign");
                    OutputStream pKeyFileOutputStream = new BufferedOutputStream(new FileOutputStream(pKeyFile));
                    try {
                        pKeyFileInputStream = new BufferedInputStream(ResourceUtils.getSharedResourceAsStream(pKeyFileUrl));
                        IOUtils.copy(pKeyFileInputStream, pKeyFileOutputStream);
                    } catch (Exception e) {
                        throw new ServiceException("Ошибка доступа к файлу базы открытых ключей.", e);
                    } finally {
                        IOUtils.closeQuietly(pKeyFileOutputStream);
                        IOUtils.closeQuietly(pKeyFileInputStream);
                    }
                    if (!signService.checkSign(dataFile.getAbsolutePath(), pKeyFile.getAbsolutePath(), 0)) {
                        throw new ServiceException("Ошибка проверки цифровой подписи.");
                    }
                }
            }

            FormData fd = formDataDao.get(formDataId);

            dataFileInputStream = new BufferedInputStream(new FileInputStream(dataFile));
            Map<String, Object> additionalParameters = new HashMap<String, Object>();
            additionalParameters.put("ImportInputStream", dataFileInputStream);
            additionalParameters.put("UploadFileName", fileName);
            formDataScriptingService.executeScript(userInfo, fd, formDataEvent, logger, additionalParameters);
            IOUtils.closeQuietly(dataFileInputStream);

            if (logger.containsLevel(LogLevel.ERROR)) {
                throw new ServiceLoggerException(
                        "Есть критические ошибки при выполнения скрипта.", logEntryService.save(logger.getEntries()));
            } else {
                logger.info("Данные загружены");
            }

            logBusinessService.add(formDataId, null, userInfo, formDataEvent, null);
            auditService.add(formDataEvent, userInfo, fd.getDepartmentId(), fd.getReportPeriodId(),
                    null, fd.getFormType().getId(), fd.getKind().getId(), fileName);
        } catch (IOException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        } finally {
            IOUtils.closeQuietly(dataFileOutputStream);
            IOUtils.closeQuietly(dataFileInputStream);
            if (dataFile != null) {
                dataFile.delete();
            }
            if (pKeyFile != null) {
                pKeyFile.delete();
            }
        }
    }

    private static String getFileExtention(String filename){
        int dotPos = filename.lastIndexOf('.') + 1;
        return filename.substring(dotPos);
    }

    @Override
	public long createFormDataWithoutCheck(Logger logger, TAUserInfo userInfo, int formTemplateId, int departmentId,
                                           FormDataKind kind, int reportPeriodId, Integer periodOrder, boolean importFormData) {
		FormTemplate formTemplate = formTemplateService.getFullFormTemplate(formTemplateId);
		FormData formData = new FormData(formTemplate);
		
		formData.setState(WorkflowState.CREATED);
		formData.setDepartmentId(departmentId);
		formData.setKind(kind);
		formData.setReportPeriodId(reportPeriodId);
        formData.setPeriodOrder(periodOrder);
		
		// Execute scripts for the form event CREATE
		formDataScriptingService.executeScript(userInfo, formData,
				importFormData ? FormDataEvent.IMPORT : FormDataEvent.CREATE, logger, null);

		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Произошли ошибки в скрипте создания налоговой формы",
                    logEntryService.save(logger.getEntries()));
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

		formDataAccessService.canEdit(userInfo, formData.getId());

		Map<String, Object> additionalParameters = new HashMap<String, Object>();
		additionalParameters.put("currentDataRow", currentDataRow);
		formDataScriptingService.executeScript(userInfo, formData,
				FormDataEvent.ADD_ROW, logger, additionalParameters);
		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Произошли ошибки в скрипте добавления новой строки",
                    logEntryService.save(logger.getEntries()));
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

		formDataAccessService.canEdit(userInfo, formData.getId());
		
		Map<String, Object> additionalParameters = new HashMap<String, Object>();
		additionalParameters.put("currentDataRow", currentDataRow);
		formDataScriptingService.executeScript(userInfo, formData,
				FormDataEvent.DELETE_ROW, logger, additionalParameters);
		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Произошли ошибки в скрипте удаления строки",
                    logEntryService.save(logger.getEntries()));
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

		formDataAccessService.canEdit(userInfo, formData.getId());

		formDataScriptingService.executeScript(userInfo, formData,
				FormDataEvent.CALCULATE, logger, null);

		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException("Найдены ошибки при выполнении расчета формы", logEntryService.save(logger.getEntries()));
		} else {
			logger.info("Расчет завершен, фатальных ошибок не обнаружено");
		}
	}

	@Override
    @Transactional(noRollbackFor = ServiceLoggerException.class)
	public void doCheck(Logger logger, TAUserInfo userInfo, FormData formData) {
		// Форма не должна быть заблокирована для редактирования другим пользователем
		lockCoreService.checkNoLockedAnother(FormData.class, formData.getId(), userInfo);
		// Временный срез формы должен быть в актуальном состоянии
		// Если не заблокировано то откат среза на всякий случай
		if (getObjectLock(formData.getId(), userInfo)==null){
			dataRowDao.rollback(formData.getId());
		}

		formDataAccessService.canRead(userInfo, formData.getId());

		formDataScriptingService.executeScript(userInfo, formData, FormDataEvent.CHECK, logger, null);

		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Найдены ошибки при выполнении проверки формы", logEntryService.save(logger.getEntries()));
		} else {
			// Ошибка для отката транзакции
			logger.info("Проверка завершена, фатальных ошибок не обнаружено");
			throw new ServiceLoggerException("Ошибок не обнаружено", logEntryService.save(logger.getEntries()));
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

		formDataAccessService.canEdit(userInfo, formData.getId());

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
		formDataAccessService.canRead(userInfo, formDataId);

		FormData formData = formDataDao.get(formDataId);

		formDataScriptingService.executeScript(userInfo,
				formData, FormDataEvent.AFTER_LOAD, logger, null);

		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Произошли ошибки в скрипте, который выполняется после загрузки формы",
                    logEntryService.save(logger.getEntries()));
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

		formDataAccessService.canDelete(userInfo, formDataId);

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

		formDataScriptingService.executeScript(userInfo,formData, workflowMove.getEvent(), logger, null);
		
		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Произошли ошибки в скрипте, который выполняется перед переходом",
                    logEntryService.save(logger.getEntries()));
		}
	
		eventHandlerLauncher.process(userInfo, formData, workflowMove.getEvent(), logger, null);

		if (workflowMove.getAfterEvent() != null) {
			formDataScriptingService.executeScript(
					userInfo, formData,
					workflowMove.getAfterEvent(), logger, null);
			if (logger.containsLevel(LogLevel.ERROR)) {
				throw new ServiceLoggerException(
						"Произошли ошибки в скрипте, который выполняется после перехода",
                        logEntryService.save(logger.getEntries()));
			} else {
                compose(workflowMove, formData, userInfo, logger);
            }
		}

        dataRowDao.commit(formData.getId());

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
    void compose(WorkflowMove workflowMove, FormData formData, TAUserInfo userInfo, Logger logger){
        // Проверка перехода ЖЦ. Принятие либо отмена принятия
        if (workflowMove.getToState() == WorkflowState.ACCEPTED || workflowMove.getFromState() == WorkflowState.ACCEPTED) {
            // признак периода ввода остатков
            if (!reportPeriodService.isBalancePeriod(formData.getReportPeriodId(), formData.getDepartmentId())) {
                // получение списка типов приемников для текущей формы
                List<DepartmentFormType> departmentFormTypes = departmentFormTypeDao.getFormDestinations(formData.getDepartmentId(), formData.getFormType().getId(), formData.getKind());
                // Если найдены приемники то обработаем их
                if (departmentFormTypes != null && !departmentFormTypes.isEmpty()) {
                    for (DepartmentFormType i: departmentFormTypes) {
                        // получим созданные формы с бд
                        FormData destinationForm = formDataDao.find(i.getFormTypeId(), i.getKind(), i.getDepartmentId(), formData.getReportPeriodId());
                        //В связи с http://jira.aplana.com/browse/SBRFACCTAX-4723
                        // Только для распринятия
                        if (destinationForm == null && workflowMove.getFromState() == WorkflowState.ACCEPTED)
                            continue;
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

							FormDataCompositionService formDataCompositionService = applicationContext.getBean(FormDataCompositionService.class);
                            ((ScriptComponentContextHolder)formDataCompositionService).setScriptComponentContext(scriptComponentContext);
                            formDataCompositionService.compose(formData, i.getDepartmentId(),
                                    i.getFormTypeId(), i.getKind());
                        } else if (destinationForm != null){
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

    @Override
    @Transactional(readOnly = true)
    public void checkDestinations(long formDataId) {
        FormData formData = formDataDao.get(formDataId);
        // Проверка вышестоящих налоговых форм
        List<DepartmentFormType> departmentFormTypes =
                departmentFormTypeDao.getFormDestinations(formData.getDepartmentId(),
                        formData.getFormType().getId(), formData.getKind());
        if (departmentFormTypes != null) {
            for (DepartmentFormType departmentFormType : departmentFormTypes) {
                FormData form = formDataDao.find(departmentFormType.getFormTypeId(), departmentFormType.getKind(),
                        departmentFormType.getDepartmentId(), formData.getReportPeriodId());
                // Если форма существует и статус отличен от "Создана"
                if (form != null && form.getState() != WorkflowState.CREATED) {
                    throw new ServiceException("Переход невозможен, т.к. уже подготовлена/утверждена/принята вышестоящая налоговая форма.");
                }
                if (!reportPeriodService.isActivePeriod(formData.getReportPeriodId(), departmentFormType.getDepartmentId())) {
                    throw new ServiceException(ERROR_PERIOD);
                }
            }
        }

        // Проверка вышестоящих деклараций
        List<DepartmentDeclarationType> departmentDeclarationTypes = departmentDeclarationTypeDao.getDestinations(
                formData.getDepartmentId(), formData.getFormType().getId(), formData.getKind());
        if (departmentDeclarationTypes != null) {
            for (DepartmentDeclarationType departmentDeclarationType : departmentDeclarationTypes) {
                DeclarationData declaration = declarationDataDao.find(departmentDeclarationType.getDeclarationTypeId(),
                        departmentDeclarationType.getDepartmentId(), formData.getReportPeriodId());
                // Если декларация существует и статус "Принята"
                if (declaration != null && declaration.isAccepted()) {
                    String str = formData.getFormType().getTaxType() == TaxType.DEAL ? "принято уведомление" :
                            "принята декларация";
                    throw new ServiceException("Переход невозможен, т.к. уже " + str + ".");
                }
                assert declaration != null;
                if (!reportPeriodService.isActivePeriod(formData.getReportPeriodId(), declaration.getDepartmentId())) {
                    throw new ServiceException(ERROR_PERIOD);
                }
            }
        }
    }

    @Override
    public List<Long> getFormDataLisByVersionTemplate(int formTemplateId) {
        return formDataDao.findFormDataByFormTemplate(formTemplateId);
    }
}
