package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockCoreService;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.impl.eventhandler.EventLauncher;
import com.aplana.sbrf.taxaccounting.service.shared.FormDataCompositionService;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

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
    public static final String MSG_IS_EXIST_FORM = "Существует экземпляр налоговой формы %s типа %s в подразделении %s периоде %s";

    @Autowired
	private FormDataDao formDataDao;
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
	private ReportPeriodDao reportPeriodDao;
	@Autowired
	private DepartmentDao departmentDao;
    @Autowired
    private DepartmentFormTypeDao departmentFormTypeDao;
    @Autowired
    private PeriodService reportPeriodService;
    @Autowired
    private EventLauncher eventHandlerLauncher;
	@Autowired
	private SignService signService;
	@Autowired
	private ConfigurationDao configurationDao;
	@Autowired
	private ApplicationContext applicationContext;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    SourceService sourceService;
    @Autowired
    TransactionHelper tx;
    @Autowired
    TAUserService userService;

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
    public void createManualFormData(Logger logger, TAUserInfo userInfo, Long formDataId) {
        FormData formData = formDataDao.get(formDataId, false);
        formDataAccessService.canCreateManual(logger, userInfo, formDataId);

        List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null, null);
        formData.setManual(true);
        dataRowDao.saveRows(formData, rows);
        dataRowDao.commit(formData.getId());

        logger.info("Для налоговой формы успешно создана версия ручного ввода");
    }

    @Override
    public void importFormData(Logger logger, TAUserInfo userInfo, long formDataId, InputStream inputStream, String fileName, FormDataEvent formDataEvent) {
        loadFormData(logger, userInfo, formDataId, inputStream, fileName, formDataEvent);
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

        formDataAccessService.canEdit(userInfo, formDataId, false);

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

                String pKeyFileUrl = null;
                List<String> paramList = configurationDao.getAll().get(ConfigurationParam.KEY_FILE, 0);
                if (paramList != null && !paramList.isEmpty()) {
                    pKeyFileUrl =  paramList.get(0); // TODO Ключи нужно искать в нескольких каталогах
                }

                if (pKeyFileUrl != null) { // Необходимо проверить подпись
                    InputStream pKeyFileInputStream = null;

                    pKeyFile = File.createTempFile("signature", ".sign");
                    OutputStream pKeyFileOutputStream = new BufferedOutputStream(new FileOutputStream(pKeyFile));
                    try {
                        pKeyFileInputStream = new BufferedInputStream(ResourceUtils.getSharedResourceAsStream(pKeyFileUrl));
                        IOUtils.copy(pKeyFileInputStream, pKeyFileOutputStream);
                    } catch (Exception e) {
                        throw new ServiceException("Ошибка доступа к файлу базы открытых ключей", e);
                    } finally {
                        IOUtils.closeQuietly(pKeyFileOutputStream);
                        IOUtils.closeQuietly(pKeyFileInputStream);
                    }
                    if (!signService.checkSign(dataFile.getAbsolutePath(), pKeyFile.getAbsolutePath(), 0)) {
                        throw new ServiceException("Ошибка проверки цифровой подписи");
                    }
                }
            }

            FormData fd = formDataDao.get(formDataId, false);

            dataFileInputStream = new BufferedInputStream(new FileInputStream(dataFile));
            Map<String, Object> additionalParameters = new HashMap<String, Object>();
            additionalParameters.put("ImportInputStream", dataFileInputStream);
            additionalParameters.put("UploadFileName", fileName);
            formDataScriptingService.executeScript(userInfo, fd, formDataEvent, logger, additionalParameters);
            IOUtils.closeQuietly(dataFileInputStream);

            if (logger.containsLevel(LogLevel.ERROR)) {
                throw new ServiceLoggerException("Есть критические ошибки при выполнения скрипта",
                        logEntryService.save(logger.getEntries()));
            } else {
                logger.info("Данные загружены");
            }

            logBusinessService.add(formDataId, null, userInfo, formDataEvent, null);
            auditService.add(formDataEvent, userInfo, fd.getDepartmentId(), fd.getReportPeriodId(),
                    null, fd.getFormType().getName(), fd.getKind().getId(), fileName);
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
        formData.setManual(false);

        // Execute scripts for the form event CREATE
        ReportPeriod prevReportPeriod = reportPeriodService.getPrevReportPeriod(reportPeriodId);
        FormDataPerformer performer = null;
        if (prevReportPeriod != null) {
            FormData formDataOld;
            if (periodOrder == null)
                formDataOld = formDataDao.find(formTemplate.getType().getId(), kind, departmentId, prevReportPeriod.getId());
            else
                formDataOld = formDataDao.findMonth(formTemplate.getType().getId(), kind, departmentId, prevReportPeriod.getId(), periodOrder);
            if (formDataOld != null) {
                List<FormDataSigner> signer = new ArrayList<FormDataSigner>();
                List<FormDataSigner> signerOld = formDataOld.getSigners();
                for (FormDataSigner formDataSignerOld : signerOld) {
                    FormDataSigner formDataSigner = new FormDataSigner();
                    formDataSigner.setName(formDataSignerOld.getName());
                    formDataSigner.setPosition(formDataSignerOld.getPosition());
                    signer.add(formDataSigner);
                }
                formData.setSigners(signer);
                performer = formDataOld.getPerformer();
            }
        }
        if (performer == null) {
            performer = new FormDataPerformer();
            performer.setName(" ");
            performer.setPrintDepartmentId(departmentId);
            performer.setReportDepartmentName(departmentDao.getReportDepartmentName(departmentId));
        }
        formData.setPerformer(performer);

        // Execute scripts for the form event CREATE
		formDataScriptingService.executeScript(userInfo, formData,
                importFormData ? FormDataEvent.IMPORT : FormDataEvent.CREATE, logger, null);
		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Произошли ошибки в скрипте создания налоговой формы",
                    logEntryService.save(logger.getEntries()));
		}
		formDataDao.save(formData);

		logBusinessService.add(formData.getId(), null, userInfo, FormDataEvent.CREATE, null);
		auditService.add(FormDataEvent.CREATE, userInfo, formData.getDepartmentId(), formData.getReportPeriodId(),

				null, formData.getFormType().getName(), formData.getKind().getId(), null);
		// Заполняем начальные строки (но не сохраняем)
		dataRowDao.saveRows(formData, formTemplate.getRows());

		if (!importFormData) {
			// Execute scripts for the form event AFTER_CREATE
			formDataScriptingService.executeScript(userInfo, formData, FormDataEvent.AFTER_CREATE, logger, null);
			if (logger.containsLevel(LogLevel.ERROR)) {
				throw new ServiceLoggerException(
						"Произошли ошибки в скрипте после создания налоговой формы",
						logEntryService.save(logger.getEntries()));
			}
		}

		dataRowDao.commit(formData.getId());

        updatePreviousRowNumber(formData);
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

		formDataAccessService.canEdit(userInfo, formData.getId(), formData.isManual());

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

		formDataAccessService.canEdit(userInfo, formData.getId(), formData.isManual());
		
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

		formDataAccessService.canEdit(userInfo, formData.getId(), formData.isManual());

		formDataScriptingService.executeScript(userInfo, formData,
				FormDataEvent.CALCULATE, logger, null);

        String msg = updatePreviousRowNumber(formData);
        if (msg != null) {
            logger.info(msg);
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException("Найдены ошибки при выполнении расчета формы", logEntryService.save(logger.getEntries()));
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

		formDataAccessService.canEdit(userInfo, formData.getId(), formData.isManual());

		formDataScriptingService.executeScript(userInfo, formData,
                FormDataEvent.SAVE, logger, null);

		formDataDao.save(formData);
		
		dataRowDao.commit(formData.getId());

		logBusinessService.add(formData.getId(), null, userInfo, FormDataEvent.SAVE, null);
		auditService.add(FormDataEvent.SAVE, userInfo, formData.getDepartmentId(), formData.getReportPeriodId(),
				null, formData.getFormType().getName(), formData.getKind().getId(), null);

        String msg = updatePreviousRowNumber(formData);
        if (msg != null) {
            logger.info(msg);
        }

		return formData.getId();
	}

	/**
	 * Получить данные по налоговой форме
	 *
	 *
     * @param userInfo
     *            информация о пользователе, выполняющего операцию
     * @param formDataId
     *            идентификатор записи, которую необходимо считать
     * @param manual
     *@param logger
     *            логгер-объект для фиксации диагностических сообщений  @return объект с данными по налоговой форме
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException
	 *             если у пользователя нет прав просматривать налоговую форму с
	 *             такими параметрами
	 */
	@Override
	@Transactional
	public FormData getFormData(TAUserInfo userInfo, long formDataId, Boolean manual, Logger logger) {
		formDataAccessService.canRead(userInfo, formDataId);

		FormData formData = formDataDao.get(formDataId, manual);

		formDataScriptingService.executeScript(userInfo,
				formData, FormDataEvent.AFTER_LOAD, logger, null);

        updatePreviousRowNumber(formData);

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
	public void deleteFormData(Logger logger, TAUserInfo userInfo, long formDataId, boolean manual) {
		// Форма не должна быть заблокирована для редактирования другим пользователем
		lockCoreService.checkNoLockedAnother(FormData.class, formDataId, userInfo);

        if (manual) {
            formDataAccessService.canDeleteManual(logger, userInfo, formDataId);
            formDataDao.deleteManual(formDataId);
        } else {
            formDataAccessService.canDelete(userInfo, formDataId);

            FormData formData = formDataDao.get(formDataId, manual);
            auditService.add(FormDataEvent.DELETE, userInfo, formData.getDepartmentId(), formData.getReportPeriodId(),
                    null, formData.getFormType().getName(), formData.getKind().getId(), null);
            formDataDao.delete(formDataId);
        }
	}

    /**
     * Перемещает форму из одного состояния в другое.
     *
     * @param formDataId   идентификатор налоговой формы
     * @param userInfo     информация о текущем пользователе
     * @param workflowMove переход
     */
    @Override
    public void doMove(long formDataId, boolean manual, TAUserInfo userInfo, WorkflowMove workflowMove, String note, Logger logger) {
        List<FormData> formDataList = null;
        // Форма не должна быть заблокирована даже текущим пользователем;
        lockCoreService.checkUnlocked(FormData.class, formDataId, userInfo);
        // Временный срез формы должен быть в актуальном состоянии
        dataRowDao.rollback(formDataId);

        formDataAccessService.checkDestinations(formDataId);
        List<WorkflowMove> availableMoves = formDataAccessService.getAvailableMoves(userInfo, formDataId);
        if (!availableMoves.contains(workflowMove)) {
            throw new ServiceException(
                    "Переход \""
                            + workflowMove.getRoute()
                            + "\" из текущего состояния невозможен, или у пользователя " +
                            "не хватает полномочий для его осуществления");
        }

        FormData formData = formDataDao.get(formDataId, manual);

        formDataScriptingService.executeScript(userInfo, formData, workflowMove.getEvent(), logger, null);

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

        logger.info("Форма \"" + formData.getFormType().getName() + "\" переведена в статус \"" + workflowMove.getToState().getName() + "\"");

        logBusinessService.add(formData.getId(), null, userInfo, workflowMove.getEvent(), note);
        auditService.add(workflowMove.getEvent(), userInfo, formData.getDepartmentId(), formData.getReportPeriodId(),
                null, formData.getFormType().getName(), formData.getKind().getId(), note);

        if (workflowMove.getFromState() == WorkflowState.CREATED || workflowMove.getToState() == WorkflowState.CREATED) {
            String msg = updatePreviousRowNumber(formData);
            if (msg != null)
                logger.info(msg);
        }
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
        // Проверка перехода ЖЦ. Принятие либо отмена принятия. Прочие переходы не обрабатываются.
        if (workflowMove.getToState() != WorkflowState.ACCEPTED && workflowMove.getFromState() != WorkflowState.ACCEPTED) {
            return;
        }
        // Период ввода остатков не обрабатывается. Если форма ежемесячная, то только первый месяц периода может быть периодом ввода остатков.
        if ((formData.getPeriodOrder() == null || formData.getPeriodOrder() - 1 % 3 == 0) &&
                reportPeriodService.isBalancePeriod(formData.getReportPeriodId(), formData.getDepartmentId())) {
            return;
        }
        ReportPeriod reportPeriod = reportPeriodDao.get(formData.getReportPeriodId());
        // Список типов приемников для текущей формы
        List<DepartmentFormType> departmentFormTypes = departmentFormTypeDao.getFormDestinations(formData.getDepartmentId(), formData.getFormType().getId(), formData.getKind(), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
        // Если нет приемников, то обработка не требуется.
        if (departmentFormTypes == null || departmentFormTypes.isEmpty()) {
            return;
        }
        // Проверяем блокировку приемников
        List<FormData> lockedForms = new ArrayList<FormData>();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        for (DepartmentFormType destinationDFT : departmentFormTypes) {
            // Проверяем наличие активного шаблона приемника (встроенный в дао Exception)
            formTemplateService.getActiveFormTemplateId(destinationDFT.getFormTypeId(), formData.getReportPeriodId());
            // Экземпляр формы-приемника
            FormData destinationForm = findFormData(destinationDFT.getFormTypeId(), destinationDFT.getKind(), destinationDFT.getDepartmentId(), formData.getReportPeriodId(), formData.getPeriodOrder());
            // Если форма распринимается при отсутствии экземпляра формы-приемника, то такую форму не обрабатываем.
            if (destinationForm == null) {
                continue;
            }
            ObjectLock lock = lockCoreService.getLock(FormData.class, destinationForm.getId(), userInfo);
            if (lock != null) {
                lockedForms.add(destinationForm);
                logger.error("Невозможно принять налоговую форму и осуществить консолидацию " +
                        "из-за блокировки другими пользователями форм-приемников " +
                        "Форма " + destinationForm.getFormType().getName() + " " + destinationForm.getKind().getName() + ", " +
                        reportPeriodService.getReportPeriod(destinationForm.getReportPeriodId()).getName() + ", " +
                        departmentDao.getDepartment(destinationForm.getDepartmentId()).getName() + " заблокирована пользователем " +
                        userService.getUser(lock.getUserId()).getName() + " " + dateTimeFormat.format(lock.getLockTime())
                );

                continue;
            }
        }
        if (!lockedForms.isEmpty()) {
            throw new ServiceLoggerException("", logEntryService.save(logger.getEntries()));
        }
        try {
            // Проход по типам приемников
            for (DepartmentFormType destinationDFT : departmentFormTypes) {

                    // Экземпляр формы-приемника
                    FormData destinationForm = findFormData(destinationDFT.getFormTypeId(), destinationDFT.getKind(), destinationDFT.getDepartmentId(), formData.getReportPeriodId(), formData.getPeriodOrder());
                    // Если форма распринимается при отсутствии экземпляра формы-приемника, то такую форму не обрабатываем.
                    if (destinationForm == null && workflowMove.getFromState() == WorkflowState.ACCEPTED) {
                        continue;
                    }
                    // Список типов источников для текущего типа приемников
                    List<DepartmentFormType> sourceFormTypes = departmentFormTypeDao.getFormSources(destinationDFT.getDepartmentId(), destinationDFT.getFormTypeId(), destinationDFT.getKind(), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
                    // Признак наличия принятых экземпляров источников
                    boolean existAcceptedSources = false;
                    for (DepartmentFormType sourceDFT : sourceFormTypes) {
                        FormData sourceForm = findFormData(sourceDFT.getFormTypeId(), sourceDFT.getKind(), sourceDFT.getDepartmentId(), formData.getReportPeriodId(), formData.getPeriodOrder());
                        if (sourceForm != null && sourceForm.getState().equals(WorkflowState.ACCEPTED)) {
                            existAcceptedSources = true;
                            break;
                        }
                    }
                    // Если текущая форма-приемник имеет один или более источников в статусе «Принята» то консолидируем ее, иначе удаляем
                    if (existAcceptedSources) {
                        ScriptComponentContextImpl scriptComponentContext = new ScriptComponentContextImpl();
                        scriptComponentContext.setUserInfo(userInfo);
                        scriptComponentContext.setLogger(logger);
                        FormDataCompositionService formDataCompositionService = applicationContext.getBean(FormDataCompositionService.class);
                        ((ScriptComponentContextHolder) formDataCompositionService).setScriptComponentContext(scriptComponentContext);
                        Integer periodOrder = (destinationDFT.getKind() == FormDataKind.PRIMARY || destinationDFT.getKind() == FormDataKind.CONSOLIDATED) ? formData.getPeriodOrder() : null;
                        formDataCompositionService.compose(destinationForm, formData.getReportPeriodId(), periodOrder,
                                destinationDFT.getDepartmentId(), destinationDFT.getFormTypeId(), destinationDFT.getKind());
                    } else if (destinationForm != null) {
                        String formName = destinationForm.getFormType().getName();
                        String kindName = destinationForm.getKind().getName();
                        String departmentName = departmentDao.getDepartment(destinationForm.getDepartmentId()).getName();
                        deleteFormData(logger, userInfo, destinationForm.getId(), formData.isManual());
                        logger.info("%s: Расформирована налоговая форма-приемник: Подразделение: «%s», Тип: «%s», Вид: «%s».",
                                FormDataEvent.COMPOSE.getTitle(), departmentName, kindName, formName);
                    }
            }
        } finally {
            for (FormData lockedForm : lockedForms) {
                lockCoreService.unlock(FormData.class, lockedForm.getId(), userInfo);
            }
        }
    }

    @Override
    public FormData findFormData(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder) {
        if (periodOrder == null || kind != FormDataKind.PRIMARY && kind != FormDataKind.CONSOLIDATED) {
            // Если форма-источник квартальная или форма-приемник не является первичной или консолидированной, то ищем квартальный экземпляр
            return formDataDao.find(formTypeId, kind, departmentId, reportPeriodId);
        } else {
            // Если форма-источник ежемесячная и форма приемник является первичной или консолидированной, то ищем ежемесячный экземпляр
            Integer taxPeriodId = reportPeriodService.getReportPeriod(reportPeriodId).getTaxPeriod().getId();
            return formDataDao.findMonth(formTypeId, kind, departmentId, taxPeriodId, periodOrder);
        }
    }

    @Override
	@Transactional
	public void lock(long formDataId, TAUserInfo userInfo) {
		lockCoreService.lock(FormData.class, formDataId, userInfo);
		dataRowDao.rollback(formDataId);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void unlock(final long formDataId, final TAUserInfo userInfo) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                lockCoreService.unlock(FormData.class, formDataId, userInfo);
                dataRowDao.rollback(formDataId);
            }

            @Override
            public Object executeWithReturn() {
                return null;
            }
        });
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean unlockAllByUser(TAUserInfo userInfo) {
		//Это зло
		//lockDao.unlockAllObjectByUserId(userInfo.getUser().getId());
		return true;// TODO обработать возможные ошибки
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public ObjectLock<Long> getObjectLock(final long formDataId, final TAUserInfo userInfo) {
        return tx.returnInNewTransaction(new TransactionLogic<ObjectLock<Long>>() {
            @Override
            public ObjectLock<Long> executeWithReturn() {
                return lockCoreService.getLock(FormData.class, formDataId, userInfo);
            }

            @Override
            public void execute() {
            }
        });
	}

    public List<Long> getFormDataListInActualPeriodByTemplate(int templateId, Date startDate) {
        return formDataDao.getFormDataListInActualPeriodByTemplate(templateId, startDate);
    }

    @Override
    public boolean existManual(Long formDataId) {
        return formDataDao.existManual(formDataId);
    }

    @Override
    public boolean isBankSummaryForm(long formDataId) {
        //TODO
        return true;
    }

    @Override
    public List<FormData> find(List<Integer> departmentIds, int reportPeriodId) {
        return formDataDao.find(departmentIds, reportPeriodId);
    }

    @Override
    public boolean existFormData(int formTypeId, FormDataKind kind, int departmentId, Logger logger) {
        // Если для удаляемого назначения нет созданных экземпляров форм
        List<Long> formDataIds = formDataDao.getFormDataIds(formTypeId, kind, departmentId);
        if (logger != null) {
            for (long formDataId : formDataIds) {
                FormData formData = formDataDao.getWithoutRows(formDataId);
                ReportPeriod period = reportPeriodDao.get(formData.getReportPeriodId());

                logger.error(MSG_IS_EXIST_FORM,
                        formData.getFormType().getName(),
                        kind.getName(),
                        departmentDao.getDepartment(departmentId).getName(),
                        period.getName() + " " + period.getTaxPeriod().getYear());
            }
        }
        return !formDataIds.isEmpty();
    }


    @Override
    public boolean existFormDataByTaxAndDepartment(List<TaxType> taxTypes, List<Integer> departmentIds) {
       try {
           List<Long> ids = formDataDao.getFormDataIds(taxTypes, departmentIds);
           return !ids.isEmpty();
       } catch (DaoException e){
           throw new ServiceException("", e);
       }
    }

    @Override
    public void updateFDTBNames(int newDepTBId, int oldDepTBId, Date dateFrom, Date dateTo) {
        if (dateFrom == null)
            throw new ServiceException("Должна быть установлена хотя бы \"Дата от\"");
        try {
            Department departmentTBNew = departmentDao.getDepartment(newDepTBId);
            Department departmentTBOld = departmentDao.getDepartment(oldDepTBId);
            formDataDao.updateFDPerformerTBDepartmentNames(departmentTBNew.getName(), departmentTBOld.getName(), dateFrom, dateTo);
        } catch (ServiceException e){
            throw new ServiceException("Ошибка при обновлении имени ТБ", e);
        }
    }

    @Override
    public void updateFDDepartmentNames(int depTBId, String depName, Date dateFrom, Date dateTo) {
        if (dateFrom == null)
            throw new ServiceException("Должна быть установлена хотя бы \"Дата от\"");
        try {
            formDataDao.updateFDPerformerDepartmentNames(depTBId, depName, dateFrom, dateTo);
        } catch (ServiceException e){
            throw new ServiceException("Ошибка при обновлении имени ТБ", e);
        }
    }

    public Integer getPreviousRowNumber(FormData formData) {
        int previousRowNumber = 0;
        // Получить налоговый период
        ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(formData.getReportPeriodId());
        TaxPeriod taxPeriod = reportPeriod.getTaxPeriod();
        // Получить упорядоченный список экземпляров НФ, которые участвуют в сквозной нумерации и находятся до указанного экземпляра НФ
        List<FormData> formDataList = formDataDao.getPrevFormDataListForCrossNumeration(formData, taxPeriod.getYear(),
                String.valueOf(taxPeriod.getTaxType().getCode()));

        // Если экземпляр НФ является не первым экземпляром в сквозной нумерации
        if (formDataList.size() > 0) {
            for (FormData aFormData : formDataList) {
                if (aFormData.getState() != WorkflowState.CREATED) {
                    previousRowNumber += dataRowDao.getSizeWithoutTotal(aFormData, null);
                }
                if (aFormData.getId().equals(formData.getId())) {
                    return previousRowNumber;
                }
            }
        }

        return previousRowNumber;
    }

    @Override
    public String updatePreviousRowNumber(FormData formData) {
        String msg = null;

        FormTemplate formTemplate = formTemplateService.get(formData.getFormTemplateId());
        if (formTemplateService.isAnyAutoNumerationColumn(formTemplate, AutoNumerationColumnType.CROSS)) {
            // Получить налоговый период
            TaxPeriod taxPeriod = reportPeriodService.getReportPeriod(formData.getReportPeriodId()).getTaxPeriod();
            // Получить список экземпляров НФ следующих периодов
            List<FormData> formDataList = formDataDao.getNextFormDataListForCrossNumeration(formData, taxPeriod.getYear(),
                    String.valueOf(taxPeriod.getTaxType().getCode()));

            // Устанавливаем значение для текущего экземпляра НФ
            formDataDao.updatePreviousRowNumber(formData.getId(), getPreviousRowNumber(formData));

            StringBuilder stringBuilder = new StringBuilder();
            // Обновляем последующие периоды
            int size = formDataList.size();
            for (FormData data : formDataList) {
                // Для экземпляров в статусе "Создано" не обновляем
                if (data.getState() != WorkflowState.CREATED) {
                    formDataDao.updatePreviousRowNumber(data.getId(), getPreviousRowNumber(data));
                    ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(data.getReportPeriodId());
                    stringBuilder.append(reportPeriod.getName() + " " + reportPeriod.getTaxPeriod().getYear());
                    // TODO - разобраться с запятой!
                    if (--size  > 0) {
                        stringBuilder.append(", ");
                    }
                    msg = "Сквозная нумерация обновлена в налоговых формах следующих периодов текущей сквозной нумерации: " +
                            stringBuilder.toString();
                }
            }
        }
        return msg;
    }

    @Override
    public List<FormData> getManualInputForms(List<Integer> departments, int reportPeriodId, TaxType taxType, FormDataKind kind) {
        ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
        return formDataDao.getManualInputForms(departments, reportPeriodId, taxType, kind, reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
    }
}
