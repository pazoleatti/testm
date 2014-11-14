package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormPerformerDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.*;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.impl.eventhandler.EventLauncher;
import com.aplana.sbrf.taxaccounting.service.shared.FormDataCompositionService;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
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
    private static final String XLSM_EXT = "xlsm";
    public static final String MSG_IS_EXIST_FORM = "Существует экземпляр налоговой формы \"%s\" типа \"%s\" в подразделении \"%s\" в периоде \"%s\"";
    final static String LOCK_MESSAGE = "Форма заблокирована и не может быть изменена. Попробуйте выполнить операцию позже.";
    final static String LOCK_REFBOOK_MESSAGE = "Справочник \"%s\" заблокирован и не может быть использован для заполнения атрибутов формы. Попробуйте выполнить операцию позже.";
    final static String REF_BOOK_RECORDS_ERROR =  "Строка %s, атрибут \"%s\": период актуальности значения не пересекается с отчетным периодом формы";
    final static String DEPARTMENT_REPORT_PERIOD_NOT_FOUND_ERROR = "Не найден отчетный период подразделения с id = %d.";
    private static final String SAVE_ERROR = "Найдены ошибки при сохранении формы!";
    private static final String SORT_ERROR = "Найдены ошибки при сортировке строк формы!";

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
    private TransactionHelper tx;
    @Autowired
    private TAUserService userService;
    @Autowired
    private LockDataService lockService;
    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private ReportService reportService;
    @Autowired
    private FormPerformerDao formPerformerDao;
    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;
    @Autowired
    private IfrsDataService ifrsDataService;

    @Override
    public long createFormData(Logger logger, TAUserInfo userInfo, int formTemplateId, int departmentReportPeriodId, FormDataKind kind, Integer periodOrder) {
        formDataAccessService.canCreate(userInfo, formTemplateId, kind, departmentReportPeriodId);
        return createFormDataWithoutCheck(logger, userInfo, formTemplateId, departmentReportPeriodId, kind, periodOrder, false);
    }

    @Override
    public void createManualFormData(Logger logger, TAUserInfo userInfo, Long formDataId) {
        FormData formData = formDataDao.get(formDataId, false);
        formDataAccessService.canCreateManual(logger, userInfo, formDataId);

        List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);
        formData.setManual(true);
        dataRowDao.saveRows(formData, rows);
        dataRowDao.commit(formData.getId());

        logger.info("Для налоговой формы успешно создана версия ручного ввода");
    }

    @Override
    public void importFormData(Logger logger, TAUserInfo userInfo, long formDataId, boolean isManual, InputStream inputStream, String fileName, FormDataEvent formDataEvent) {
        loadFormData(logger, userInfo, formDataId, isManual, inputStream, fileName, formDataEvent);
    }

    @Override
    public void importFormData(Logger logger, TAUserInfo userInfo, long formDataId, boolean isManual, InputStream inputStream, String fileName) {
        loadFormData(logger, userInfo, formDataId, isManual, inputStream, fileName, FormDataEvent.IMPORT);
        if (lockService.isLockExists(LockData.LockObjects.FORM_DATA_IMPORT.name() + "_" + formDataId + "_" + isManual)) {
            lockService.unlock(LockData.LockObjects.FORM_DATA_IMPORT.name() + "_" + formDataId + "_" + isManual, userInfo.getUser().getId());
        } else {
            //Если блокировка уже не существует, значит загружаемые данные не актуальны - откатываем их
            //Т.к она снимается только при закрытии страницы, то этот эксепшен все равно никто не увидит
            throw new ServiceException("Загружаемые данные уже не актуальны. Изменения были отменены.");
        }
    }

    @Override
    @Transactional
    public void migrationFormData(Logger logger, TAUserInfo userInfo, long formDataId, InputStream inputStream, String fileName) {
        loadFormData(logger, userInfo, formDataId, false, inputStream, fileName, FormDataEvent.MIGRATION);
    }

    private void loadFormData(Logger logger, TAUserInfo userInfo, long formDataId, boolean isManual, InputStream inputStream, String fileName, FormDataEvent formDataEvent) {
		// Поскольку импорт используется как часть редактирования НФ, т.е. иморт только строк (форма уже существует) то все проверки должны 
    	// соответствовать редактированию (добавление, удаление, пересчет)
    	// Форма должна быть заблокирована текущим пользователем для редактирования
        checkLockedMe(lockService.getLock(LockData.LockObjects.FORM_DATA.name() + "_" + formDataId), userInfo.getUser());

        formDataAccessService.canEdit(userInfo, formDataId, isManual);

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

            // Проверка ЭЦП
            // Если флаг проверки отсутствует или не равен «1», то файл считается проверенным
            boolean check = false;
            // исключить проверку ЭЦП для файлов эксель
            if (!ext.equals(XLS_EXT) && !ext.equals(XLSX_EXT) && !ext.equals(XLSM_EXT)) {
                List<String> signList = configurationDao.getByDepartment(0).get(ConfigurationParam.SIGN_CHECK, 0);
                if (signList != null && !signList.isEmpty() && signList.get(0).equals("1")) {
                    List<String> paramList = configurationDao.getAll().get(ConfigurationParam.KEY_FILE, 0);
                    if (paramList != null) { // Необходимо проверить подпись
                        try {
                            check = signService.checkSign(dataFile.getAbsolutePath(), 0);
                        } catch (Exception e) {
                            logger.error("Ошибка при проверке ЭЦП: " + e.getMessage());
                        }
                        if (!check) {
                            logger.error("Ошибка проверки цифровой подписи");
                        }
                    }
                } else {
                    check = true;
                }
            } else {
                check = true;
            }

            FormData fd = formDataDao.get(formDataId, false);

            if (check) {
                dataFileInputStream = new BufferedInputStream(new FileInputStream(dataFile));
                Map<String, Object> additionalParameters = new HashMap<String, Object>();
                additionalParameters.put("ImportInputStream", dataFileInputStream);
                additionalParameters.put("UploadFileName", fileName);
                formDataScriptingService.executeScript(userInfo, fd, formDataEvent, logger, additionalParameters);
                IOUtils.closeQuietly(dataFileInputStream);
            }

            if (logger.containsLevel(LogLevel.ERROR)) {
                throw new ServiceLoggerException("Есть критические ошибки при выполнения скрипта",
                        logEntryService.save(logger.getEntries()));
            } else {
                logger.info("Данные загружены");
            }

            logBusinessService.add(formDataId, null, userInfo, formDataEvent, null);
            auditService.add(formDataEvent, userInfo, fd.getDepartmentId(), fd.getReportPeriodId(),
                    null, fd.getFormType().getName(), fd.getKind().getId(), fileName, null, fd.getFormType().getId());
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
	public long createFormDataWithoutCheck(Logger logger, TAUserInfo userInfo, int formTemplateId, int departmentReportPeriodId,
                                           FormDataKind kind, Integer periodOrder, boolean importFormData) {
		FormTemplate formTemplate = formTemplateService.getFullFormTemplate(formTemplateId);
        FormData formData = new FormData(formTemplate);

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(departmentReportPeriodId);
        if (departmentReportPeriod == null) {
            throw new ServiceException(DEPARTMENT_REPORT_PERIOD_NOT_FOUND_ERROR, departmentReportPeriodId);
        }

        formData.setState(WorkflowState.CREATED);
        formData.setReportPeriodId(departmentReportPeriod.getReportPeriod().getId());
        formData.setDepartmentReportPeriodId(departmentReportPeriodId);
        formData.setDepartmentId(departmentReportPeriod.getDepartmentId());
        formData.setKind(kind);
        formData.setPeriodOrder(periodOrder);
        formData.setManual(false);

        FormDataPerformer performer = null;
        FormData formDataOld = getPrevPeriodFormData(formTemplate, departmentReportPeriod, kind, periodOrder);
        if (formDataOld != null) {
            FormData fdOld = formDataDao.get(formDataOld.getId(), false);
            List<FormDataSigner> signerOld = fdOld.getSigners();
            List<FormDataSigner> signer = new ArrayList<FormDataSigner>();
            for (FormDataSigner formDataSignerOld : signerOld) {
                FormDataSigner formDataSigner = new FormDataSigner();
                formDataSigner.setName(formDataSignerOld.getName());
                formDataSigner.setPosition(formDataSignerOld.getPosition());
                formDataSigner.setOrd(formDataSignerOld.getOrd());
                signer.add(formDataSigner);
            }
            formData.setSigners(signer);
            performer = fdOld.getPerformer();
        }
        if (performer == null) {
            performer = new FormDataPerformer();
            performer.setName(" ");
            performer.setPrintDepartmentId(departmentReportPeriod.getDepartmentId());
            performer.setReportDepartmentName(departmentDao.getReportDepartmentName(departmentReportPeriod.getDepartmentId()));
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
				null, formData.getFormType().getName(), formData.getKind().getId(), "Форма создана", null, formData.getFormType().getId());

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
     * Получение налоговой формы из предыдущего отчетного периода (для ежемесячных форм поиск ведется в текущем периоде, если это не первый месяц периода)
     * @param formTemplate
     * @param departmentReportPeriod
     * @param kind
     * @param periodOrder
     * @return
     */
    public FormData getPrevPeriodFormData(FormTemplate formTemplate, DepartmentReportPeriod departmentReportPeriod, FormDataKind kind, Integer periodOrder) {
        FormData formDataOld = null;
        boolean isNotThisReportPeriod = false;
        if (periodOrder != null) {
            List<Months> availableMonthList = reportPeriodService.getAvailableMonthList(departmentReportPeriod.getReportPeriod().getId());
            if  (periodOrder > 1 && availableMonthList.contains(Months.fromId(periodOrder - 2))) {
                isNotThisReportPeriod = true;
                formDataOld = formDataDao.find(formTemplate.getType().getId(), kind, departmentReportPeriod.getId().intValue(), Integer.valueOf(periodOrder - 1));
            }
        }
        ReportPeriod prevReportPeriod = reportPeriodService.getPrevReportPeriod(departmentReportPeriod.getReportPeriod().getId());
        if (!isNotThisReportPeriod && prevReportPeriod != null) {
            Integer lastPeriodOrder = null;
            if (periodOrder != null) {
                List<Months> availableMonthList = reportPeriodService.getAvailableMonthList(prevReportPeriod.getId());
                lastPeriodOrder = availableMonthList.get(availableMonthList.size() - 1).getId();
            }
            DepartmentReportPeriod departmentReportPeriodOld = departmentReportPeriodDao.getLast(departmentReportPeriod.getDepartmentId(), prevReportPeriod.getId().intValue());
            formDataOld = formDataDao.find(formTemplate.getType().getId(), kind, departmentReportPeriodOld.getId().intValue(), lastPeriodOrder);
        }
        return formDataOld;
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
        checkLockedMe(lockService.getLock(LockData.LockObjects.FORM_DATA.name() + "_" + formData.getId()), userInfo.getUser());

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
        checkLockedMe(lockService.getLock(LockData.LockObjects.FORM_DATA.name() + "_" + formData.getId()), userInfo.getUser());

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
        checkLockedMe(lockService.getLock(LockData.LockObjects.FORM_DATA.name() + "_" + formData.getId()), userInfo.getUser());

		formDataAccessService.canEdit(userInfo, formData.getId(), formData.isManual());

		formDataScriptingService.executeScript(userInfo, formData,
                FormDataEvent.CALCULATE, logger, null);

        if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException("Найдены ошибки при выполнении расчета формы", logEntryService.save(logger.getEntries()));
		} else {
			logger.info("Расчет завершен, фатальных ошибок не обнаружено");
		}
	}

	@Override
	public void doCheck(Logger logger, TAUserInfo userInfo, FormData formData) {
		// Форма не должна быть заблокирована для редактирования другим пользователем
		checkLockAnotherUser(lockService.getLock(LockData.LockObjects.FORM_DATA.name() + "_" + formData.getId()), logger, userInfo.getUser());
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
        checkLockedMe(lockService.getLock(LockData.LockObjects.FORM_DATA.name() + "_" + formData.getId()), userInfo.getUser());

		formDataAccessService.canEdit(userInfo, formData.getId(), formData.isManual());

        // Отработка скриптом события сохранения
		formDataScriptingService.executeScript(userInfo, formData,
                FormDataEvent.SAVE, logger, null);

        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(SAVE_ERROR, logEntryService.save(logger.getEntries()));
        }

        // Отработка скриптом события сортировки
        formDataScriptingService.executeScript(userInfo, formData,
                FormDataEvent.SORT_ROWS, logger, null);

        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(SORT_ERROR, logEntryService.save(logger.getEntries()));
        }

        // Обновление для сквозной нумерации
        updatePreviousRowNumberAttr(formData, logger);

        formDataDao.save(formData);

		dataRowDao.commit(formData.getId());

        deleteReport(formData.getId(), formData.isManual());

        // ЖА и история изменений
		logBusinessService.add(formData.getId(), null, userInfo, FormDataEvent.SAVE, null);
		auditService.add(FormDataEvent.SAVE, userInfo, formData.getDepartmentId(), formData.getReportPeriodId(),
				null, formData.getFormType().getName(), formData.getKind().getId(), "Форма сохранена", null, formData.getFormType().getId());

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
	public FormData getFormData(TAUserInfo userInfo, long formDataId, boolean manual, Logger logger) {
		formDataAccessService.canRead(userInfo, formDataId);

		FormData formData = formDataDao.get(formDataId, manual);

		formDataScriptingService.executeScript(userInfo,
				formData, FormDataEvent.AFTER_LOAD, logger, null);

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
        checkLockAnotherUser(lockService.getLock(LockData.LockObjects.FORM_DATA.name() + "_" + formDataId),
                logger,  userInfo.getUser());

        if (manual) {
            formDataAccessService.canDeleteManual(logger, userInfo, formDataId);
            formDataDao.deleteManual(formDataId);
        } else {
            formDataAccessService.canDelete(userInfo, formDataId);

            FormData formData = formDataDao.get(formDataId, false);
            auditService.add(FormDataEvent.DELETE, userInfo, formData.getDepartmentId(), formData.getReportPeriodId(),
                    null, formData.getFormType().getName(), formData.getKind().getId(), "Форма удалена", null, formData.getFormType().getId());
            formDataDao.delete(formDataId);
            deleteReport(formDataId, null);
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
        // Форма не должна быть заблокирована даже текущим пользователем
        String lockKey = LockData.LockObjects.FORM_DATA.name() + "_" + formDataId;
        checkLockAnotherUser(lockService.getLock(lockKey), logger,  userInfo.getUser());
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

        if (workflowMove == WorkflowMove.CREATED_TO_PREPARED
                || workflowMove == WorkflowMove.PREPARED_TO_APPROVED
                || workflowMove == WorkflowMove.APPROVED_TO_ACCEPTED
                || workflowMove == WorkflowMove.PREPARED_TO_ACCEPTED) {
            //Устанавливаем блокировку на текущую нф
            List<String> lockedObjects = new ArrayList<String>();
            int userId = userInfo.getUser().getId();
            LockData lockData = lockService.lock(lockKey, userId, LockData.STANDARD_LIFE_TIME);
            checkLockAnotherUser(lockData, logger,  userInfo.getUser());
            if (lockData == null) {
                try {
                    //Блокировка установлена
                    lockedObjects.add(lockKey);
                    //Блокируем связанные справочники
                    for (Column column : formData.getFormColumns()) {
                        if (ColumnType.REFBOOK.equals(column.getColumnType())) {
                            Long attributeId = ((RefBookColumn) column).getRefBookAttributeId();
                            if (attributeId != null) {
                                RefBook refBook = refBookDao.getByAttribute(attributeId);
                                String referenceLockKey = LockData.LockObjects.REF_BOOK.name() + "_" + refBook.getId();
                                if (!lockedObjects.contains(referenceLockKey)) {
                                    LockData referenceLockData = lockService.lock(referenceLockKey, userId, LockData.STANDARD_LIFE_TIME);
                                    if (referenceLockData == null) {
                                        //Блокировка установлена
                                        lockedObjects.add(referenceLockKey);
                                    } else {
                                        throw new ServiceLoggerException(String.format(LOCK_REFBOOK_MESSAGE, refBook.getName()),
                                                logEntryService.save(logger.getEntries()));
                                    }
                                }
                            }
                        }
                    }
                    //Проверяем что записи справочников, на которые есть ссылки в нф все еще существуют в периоде формы
                    checkReferenceValues(logger, formData);
                    //Делаем переход
                    moveProcess(formData, userInfo, workflowMove, note, logger);
                } finally {
                    for (String lock : lockedObjects) {
                        lockService.unlock(lock, userId);
                    }
                }
            } else {
                throw new ServiceLoggerException(LOCK_MESSAGE,
                        logEntryService.save(logger.getEntries()));
            }
        } else {
            moveProcess(formData, userInfo, workflowMove, note, logger);
        }
    }

    private final class ReferenceInfo {
        private int rownum;
        private String columnName;

        private ReferenceInfo(int rownum, String columnName) {
            this.rownum = rownum;
            this.columnName = columnName;
        }

        private int getRownum() {
            return rownum;
        }

        private String getColumnName() {
            return columnName;
        }
    }

    @Override
    public void checkReferenceValues(Logger logger, FormData formData) {
        Map<Long, List<Long>> recordsToCheck = new HashMap<Long, List<Long>>();
        Map<Long, ReferenceInfo> referenceInfoMap = new HashMap<Long, ReferenceInfo>();
        List<DataRow<Cell>> rows = dataRowDao.getSavedRows(formData, null);
        for (Column column : formData.getFormColumns()) {
            if (ColumnType.REFBOOK.equals(column.getColumnType())) {
                Long attributeId = ((RefBookColumn) column).getRefBookAttributeId();
                if (attributeId != null) {
                    RefBook refBook = refBookDao.getByAttribute(attributeId);
                    for (DataRow<Cell> row : rows) {
                        if (row.getCell(column.getAlias()).getNumericValue() != null) {
                            if (!recordsToCheck.containsKey(refBook.getId())) {
                                recordsToCheck.put(refBook.getId(), new ArrayList<Long>());
                            }
                            //Раскладываем значения ссылок по справочникам, на которые они ссылаются
                            recordsToCheck.get(refBook.getId()).add(row.getCell(column.getAlias()).getNumericValue().longValue());

                            //Сохраняем информацию о местоположении ссылки
                            referenceInfoMap.put(row.getCell(column.getAlias()).getNumericValue().longValue(),
                                    new ReferenceInfo(row.getIndex(), column.getName()));
                        }
                    }
                }
            }
        }

        ReportPeriod reportPeriod = reportPeriodDao.get(formData.getReportPeriodId());
        boolean error = false;
        for (Map.Entry<Long, List<Long>> referencesToCheck : recordsToCheck.entrySet()) {
            RefBookDataProvider provider = refBookFactory.getDataProvider(referencesToCheck.getKey());
            List<Long> inactiveRecords = provider.getInactiveRecordsInPeriod(referencesToCheck.getValue(), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
            if (!inactiveRecords.isEmpty()) {
                for (Long inactiveRecord : inactiveRecords) {
                    ReferenceInfo referenceInfo = referenceInfoMap.get(inactiveRecord);
                    logger.error(String.format(REF_BOOK_RECORDS_ERROR, referenceInfo.getRownum(), referenceInfo.getColumnName()));
                }
                error = true;
            }
        }

        if (error) {
            throw new ServiceLoggerException("Произошла ошибка при проверке справочных значений формы",
                    logEntryService.save(logger.getEntries()));
        }
    }

    private void moveProcess(FormData formData, TAUserInfo userInfo, WorkflowMove workflowMove, String note, Logger logger) {
        formDataScriptingService.executeScript(userInfo, formData, workflowMove.getEvent(), logger, null);

        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(
                    "Произошли ошибки в скрипте, который выполняется перед переходом",
                    logEntryService.save(logger.getEntries()));
        }

        if (formData.getFormType().getIsIfrs() && workflowMove.getFromState().equals(WorkflowState.ACCEPTED) &&
                departmentReportPeriodDao.get(formData.getDepartmentReportPeriodId()).getCorrectionDate() == null) {
            IfrsData ifrsData = ifrsDataService.get(formData.getReportPeriodId());
            if (ifrsData != null && ifrsData.getBlobDataId() != null) {
                ifrsDataService.deleteReport(formData, userInfo);
            } else if (lockService.getLock(ifrsDataService.generateTaskKey(formData.getReportPeriodId())) != null) {
                ifrsDataService.cancelTask(formData, userInfo);
            }
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

        //Считаем что при наличие версии ручного ввода движение о жц невозможно
        deleteReport(formData.getId(), null);

        logBusinessService.add(formData.getId(), null, userInfo, workflowMove.getEvent(), note);
        auditService.add(workflowMove.getEvent(), userInfo, formData.getDepartmentId(), formData.getReportPeriodId(),
                null, formData.getFormType().getName(), formData.getKind().getId(), workflowMove.getEvent().getTitle(), null, formData.getFormType().getId());

        updatePreviousRowNumberAttr(formData, workflowMove, logger);
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
    void compose(WorkflowMove workflowMove, FormData formData, TAUserInfo userInfo, Logger logger) {
        // Проверка перехода ЖЦ. Принятие либо отмена принятия. Прочие переходы не обрабатываются.
        if (workflowMove.getToState() != WorkflowState.ACCEPTED && workflowMove.getFromState() != WorkflowState.ACCEPTED) {
            return;
        }
        // Период ввода остатков не обрабатывается. Если форма ежемесячная, то только первый месяц периода может быть периодом ввода остатков.
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(formData.getDepartmentReportPeriodId());

        if ((formData.getPeriodOrder() == null || formData.getPeriodOrder() - 1 % 3 == 0) && departmentReportPeriod.isBalance()) {
            return;
        }
        ReportPeriod reportPeriod = reportPeriodDao.get(formData.getReportPeriodId());
        // Список типов приемников для текущей формы
        List<DepartmentFormType> departmentFormTypes = departmentFormTypeDao.getFormDestinations(formData.getDepartmentId(), formData.getFormType().getId(), formData.getKind(), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
        // Если нет приемников, то обработка не требуется.
        if (departmentFormTypes == null || departmentFormTypes.isEmpty()) {
            return;
        }

       List<String> lockedForms = new ArrayList<String>();

        try {
            // Проверяем блокировку приемников
            List<String> errorsList = new ArrayList<String>();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd.MM.yyyy");
            for (DepartmentFormType destinationDFT : departmentFormTypes) {
                String periodOrder = ((destinationDFT.getKind() == FormDataKind.PRIMARY || destinationDFT.getKind() == FormDataKind.CONSOLIDATED) && formData.getPeriodOrder() != null) ?
                        String.valueOf(formData.getPeriodOrder()) : "";
                String lockKey = formData.getReportPeriodId() + "." + periodOrder + "." + destinationDFT.getDepartmentId()
                        + "." + destinationDFT.getFormTypeId() + "." + destinationDFT.getKind();

                LockData lockData = lockService.lock(lockKey, userInfo.getUser().getId(), LockData.STANDARD_LIFE_TIME * 3);
                if (lockData != null) {
                    FormTemplate formTemplate = formTemplateService.get(formTemplateService.getActiveFormTemplateId(destinationDFT.getFormTypeId(), formData.getReportPeriodId()));
                    errorsList.add(String.format("«%s» %s, %s, «%s» заблокирована пользователем %s, %s",
                                    formTemplate.getName(), destinationDFT.getKind().getName(),
                                    reportPeriod.getTaxPeriod().getYear()+" "+reportPeriod.getName(),
                                    departmentDao.getDepartment(destinationDFT.getDepartmentId()).getName(),
                                    userService.getUser(lockData.getUserId()).getName(), sdf.format(lockData.getDateBefore())));
                } else {
                    lockedForms.add(lockKey);
                }
            }
            if (!errorsList.isEmpty()) {
                logger.error("Невозможно принять налоговую форму и осуществить консолидацию из-за блокировки другими пользователями форм-приемников:");
                for(String error : errorsList){
                    logger.error(error);
                }
                throw new ServiceLoggerException("Ошибка при консолидации", logEntryService.save(logger.getEntries()));
            }

            // Проход по типам приемников
            for (DepartmentFormType destinationDFT : departmentFormTypes) {
                // Последний отчетный период подразделения
                DepartmentReportPeriod destinationDepartmentReportPeriod =
                        departmentReportPeriodDao.getLast(destinationDFT.getDepartmentId(), formData.getReportPeriodId());

                if (destinationDepartmentReportPeriod == null) {
                    continue;
                }

                // Экземпляр формы-приемника
                FormData destinationForm = findFormData(destinationDFT.getFormTypeId(), destinationDFT.getKind(),
                        destinationDepartmentReportPeriod.getId(), formData.getPeriodOrder());
                // Если форма распринимается при отсутствии экземпляра формы-приемника, то такую форму не обрабатываем.
                if (destinationForm == null && workflowMove.getFromState() == WorkflowState.ACCEPTED) {
                    continue;
                }
                // Список типов источников для текущего типа приемников
                List<DepartmentFormType> sourceFormTypes = departmentFormTypeDao.getFormSources(
                        destinationDFT.getDepartmentId(), destinationDFT.getFormTypeId(), destinationDFT.getKind(),
                        reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
                // Признак наличия принятых экземпляров источников
                boolean existAcceptedSources = false;
                for (DepartmentFormType sourceDFT : sourceFormTypes) {
                    FormData sourceForm = getLast(sourceDFT.getFormTypeId(), sourceDFT.getKind(),
                            sourceDFT.getDepartmentId(), formData.getReportPeriodId(), formData.getPeriodOrder());
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
                    formDataCompositionService.compose(destinationForm, destinationDepartmentReportPeriod.getId(),
                            periodOrder, destinationDFT.getFormTypeId(), destinationDFT.getKind());
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
            for (String lockKey : lockedForms) {
                lockService.unlock(lockKey, userInfo.getUser().getId());
            }
        }
    }

    @Override
    public FormData findFormData(int formTypeId, FormDataKind kind, int departmentReportPeriodId, Integer periodOrder) {
        if (periodOrder == null || kind != FormDataKind.PRIMARY && kind != FormDataKind.CONSOLIDATED) {
            // Если форма-источник квартальная или форма-приемник не является первичной или консолидированной, то ищем квартальный экземпляр
            periodOrder = null;
        }
        return formDataDao.find(formTypeId, kind, departmentReportPeriodId, periodOrder);
    }

    @Override
	@Transactional
	public void lock(long formDataId, TAUserInfo userInfo) {
        checkLockAnotherUser(lockService.lock(LockData.LockObjects.FORM_DATA.name() + "_" + formDataId,
                userInfo.getUser().getId(), LockData.STANDARD_LIFE_TIME), null,  userInfo.getUser());
		dataRowDao.rollback(formDataId);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void unlock(final long formDataId, final TAUserInfo userInfo) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                lockService.unlock(LockData.LockObjects.FORM_DATA.name() + "_" + formDataId, userInfo.getUser().getId());
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
	public LockData getObjectLock(final long formDataId, final TAUserInfo userInfo) {
        return tx.returnInNewTransaction(new TransactionLogic<LockData>() {
            @Override
            public LockData executeWithReturn() {
                return lockService.getLock(LockData.LockObjects.FORM_DATA.name() + "_" + formDataId);
            }

            @Override
            public void execute() {
            }
        });
	}

    @Override
    public List<Long> getFormDataListInActualPeriodByTemplate(int templateId, Date startDate) {
        return formDataDao.getFormDataListInActualPeriodByTemplate(templateId, startDate);
    }

    @Override
    public boolean existManual(Long formDataId) {
        return formDataDao.existManual(formDataId);
    }

    @Override
    public List<FormData> find(List<Integer> departmentIds, int reportPeriodId) {
        return formDataDao.find(departmentIds, reportPeriodId);
    }

    @Override
    public List<FormData> getIfrsForm(int reportPeriodId) {
        return formDataDao.getIfrsForm(reportPeriodId);
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
    public void updateFDTBNames(int depTBId,  String depName, Date dateFrom, Date dateTo, boolean isChangeTB) {
        if (dateFrom == null)
            throw new ServiceException("Должна быть установлена хотя бы \"Дата от\"");
        try {
            List<Long> formDataIds = formPerformerDao.getFormDataId(depTBId, dateFrom, dateTo);
            for(Long formDataId: formDataIds)
                deleteReport(formDataId, null);
            formDataDao.updateFDPerformerTBDepartmentNames(depTBId, depName, dateFrom, dateTo, isChangeTB);
        } catch (ServiceException e){
            throw new ServiceException("Ошибка при обновлении имени ТБ", e);
        }
    }

    @Override
    public void updateFDDepartmentNames(int depTBId, String depName, Date dateFrom, Date dateTo) {
        if (dateFrom == null)
            throw new ServiceException("Должна быть установлена хотя бы \"Дата от\"");
        try {
            List<Long> formDataIds = formPerformerDao.getFormDataId(depTBId, dateFrom, dateTo);
            for(Long formDataId: formDataIds)
                deleteReport(formDataId, null);
            formDataDao.updateFDPerformerDepartmentNames(depTBId, depName, dateFrom, dateTo);
        } catch (ServiceException e){
            throw new ServiceException("Ошибка при обновлении имени ТБ", e);
        }
    }

    @Override
    public Integer getPreviousRowNumber(FormData formData) {
        int previousRowNumber = 0;
        // Отчетный период подразделения
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(formData.getDepartmentReportPeriodId());
        // Налоговый период
        TaxPeriod taxPeriod = departmentReportPeriod.getReportPeriod().getTaxPeriod();
        // Получить упорядоченный список экземпляров НФ, которые участвуют в сквозной нумерации и находятся до указанного экземпляра НФ
        List<FormData> formDataList = formDataDao.getPrevFormDataList(formData, taxPeriod);

        // Если экземпляр НФ является не первым экземпляром в сквозной нумерации
        if (formDataList.size() > 0) {
            for (FormData aFormData : formDataList) {
                if (beInOnAutoNumeration(aFormData.getState(), departmentReportPeriod)) {
                    previousRowNumber += dataRowDao.getSizeWithoutTotal(aFormData);
                }
                if (aFormData.getId().equals(formData.getId())) {
                    return previousRowNumber;
                }
            }
        }

        return previousRowNumber;
    }

    /**
     * Обновление значений атрибута "Номер последней строки предыдущей НФ" при сохранении
     *
     * @param logger   логгер для регистрации ошибок
     * @param formData редактируемый экземпляр НФ
     */
    void updatePreviousRowNumberAttr(FormData formData, Logger logger) {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(formData.getDepartmentReportPeriodId());
        if (beInOnAutoNumeration(formData.getState(), departmentReportPeriod)
                && dataRowDao.isDataRowsCountChanged(formData.getId())) {
            updatePreviousRowNumber(formData, logger);
        }
    }

    /**
     * Обновление значений атрибута "Номер последней строки предыдущей НФ" при переходе между ЖЦ
     *
     * @param workflowMove переход по ЖЦ
     * @param logger       логгер для регистрации ошибок
     * @param formData     редактируемый экземпляр НФ
     */
    public void updatePreviousRowNumberAttr(FormData formData, WorkflowMove workflowMove, Logger logger) {
        if (canUpdatePreviousRowNumberWhenDoMove(workflowMove)) {
            updatePreviousRowNumber(formData, logger);
        }
    }

    @Override
    public void updatePreviousRowNumber(FormData formData) {
        updatePreviousRowNumber(formData, null);
    }

    @Override
    public void updatePreviousRowNumber(FormData formData, Logger logger) {
        FormTemplate formTemplate = formTemplateService.get(formData.getFormTemplateId());
        updatePreviousRowNumber(formData, formTemplate, logger);
    }

    @Override
    public void updatePreviousRowNumber(FormData formData, FormTemplate formTemplate, Logger logger) {
        String msg = null;

        if (formTemplateService.isAnyAutoNumerationColumn(formTemplate, NumerationType.CROSS)) {
            // Получить налоговый период
            TaxPeriod taxPeriod = reportPeriodService.getReportPeriod(formData.getReportPeriodId()).getTaxPeriod();
            // Получить список экземпляров НФ следующих периодов
            List<FormData> formDataList = formDataDao.getNextFormDataList(formData, taxPeriod);

            // Устанавливаем значение для текущего экземпляра НФ
            formDataDao.updatePreviousRowNumber(formData.getId(), getPreviousRowNumber(formData));

            StringBuilder stringBuilder = new StringBuilder();
            // Обновляем последующие периоды
            int size = formDataList.size();

            for (FormData data : formDataList) {
                formDataDao.updatePreviousRowNumber(data.getId(), getPreviousRowNumber(data));
                ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(data.getReportPeriodId());
                stringBuilder.append(reportPeriod.getName() + " " + reportPeriod.getTaxPeriod().getYear());
                if (--size > 0) {
                    stringBuilder.append(", ");
                }
                msg = "Сквозная нумерация обновлена в налоговых формах следующих периодов текущей сквозной нумерации: " +
                        stringBuilder.toString();
            }

            if (logger != null && msg != null) {
                logger.info(msg);
            }
        }
    }

    @Override
    public List<FormData> getManualInputForms(List<Integer> departments, int reportPeriodId, TaxType taxType, FormDataKind kind) {
        ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
        return formDataDao.getManualInputForms(departments, reportPeriodId, taxType, kind, reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
    }

    @Override
    public void batchUpdatePreviousNumberRow(FormTemplate formTemplate) {
        List<FormData> formDataList = formDataDao.getFormDataListByTemplateId(formTemplate.getId());
        for (FormData formData : formDataList) {
            updatePreviousRowNumber(formData, formTemplate, null);
        }
    }

    @Override
    public FormData getLast(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder) {
        return formDataDao.getLast(formTypeId, kind, departmentId, reportPeriodId, periodOrder);
    }

    @Override
    public FormData getPreviousFormDataCorrection(FormData formData, List<DepartmentReportPeriod> departmentReportPeriodList, DepartmentReportPeriod departmentReportPeriod) {
        // Предыдущий отчетный период
        DepartmentReportPeriod prevDepartmentReportPeriod = getPreviousDepartmentReportPeriod(departmentReportPeriodList,
                departmentReportPeriod);

        if (prevDepartmentReportPeriod == null) {
            // Если отчетный период не найден, то и экземпляра НФ нет
            return null;
        }

        // Экземпляр НФ в пред. отчетном периоде подразделения
        FormData prevFormData = findFormData(formData.getFormType().getId(), formData.getKind(),
                prevDepartmentReportPeriod.getId(), formData.getPeriodOrder());

        if (prevFormData != null && prevFormData.getState() == WorkflowState.ACCEPTED) {
            return prevFormData;
        }

        return getPreviousFormDataCorrection(formData, departmentReportPeriodList, prevDepartmentReportPeriod);
    }

    /**
     * Поиск предыдущего отчетного периода из списка
     */
    private DepartmentReportPeriod getPreviousDepartmentReportPeriod(List<DepartmentReportPeriod> departmentReportPeriodList,
                                                                     DepartmentReportPeriod departmentReportPeriod) {
        if (departmentReportPeriodList.size() < 2) {
            return null;
        }
        for (int i = 0; i < departmentReportPeriodList.size() - 1; i++) {
            if (departmentReportPeriodList.get(i + 1).getId().equals(departmentReportPeriod.getId())) {
                return departmentReportPeriodList.get(i);
            }
        }
        return null;
    }

    /**
     * Экземпляры в статусе "Создана" не участвуют в сквозной нумерации
     * @param formState Состояние НФ
     * @param departmentReportPeriod Отчетный период подразделения НФ
     * @return true - участвует, false - не участвует
     */
    public boolean beInOnAutoNumeration(WorkflowState formState, DepartmentReportPeriod departmentReportPeriod) {
        return formState != WorkflowState.CREATED && departmentReportPeriod.getCorrectionDate() == null;
    }

    /**
     * "Номер последней строки предыдущей НФ" обновляется для последующих экземпляров НФ текущей сквозной нумерации
     * только при переходах по ЖЦ:
     * 1. из состояния "Создана" в любое состояние
     * 2. из любого состояния в состояние "Создана"
     *
     * @param workflowMove переход по ЖЦ
     * @return true - может меняться, false - не может
     */
    public boolean canUpdatePreviousRowNumberWhenDoMove(WorkflowMove workflowMove) {
        return workflowMove.getFromState() == WorkflowState.CREATED || workflowMove.getToState() == WorkflowState.CREATED;
    }

    private void checkLockAnotherUser(LockData lockData, Logger logger, TAUser user){
        if (lockData != null && lockData.getUserId() != user.getId())
            throw new ServiceLoggerException(LOCK_MESSAGE,
                    logEntryService.save(logger.getEntries()));
    }

    private void checkLockedMe(LockData lockData, TAUser user){
        if (lockData.getUserId() != user.getId()) {
            throw new ServiceException("Объект не заблокирован текущим пользователем");
        }
    }

    @Override
    public void deleteReport(long formDataId, Boolean manual) {
        boolean[] b = {false, true};
        ReportType[] reportTypes = {ReportType.CSV, ReportType.EXCEL};
        for (ReportType reportType: reportTypes) {
            for (boolean showChecked : b) {
                for(boolean saved : b) {
                    if (manual != null) {
                        lockService.unlock(String.format("%s_%s_%s_isShowChecked_%s_manual_%s_saved_%s", LockData.LockObjects.FORM_DATA.name(), formDataId, reportType.getName(), showChecked, manual, saved), 0, true);
                    } else {
                        for(boolean all: b) {
                            lockService.unlock(String.format("%s_%s_%s_isShowChecked_%s_manual_%s_saved_%s", LockData.LockObjects.FORM_DATA.name(), formDataId, reportType.getName(), showChecked, all, saved), 0, true);
                        }
                    }
                }
            }
        }
        reportService.delete(formDataId, manual);
    }
}
