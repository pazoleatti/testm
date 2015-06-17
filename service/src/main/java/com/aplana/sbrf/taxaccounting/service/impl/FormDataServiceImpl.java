package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskTypeDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormPerformerDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceRollbackException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.impl.eventhandler.EventLauncher;
import com.aplana.sbrf.taxaccounting.service.shared.FormDataCompositionService;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    protected static final Log log = LogFactory.getLog(FormDataServiceImpl.class);

    private static final SimpleDateFormat SDF_DD_MM_YYYY = new SimpleDateFormat("dd.MM.yyyy");
	private static final SimpleDateFormat SDF_HH_MM_DD_MM_YYYY = new SimpleDateFormat("HH:mm dd.MM.yyyy");
    private static final SimpleDateFormat SDF_DD_MM_YYYY_HH_MM_SS = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private static final Calendar CALENDAR = Calendar.getInstance();
    private static final Date MAX_DATE;
    static {
        CALENDAR.clear();
        CALENDAR.set(9999, Calendar.DECEMBER, 31);
        MAX_DATE = CALENDAR.getTime();
        CALENDAR.clear();
    }

    private static final String XLSX_EXT = "xlsx";
    private static final String XLS_EXT = "xls";
    private static final String XLSM_EXT = "xlsm";
    public static final String MSG_IS_EXIST_FORM = "Существует экземпляр налоговой формы \"%s\" типа \"%s\" в подразделении \"%s\" в периоде \"%s\"%s";
    final static String LOCK_MESSAGE = "Форма заблокирована и не может быть изменена. Попробуйте выполнить операцию позже.";
    final static String LOCK_MESSAGE_TASK = "Выполнение операции \"%s\" невозможно, т.к. для текущего экземпляра налоговой формы запущена операция изменения данных";
    final static String LOCK_REFBOOK_MESSAGE = "Справочник \"%s\" заблокирован и не может быть использован для заполнения атрибутов формы. Попробуйте выполнить операцию позже.";
    final static String REF_BOOK_RECORDS_ERROR =  "Строка %s, атрибут \"%s\": период актуальности значения не пересекается с отчетным периодом формы";
    final static String DEPARTMENT_REPORT_PERIOD_NOT_FOUND_ERROR = "Не найден отчетный период подразделения с id = %d.";
    private static final String SAVE_ERROR = "Найдены ошибки при сохранении формы!";
    private static final String SORT_ERROR = "Найдены ошибки при сортировке строк формы!";
    private static final String FD_NOT_IN_RANGE = "Найдена форма: \"%s\", \"%d\", \"%s\", \"%s\" в подразделении \"%s\", состояние - \"%s\"";
    private static final String LOCK_CURRENT_1 =
            "Текущая налоговая форма заблокирована пользователем \"%s\" в \"%s\". Попробуйте выполнить операцию позже";
    private static final String SOURCE_MSG_ERROR =
            "существует форма-приёмник, статус которой отличен от \"Создана\". Консолидация возможна только в том случае, если форма-приёмник не существует или имеет статус \"Создана\"";
    //Выводит информацию о НФ в определенном формате
    private static final String FORM_DATA_INFO_MSG = "«%s», «%s», «%s», «%s»%s";
    private static final String NOT_CONSOLIDATE_DESTINATION_FORM_WARNING =
            "Не выполнена консолидация данных в форму \"%s\", \"%s\", \"%s\", \"%s %d%s\"";
    private static final String NOT_CONSOLIDATE_SOURCE_FORM_WARNING =
            "Не выполнена консолидация данных из формы \"%s\", \"%s\", \"%s\", \"%s %d%s\" в статусе \"%s\"";
    private static final String NOT_EXIST_SOURCE_FORM_WARNING =
            "Не выполнена консолидация данных из формы \"%s\", \"%s\", \"%s\", \"%s %d%s\" - экземпляр формы не создан";
    private static final String NOT_CONSOLIDATED_SOURCE_FORM_ERR =
            "Не выполнена консолидация данных из форм - источников, которых находятся в статусе \"Принята\":";
    private static final String NOT_CONSOLIDATED_SOURCE_FORM =
            "\"%s\", \"%s\", \"%s\", \"%s\", \"%d%s\"";
    private static final String NOT_ACCEPTED_SOURCE_FORM_WARN =
            "Не получены данные из всех назначенных форм-источников:";
    private static final String NOT_ACCEPTED_SOURCE_FORM =
            "\"%s\", \"%s\", \"%s\", \"%s\", \"%d%s\" - \"%s\"";
    private static final String CONSOLIDATION_NOT_TOPICAL = "Текущая форма содержит неактуальные консолидированные данные " +
            "(расприняты формы-источники / удалены назначения по формам-источникам, на основе которых ранее выполнена " +
            "консолидация). Для коррекции консолидированных данных необходимо нажать на кнопку \"Консолидировать\"";

    @Autowired
	private FormDataDao formDataDao;
    @Autowired
    private FormTemplateService formTemplateService;
    @Autowired
    private FormTypeService formTypeService;
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
	private DepartmentService departmentService;
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
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private IfrsDataService ifrsDataService;
    @Autowired
    private SourceService sourceService;
    @Autowired
    private AsyncTaskTypeDao asyncTaskTypeDao;
    @Autowired
    private BlobDataService blobDataService;

    @Override
    public long createFormData(Logger logger, TAUserInfo userInfo, int formTemplateId, int departmentReportPeriodId, FormDataKind kind, Integer periodOrder, boolean importFormData) {
        formDataAccessService.canCreate(userInfo, formTemplateId, kind, departmentReportPeriodId);
        return createFormDataWithoutCheck(logger, userInfo, formTemplateId, departmentReportPeriodId, kind, periodOrder, importFormData);
    }

    @Override
    public void createManualFormData(Logger logger, TAUserInfo userInfo, Long formDataId) {
        FormData formData = formDataDao.get(formDataId, false);
        formDataAccessService.canCreateManual(logger, userInfo, formDataId);
		formData.setManual(true);
        formDataDao.updateManual(formData);
        dataRowDao.createManual(formData);
        logger.info("Для налоговой формы успешно создана версия ручного ввода");
    }

    @Override
    public void importFormData(Logger logger, TAUserInfo userInfo, long formDataId, boolean isManual, InputStream inputStream, String fileName, FormDataEvent formDataEvent) {
        loadFormData(logger, userInfo, formDataId, isManual, false, inputStream, fileName, formDataEvent, null);
    }

    @Override
    public void importFormData(Logger logger, TAUserInfo userInfo, long formDataId, boolean isManual, InputStream inputStream, String fileName, LockStateLogger stateLogger) {
        loadFormData(logger, userInfo, formDataId, isManual, true, inputStream, fileName, FormDataEvent.IMPORT, stateLogger);
    }

    @Override
    @Transactional
    public void migrationFormData(Logger logger, TAUserInfo userInfo, long formDataId, InputStream inputStream, String fileName) {
        loadFormData(logger, userInfo, formDataId, false, false, inputStream, fileName, FormDataEvent.MIGRATION, null);
    }

    private void loadFormData(Logger logger, TAUserInfo userInfo, long formDataId, boolean isManual, boolean isInner, InputStream inputStream, String fileName, FormDataEvent formDataEvent, LockStateLogger stateLogger) {
        String key = generateTaskKey(formDataId, ReportType.EDIT_FD);

        formDataAccessService.canEdit(userInfo, formDataId, isManual);

        File dataFile = null;
        OutputStream dataFileOutputStream = null;
        InputStream dataFileInputStream = null;

        try {
            log.info(String.format("Создание временного файла: %s", key));
            if (stateLogger != null) {
                stateLogger.updateState("Создание временного файла");
            }
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
                            log.info(String.format("Проверка ЭЦП: %s", key));
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

            FormData fd = formDataDao.get(formDataId, isManual);

            if (check) {
                dataFileInputStream = new BufferedInputStream(new FileInputStream(dataFile));
                Map<String, Object> additionalParameters = new HashMap<String, Object>();
                additionalParameters.put("ImportInputStream", dataFileInputStream);
                additionalParameters.put("UploadFileName", fileName);
                if (stateLogger != null) {
                    stateLogger.updateState("Импорт XLSM-файла");
                }
                log.info(String.format("Выполнение скрипта: %s", key));
                dataRowDao.createTemporary(fd);
                formDataScriptingService.executeScript(userInfo, fd, formDataEvent, logger, additionalParameters);
                IOUtils.closeQuietly(dataFileInputStream);
            }

            if (logger.containsLevel(LogLevel.ERROR)) {
                if (stateLogger != null) {
                    stateLogger.updateState("Сохранение ошибок");
                }
                log.info(String.format("Сохранение ошибок: %s", key));
                String uuid = logEntryService.save(logger.getEntries());
                throw new ServiceLoggerException("Есть критические ошибки при выполнении скрипта", uuid);
            } else if (isInner) {
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

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(departmentReportPeriodId);
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
            performer.setPrintDepartmentId(departmentReportPeriod.getDepartmentId());
            performer.setReportDepartmentName(departmentService.getReportDepartmentName(departmentReportPeriod.getDepartmentId()));
        }
        formData.setPerformer(performer);

        // Execute scripts for the form event CREATE
		formDataScriptingService.executeScript(userInfo, formData, FormDataEvent.CREATE, logger, null);
		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Произошли ошибки в скрипте создания налоговой формы",
                    logEntryService.save(logger.getEntries()));
		}
		formDataDao.save(formData);

        if (!importFormData) {
            logBusinessService.add(formData.getId(), null, userInfo, FormDataEvent.CREATE, null);
            auditService.add(FormDataEvent.CREATE, userInfo, formData.getDepartmentId(), formData.getReportPeriodId(),
                    null, formData.getFormType().getName(), formData.getKind().getId(), "Форма создана", null);
        }

        // Заполняем начальные строки (но не сохраняем)
		dataRowDao.saveRows(formData, formTemplate.getRows());

        // Execute scripts for the form event AFTER_CREATE
        formDataScriptingService.executeScript(userInfo, formData, FormDataEvent.AFTER_CREATE, logger, null);
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(
                    "Произошли ошибки в скрипте после создания налоговой формы",
                    logEntryService.save(logger.getEntries()));
        }

		dataRowDao.commit(formData);

        updatePreviousRowNumber(formData, userInfo);
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
        if (periodOrder != null) {
            List<Months> availableMonthList = reportPeriodService.getAvailableMonthList(departmentReportPeriod.getReportPeriod().getId());
            if  (periodOrder > 1 && availableMonthList.contains(Months.fromId(periodOrder - 1))) {
                return formDataDao.find(formTemplate.getType().getId(), kind, departmentReportPeriod.getId().intValue(), Integer.valueOf(periodOrder - 1));
            }
        }
        ReportPeriod prevReportPeriod = reportPeriodService.getPrevReportPeriod(departmentReportPeriod.getReportPeriod().getId());
        if (prevReportPeriod != null) {
            Integer lastPeriodOrder = null;
            if (periodOrder != null) {
                List<Months> availableMonthList = reportPeriodService.getAvailableMonthList(prevReportPeriod.getId());
                lastPeriodOrder = availableMonthList.get(availableMonthList.size() - 1).getId();
            }
            DepartmentReportPeriod departmentReportPeriodOld = departmentReportPeriodService.getLast(departmentReportPeriod.getDepartmentId(), prevReportPeriod.getId());
            if (departmentReportPeriodOld != null) {
                return formDataDao.find(formTemplate.getType().getId(), kind, departmentReportPeriodOld.getId().intValue(), lastPeriodOrder);
            }
        }
        return null;
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
        checkLockedMe(lockService.getLock(generateTaskKey(formData.getId(), ReportType.EDIT_FD)), userInfo.getUser());
        //Проверяем не заблокирована ли нф какими-либо операциями
        checkLockedByTask(formData.getId(), logger, userInfo, "Добавление строки", true);

		FormTemplate formTemplate = formTemplateService.get(formData.getFormTemplateId());
		
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
        checkLockedMe(lockService.getLock(generateTaskKey(formData.getId(), ReportType.EDIT_FD)), userInfo.getUser());
        //Проверяем не заблокирована ли нф какими-либо операциями
        checkLockedByTask(formData.getId(), logger, userInfo, "Удаление строки", true);

		FormTemplate formTemplate = formTemplateService.get(formData.getFormTemplateId());
		
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
		formDataAccessService.canEdit(userInfo, formData.getId(), formData.isManual());

		formDataScriptingService.executeScript(userInfo, formData,
                FormDataEvent.CALCULATE, logger, null);

        if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceException("Найдены ошибки при выполнении расчета формы");
		} else {
			logger.info("Расчет завершен, фатальных ошибок не обнаружено");
		}
	}

	@Override
    @Transactional
	public void doCheck(Logger logger, TAUserInfo userInfo, FormData formData, boolean editMode) {
        formDataAccessService.canRead(userInfo, formData.getId());

		formDataAccessService.canRead(userInfo, formData.getId());

		formDataScriptingService.executeScript(userInfo, formData, FormDataEvent.CHECK, logger, null);

        checkPerformer(logger, formData);

        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(
                    "Найдены ошибки при выполнении проверки формы", logEntryService.save(logger.getEntries()));
        } else {
            ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(formData.getReportPeriodId());
            if (formData.getState() == WorkflowState.ACCEPTED) {
                // Система проверяет, существует ли экземпляр формы-приёмника, консолидация в который не была выполнена.
                List<DepartmentFormType> destinationsDFT = departmentFormTypeDao.getFormDestinations(
                        formData.getDepartmentId(),
                        formData.getFormType().getId(),
                        formData.getKind(),
                        reportPeriod.getStartDate(),
                        reportPeriod.getEndDate());
                for (DepartmentFormType dftTarget : destinationsDFT) {
                    FormData destinationFD =
                            findFormData(dftTarget.getFormTypeId(), dftTarget.getKind(), formData.getDepartmentReportPeriodId(), formData.getPeriodOrder());
                    if (destinationFD != null && !sourceService.isFDSourceConsolidated(destinationFD.getId(), formData.getId())){
                        ReportPeriod rp = reportPeriodService.getReportPeriod(destinationFD.getReportPeriodId());
                        DepartmentReportPeriod drp = departmentReportPeriodService.get(destinationFD.getDepartmentReportPeriodId());
                        logger.warn(
                                NOT_CONSOLIDATE_DESTINATION_FORM_WARNING,
                                departmentService.getDepartment(destinationFD.getDepartmentId()).getName(),
                                destinationFD.getFormType().getName(),
                                destinationFD.getKind().getName(),
                                rp.getName() + (destinationFD.getPeriodOrder() != null?" " + Months.fromId(destinationFD.getPeriodOrder()).getTitle():""),
                                rp.getTaxPeriod().getYear(),
                                drp.getCorrectionDate() != null ? String.format("с датой сдачи корректировки %s",
                                        SDF_DD_MM_YYYY.format(drp.getCorrectionDate())) : ""
                        );
                    }
                }
            }
            //Система проверяет статус консолидации из форм-источников.
            List<DepartmentFormType> dftSources = departmentFormTypeDao.getFormSources(
                    formData.getDepartmentId(),
                    formData.getFormType().getId(),
                    formData.getKind(),
                    reportPeriod.getCalendarStartDate(),
                    reportPeriod.getEndDate());
            for (DepartmentFormType dftSource : dftSources){
                DepartmentReportPeriod sourceDepartmentReportPeriod =
                        departmentReportPeriodService.getLast(dftSource.getDepartmentId(), formData.getReportPeriodId());
                FormData sourceFormData =
                        findFormData(dftSource.getFormTypeId(), dftSource.getKind(),
								sourceDepartmentReportPeriod.getId(), formData.getPeriodOrder());
                ReportPeriod rp = reportPeriodService.getReportPeriod(formData.getReportPeriodId());
                if (sourceFormData == null){
                    DepartmentReportPeriod drp = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
                    logger.warn(
                            NOT_EXIST_SOURCE_FORM_WARNING,
                            departmentService.getDepartment(dftSource.getDepartmentId()).getName(),
                            formTypeService.get(dftSource.getFormTypeId()).getName(),
                            dftSource.getKind().getName(),
                            rp.getName(),
                            rp.getTaxPeriod().getYear(),
                            drp.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s",
                                    SDF_DD_MM_YYYY.format(drp.getCorrectionDate())) : ""
                    );
                } else if (!sourceService.isFDSourceConsolidated(formData.getId(), sourceFormData.getId())){
                    DepartmentReportPeriod drp = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
                    logger.warn(
							NOT_CONSOLIDATE_SOURCE_FORM_WARNING,
							departmentService.getDepartment(sourceFormData.getDepartmentId()).getName(),
							sourceFormData.getFormType().getName(),
							sourceFormData.getKind().getName(),
							rp.getName() + (sourceFormData.getPeriodOrder() != null ? " " + Months.fromId(sourceFormData.getPeriodOrder()).getTitle() : ""),
							rp.getTaxPeriod().getYear(),
							drp.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s",
									SDF_DD_MM_YYYY.format(drp.getCorrectionDate())) : "",
							sourceFormData.getState().getName()
					);
                }
            }
            if (!dftSources.isEmpty() && !logger.containsLevel(LogLevel.WARNING)){
                logger.info("Консолидация выполнена из всех форм-источников");
            }
            // Ошибка для отката транзакции
            logger.info("Проверка завершена, фатальных ошибок не обнаружено");
            throw new ServiceRollbackException("Ошибок не обнаружено");
        }
    }

    private void checkPerformer(Logger logger, FormData formData) {
        boolean check = true;
        if (formData.getPerformer() == null || formData.getPerformer().getPrintDepartmentId() == null || formData.getSigners().size() == 0 ||
                    formData.getPerformer().getReportDepartmentName() == null || formData.getPerformer().getName() == null || formData.getPerformer().getPhone() == null ||
                    formData.getPerformer().getReportDepartmentName().trim().isEmpty() || formData.getPerformer().getName().trim().isEmpty() || formData.getPerformer().getPhone().trim().isEmpty()) {
            check = false;
        }
        if (check)
            for (FormDataSigner signer : formData.getSigners()) {
                if (signer.getName().trim().isEmpty() || signer.getPosition().trim().isEmpty()) {
                    check = false;
                    break;
                }
            }
        if (!check) {
            logger.error("Параметры печатной формы заполнены не полностью. Ожидается: подразделение, исполнитель, телефон, данные хотя бы об одном подписанте.");
        }
    }

    @Override
    @Transactional
    public void savePerformer(Logger logger, TAUserInfo userInfo, FormData formData) {
        // Форма должна быть заблокирована текущим пользователем для редактирования
        checkLockedMe(lockService.getLock(generateTaskKey(formData.getId(), ReportType.EDIT_FD)), userInfo.getUser());
        formDataAccessService.canEdit(userInfo, formData.getId(), formData.isManual());
        formDataDao.savePerformerSigner(formData);

        deleteReport(formData.getId(), null, userInfo.getUser().getId());
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
		formDataAccessService.canEdit(userInfo, formData.getId(), formData.isManual());

        //Проверка актуальности справочных значений
        checkReferenceValues(logger, formData, true);

        // Отработка скриптом события сохранения
        // dataRowDao.createTemporary(formData); не вызываем, т.к это должно быть сделано до этого, в вызывающих операциях
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
        updatePreviousRowNumberAttr(formData, logger, userInfo);

        formDataDao.save(formData);

		dataRowDao.commit(formData);

        deleteReport(formData.getId(), formData.isManual(), userInfo.getUser().getId());

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
        checkLockAnotherUser(lockService.getLock(generateTaskKey(formDataId, ReportType.EDIT_FD)),
                logger,  userInfo.getUser());
        //Проверяем не заблокирована ли нф какими-либо операциями
        checkLockedByTask(formDataId, logger, userInfo, "Удаление НФ", true);

		FormData formData = formDataDao.get(formDataId, manual);
        if (manual) {
            formDataAccessService.canDeleteManual(logger, userInfo, formDataId);
			formDataDao.deleteManual(formData);
        } else {
            formDataAccessService.canDelete(userInfo, formDataId);
            auditService.add(FormDataEvent.DELETE, userInfo, formData.getDepartmentId(), formData.getReportPeriodId(),
                    null, formData.getFormType().getName(), formData.getKind().getId(), "Форма удалена", null, formData.getFormType().getId());

            formDataDao.delete(formDataId);
            deleteReport(formDataId, null, userInfo.getUser().getId());
            auditService.add(FormDataEvent.DELETE, userInfo, formData.getDepartmentId(), formData.getReportPeriodId(),
                    null, formData.getFormType().getName(), formData.getKind().getId(), "Форма удалена", null, formData.getFormType().getId());
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
        String lockKey = generateTaskKey(formDataId, ReportType.EDIT_FD);
        //checkLockAnotherUser(lockService.getLock(lockKey), logger,  userInfo.getUser());
        //Проверяем не заблокирована ли нф операцией загрузки в нее
        //checkLockedByImport(formDataId, logger);

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
        dataRowDao.createTemporary(formData);
        List<String> lockedObjects = new ArrayList<String>();
        try {
            switch (workflowMove){
                case PREPARED_TO_APPROVED:
                    lockForm(lockedObjects, userInfo, lockKey, logger, formData);
                    //Проверяем что записи справочников, на которые есть ссылки в нф все еще существуют в периоде формы
                    checkReferenceValues(logger, formData, false);
                    //Делаем переход
                    moveProcess(formData, userInfo, workflowMove, note, logger);
                    break;
                case CREATED_TO_PREPARED:
                case APPROVED_TO_ACCEPTED:
                case PREPARED_TO_ACCEPTED:
                case CREATED_TO_ACCEPTED:
                    lockForm(lockedObjects, userInfo, lockKey, logger, formData);
                    //Проверяем что записи справочников, на которые есть ссылки в нф все еще существуют в периоде формы
                    checkReferenceValues(logger, formData, false);
                    checkConsolidateFromSources(formData, logger);
                    //Делаем переход
                    moveProcess(formData, userInfo, workflowMove, note, logger);
                    break;
                case APPROVED_TO_CREATED:
                case ACCEPTED_TO_APPROVED:
                case ACCEPTED_TO_PREPARED:
                case ACCEPTED_TO_CREATED:
                    sourceService.updateFDDDConsolidation(formDataId);
                    moveProcess(formData, userInfo, workflowMove, note, logger);
                    break;
                default:
                    //Делаем переход
                    moveProcess(formData, userInfo, workflowMove, note, logger);
            }
        } finally {
            for (String lock : lockedObjects) {
                lockService.unlock(lock, userInfo.getUser().getId());
            }
        }
    }

    private void lockForm(List<String> listWithLocks, TAUserInfo userInfo, String lockKey, Logger logger, FormData formData){
        //Устанавливаем блокировку на текущую нф
        int userId = userInfo.getUser().getId();
        checkLockAnotherUser(lockService.getLock(lockKey), logger,  userInfo.getUser());
        LockData lockData = lockService.lock(lockKey, userId, getFormDataFullName(formData.getId(), null, null),
                lockService.getLockTimeout(LockData.LockObjects.FORM_DATA));
        if (lockData == null) {
            //Блокировка установлена
            listWithLocks.add(lockKey);
            //Проверяем блокировку необходимых справочников. Их не должно быть
            for (Column column : formData.getFormColumns()) {
                if (ColumnType.REFBOOK.equals(column.getColumnType())) {
                    Long attributeId = ((RefBookColumn) column).getRefBookAttributeId();
                    if (attributeId != null) {
                        RefBook refBook = refBookDao.getByAttribute(attributeId);
                        String referenceLockKey = LockData.LockObjects.REF_BOOK.name() + "_" + refBook.getId();
                        if (lockService.isLockExists(referenceLockKey, false)) {
                            throw new ServiceLoggerException(String.format(LOCK_REFBOOK_MESSAGE, refBook.getName()),
                                    logEntryService.save(logger.getEntries()));
                        }
                    }
                }
            }
        } else {
            throw new ServiceLoggerException(LOCK_MESSAGE,
                    logEntryService.save(logger.getEntries()));
        }
    }

    private void checkConsolidateFromSources(FormData formData, Logger logger){
        //Проверка на неактуальные консолидированные данные
        if (sourceService.isFDConsolidationTopical(formData.getId())){
            logger.error(CONSOLIDATION_NOT_TOPICAL);
            throw new ServiceLoggerException("", logEntryService.save(logger.getEntries()));
        }
        //Проверка выполнена ли консолидация из всех принятых источников текущего экземпляра
        ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(formData.getReportPeriodId());
        List<DepartmentFormType> departmentFormTypesSources = departmentFormTypeDao.getFormSources(
                formData.getDepartmentId(),
                formData.getFormType().getId(),
                formData.getKind(),
                reportPeriod.getCalendarStartDate(),
                reportPeriod.getEndDate());
        ArrayList<FormData> notAcceptedFDSources = new ArrayList<FormData>();
        ArrayList<FormData> notConsolidatedFDSources = new ArrayList<FormData>();
        for (DepartmentFormType sourceDFT : departmentFormTypesSources) {
            DepartmentReportPeriod sourceDepartmentReportPeriod =
                    departmentReportPeriodService.getLast(sourceDFT.getDepartmentId(), formData.getReportPeriodId());
            FormData sourceForm = findFormData(sourceDFT.getFormTypeId(), sourceDFT.getKind(),
                    sourceDepartmentReportPeriod.getId(), formData.getPeriodOrder());
            if (
                    sourceForm != null && sourceForm.getState() == WorkflowState.ACCEPTED
                    &&
                    !sourceService.isFDSourceConsolidated(formData.getId(), sourceForm.getId())
                    ) {
                notConsolidatedFDSources.add(sourceForm);
                DepartmentReportPeriod drp = departmentReportPeriodService.get(sourceForm.getDepartmentReportPeriodId());
                logger.error(NOT_CONSOLIDATED_SOURCE_FORM,
                        departmentService.getDepartment(sourceForm.getDepartmentId()).getName(),
                        sourceForm.getKind().getName(),
                        sourceForm.getFormType().getName(),
                        reportPeriod.getName() + (sourceForm.getPeriodOrder() != null ? " " + Months.fromId(sourceForm.getPeriodOrder()).getTitle() : ""),
                        reportPeriod.getTaxPeriod().getYear(),
                        drp.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s",
                                SDF_DD_MM_YYYY.format(drp.getCorrectionDate())) : ""
                );
            } else if (sourceForm == null || sourceForm.getState() != WorkflowState.ACCEPTED) {
                notAcceptedFDSources.add(sourceForm);
                logger.warn(NOT_ACCEPTED_SOURCE_FORM,
                        departmentService.getDepartment(sourceDFT.getDepartmentId()).getName(),
                        sourceDFT.getKind().getName(),
                        formTypeService.get(sourceDFT.getFormTypeId()).getName(),
                        reportPeriod.getName() + (formData.getPeriodOrder() != null ? " " + Months.fromId(formData.getPeriodOrder()).getTitle() : ""),
                        reportPeriod.getTaxPeriod().getYear(),
                        sourceDepartmentReportPeriod.getCorrectionDate() != null ?
                                String.format(" с датой сдачи корректировки %s",
                                        SDF_DD_MM_YYYY.format(sourceDepartmentReportPeriod.getCorrectionDate())) : "",
                        sourceForm == null ? "Не создана" : sourceForm.getState().getName());
            }
        }
        //Если консолидация из всех принятых источников текущего экземпляра не была выполнена
        if (!notConsolidatedFDSources.isEmpty()) {
            logger.clear(LogLevel.WARNING);
            logger.getEntries().add(0, new LogEntry(LogLevel.ERROR, NOT_CONSOLIDATED_SOURCE_FORM_ERR));
            throw new ServiceLoggerException("", logEntryService.save(logger.getEntries()));
        }
        //Если консолидация из всех принятых источников текущего экземпляра была выполнена, но есть непринятые или несозданные источники
        if (!notAcceptedFDSources.isEmpty()) {
            logger.getEntries().add(0, new LogEntry(LogLevel.WARNING, NOT_ACCEPTED_SOURCE_FORM_WARN));
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
    public void checkReferenceValues(Logger logger, FormData formData, boolean needCheckTemp) {
        Map<Long, List<Long>> recordsToCheck = new HashMap<Long, List<Long>>();
        Map<Long, List<ReferenceInfo>> referenceInfoMap = new HashMap<Long, List<ReferenceInfo>>();
        List<DataRow<Cell>> rows;
        if (!needCheckTemp) {
            rows = dataRowDao.getSavedRows(formData, null);
        } else {
            rows = dataRowDao.getTempRows(formData, null);
        }
        if (rows.size() > 0) {
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
                                long uniqueRecordId = row.getCell(column.getAlias()).getNumericValue().longValue();
                                if (!referenceInfoMap.containsKey(uniqueRecordId)) {
                                    referenceInfoMap.put(uniqueRecordId, new ArrayList<ReferenceInfo>());
                                }
                                referenceInfoMap.get(uniqueRecordId).add(new ReferenceInfo(row.getIndex(), column.getName()));
                            }
                        }
                    }
                }
            }

            ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(formData.getReportPeriodId());
            boolean error = false;
            for (Map.Entry<Long, List<Long>> referencesToCheck : recordsToCheck.entrySet()) {
                RefBookDataProvider provider = refBookFactory.getDataProvider(referencesToCheck.getKey());
                List<Long> inactiveRecords = provider.getInactiveRecordsInPeriod(referencesToCheck.getValue(), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
                if (!inactiveRecords.isEmpty()) {
                    for (Long inactiveRecord : inactiveRecords) {
                        for (ReferenceInfo referenceInfo : referenceInfoMap.get(inactiveRecord)) {
                            logger.error(String.format(REF_BOOK_RECORDS_ERROR, referenceInfo.getRownum(), referenceInfo.getColumnName()));
                        }
                    }
                    error = true;
                }
            }

            if (error) {
                throw new ServiceLoggerException("Произошла ошибка при проверке справочных значений формы",
                        logEntryService.save(logger.getEntries()));
            }
        }
    }

    private void moveProcess(FormData formData, TAUserInfo userInfo, WorkflowMove workflowMove, String note, Logger logger) {
        formDataScriptingService.executeScript(userInfo, formData, workflowMove.getEvent(), logger, null);

        if (WorkflowMove.CREATED_TO_ACCEPTED.equals(workflowMove) ||
                WorkflowMove.CREATED_TO_APPROVED.equals(workflowMove) ||
                WorkflowMove.CREATED_TO_PREPARED.equals(workflowMove) ||
                WorkflowMove.PREPARED_TO_APPROVED.equals(workflowMove) ||
                WorkflowMove.PREPARED_TO_ACCEPTED.equals(workflowMove)) {
            checkPerformer(logger, formData);
        }

        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(
                    "Произошли ошибки в скрипте, который выполняется перед переходом",
                    logEntryService.save(logger.getEntries()));
        }

        if (formData.getFormType().getIsIfrs() && workflowMove.getFromState().equals(WorkflowState.ACCEPTED) &&
                departmentReportPeriodService.get(formData.getDepartmentReportPeriodId()).getCorrectionDate() == null) {
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
            }
        }

        dataRowDao.commit(formData);

        logger.info("Форма \"" + formData.getFormType().getName() + "\" переведена в статус \"" + workflowMove.getToState().getName() + "\"");

        //Считаем что при наличие версии ручного ввода движение о жц невозможно
        deleteReport(formData.getId(), null, userInfo.getUser().getId());

        logBusinessService.add(formData.getId(), null, userInfo, workflowMove.getEvent(), note);
        auditService.add(workflowMove.getEvent(), userInfo, formData.getDepartmentId(), formData.getReportPeriodId(),
                null, formData.getFormType().getName(), formData.getKind().getId(), workflowMove.getEvent().getTitle(), null, formData.getFormType().getId());

        updatePreviousRowNumberAttr(formData, workflowMove, logger, userInfo);
    }

    private static final String CORRECTION_PATTERN = ", «%s»";
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
    @Transactional(readOnly = false)
    public void compose(final FormData formData, TAUserInfo userInfo, Logger logger, LockStateLogger stateLogger) {
        stateLogger.updateState("Выполнение проверок на возможность консолидации");
        // Период ввода остатков не обрабатывается. Если форма ежемесячная, то только первый месяц периода может быть периодом ввода остатков.
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(formData.getReportPeriodId());

        List<DepartmentFormType> departmentFormTypesSources = departmentFormTypeDao.getFormSources(
                formData.getDepartmentId(),
                formData.getFormType().getId(),
                formData.getKind(),
                reportPeriod.getCalendarStartDate(),
                reportPeriod.getEndDate());

        HashSet<Long> srcAcceptedIds = new HashSet<Long>();
        ArrayList<String> msgPull = new ArrayList<String>(0);
        //Список для блокировки форм
        ArrayList<FormData> sources = new ArrayList<FormData>(departmentFormTypesSources.size());
        for (DepartmentFormType sourceDFT : departmentFormTypesSources){
            // Последний отчетный период подразделения
            DepartmentReportPeriod sourceDepartmentReportPeriod =
                    departmentReportPeriodService.getLast(sourceDFT.getDepartmentId(), formData.getReportPeriodId());
            if (sourceDepartmentReportPeriod == null) {
                continue;
            }
            FormData sourceForm = findFormData(sourceDFT.getFormTypeId(), sourceDFT.getKind(),
                    sourceDepartmentReportPeriod.getId(), formData.getPeriodOrder());
            if (sourceForm == null){
                continue;
            }
            //Запись на будущее, чтобы второго цикла не делать
            //1E.
            if (sourceForm.getState() == WorkflowState.ACCEPTED){
                srcAcceptedIds.add(sourceForm.getId());
                msgPull.add(String.format(FORM_DATA_INFO_MSG,
                        departmentService.getDepartment(sourceDFT.getDepartmentId()).getName(),
                        sourceDFT.getKind().getName(),
                        formTypeService.get(sourceDFT.getFormTypeId()).getName(),
                        reportPeriodService.getReportPeriod(formData.getReportPeriodId()).getName(),
                        (sourceDepartmentReportPeriod.getCorrectionDate() != null ?
                                String.format(CORRECTION_PATTERN, SDF_DD_MM_YYYY.format(sourceDepartmentReportPeriod.getCorrectionDate()))
                                :
                                "")
                ));
                sources.add(sourceForm);
            }
        }

        //1Е.  Система проверяет экземпляр на возможность выполнения консолидации в него. Не существует ни одной принятой формы-источника
        if (srcAcceptedIds.isEmpty()){
            throw new ServiceException("Для текущей формы не существует ни одного источника в статусе \"Принята\"");
        }

        /*
        //Блокировка всех экземпляров источников
        List<String> lockedForms = new ArrayList<String>();
        String lockKey = "";
        //Переменная для отмечания консолидации в таблице консолидации
        for (FormData sourceForm : sources){
            // Проверяем/устанавливаем блокировку для источников
            LockData lockData;
            Pair<ReportType, LockData> lockType = getLockTaskType(sourceForm.getId());
            if (lockType == null) {
                lockKey = generateTaskKey(sourceForm.getId(), ReportType.EDIT_FD);
                lockData = lockService.lock(
                        lockKey,
                        userInfo.getUser().getId(), getFormDataFullName(formData.getId(), null, null),
                        lockService.getLockTimeout(LockData.LockObjects.FORM_DATA));
            } else {
                lockData = lockType.getSecond();
            }

            if (lockData != null) {
                DepartmentReportPeriod drp = departmentReportPeriodService.get(sourceForm.getDepartmentReportPeriodId());
                logger.error(LOCK_SOURCE,
                        sourceForm.getFormType().getName(),
                        sourceForm.getKind().getName(),
                        drp.getReportPeriod().getTaxPeriod().getYear() + " " + drp.getReportPeriod().getName(),
                        departmentService.getDepartment(sourceForm.getDepartmentId()).getName(),
                        userService.getUser(lockData.getUserId()).getName(),
                        SDF_HH_MM_DD_MM_YYYY.format(lockData.getDateLock()));
            } else {
                lockedForms.add(lockKey);
            }
        }

        //2А. Выводим ошибки блокировок
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceException("Ошибка при консолидации");
        }
         */
        //3. Консолидируем
        stateLogger.updateState("Консолидация данных в форму");
        ScriptComponentContextImpl scriptComponentContext = new ScriptComponentContextImpl();
        scriptComponentContext.setUserInfo(userInfo);
        scriptComponentContext.setLogger(logger);
        FormDataCompositionService formDataCompositionService = applicationContext.getBean(FormDataCompositionService.class);
        ((ScriptComponentContextHolder) formDataCompositionService).setScriptComponentContext(scriptComponentContext);
        /*Integer periodOrder =
                (formData.getKind() == FormDataKind.PRIMARY || formData.getKind() == FormDataKind.CONSOLIDATED) ? formData.getPeriodOrder() : null;*/
        formDataCompositionService.compose(formData, 0, null, formData.getFormType().getId(), formData.getKind());

        //Система выводит сообщение в панель уведомлений
        logger.info("Выполнена консолидация данных из форм-источников:");
        for (String s : msgPull){
            logger.info(s);
        }

        //Удаление отчета НФ             
        stateLogger.updateState("Удаление отчетов формы");
        reportService.delete(formData.getId(), null);
        //Система проверяет, содержит ли макет НФ хотя бы одну графу со сквозной автонумерацией
        stateLogger.updateState("Обновление сквозной нумерации");
        updatePreviousRowNumber(formData, logger, userInfo, false);
        //Обновление записей о консолидации
        sourceService.deleteFDConsolidationInfo(Arrays.asList(formData.getId()));
        sourceService.addFormDataConsolidationInfo(formData.getId(), srcAcceptedIds);
    }

    @Override
    public void checkCompose(final FormData formData, TAUserInfo userInfo, Logger logger) {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(formData.getReportPeriodId());
        //1А. Отчетный период закрыт
        if (!departmentReportPeriod.isActive()){
            throw new ServiceException("отчетный период закрыт, консолидация не может быть выполнена.");
        }
        //1Б. Статус экземпляра не допускает его редактирование
        if (formData.getState() != WorkflowState.CREATED) {
            throw new ServiceException("форма находится в статусе \"%s\", консолидация возможна только в статусе \"Создана\"",
                    formData.getState().getName());
        }
        //1В. Проверяем формы-приемники
        List<DepartmentFormType> destinationDFTs = departmentFormTypeDao.getFormDestinations(
                formData.getDepartmentId(),
                formData.getFormType().getId(),
                formData.getKind(),
                reportPeriod.getCalendarStartDate(),
                reportPeriod.getEndDate());
        ArrayList<String> msgPull = new ArrayList<String>(0);
        for (DepartmentFormType destinationDFT : destinationDFTs){
            DepartmentReportPeriod destinationDRP =
                    departmentReportPeriodService.getLast(destinationDFT.getDepartmentId(), formData.getReportPeriodId());
            FormData destinationForm = findFormData(destinationDFT.getFormTypeId(), destinationDFT.getKind(),
                    destinationDRP.getId(), formData.getPeriodOrder());
            if (destinationForm != null && destinationForm.getState() != WorkflowState.CREATED){
                msgPull.add(String.format(FORM_DATA_INFO_MSG,
                        departmentService.getDepartment(formData.getDepartmentId()).getName(),
                        formData.getKind().getName(),
                        formData.getFormType().getName(),
                        reportPeriodService.getReportPeriod(formData.getReportPeriodId()).getName(),
                        (destinationDRP.getCorrectionDate() != null ?
                                String.format(CORRECTION_PATTERN, SDF_DD_MM_YYYY.format(destinationDRP.getCorrectionDate()))
                                :
                                "")
                ));
            }
        }
        if (!msgPull.isEmpty()){
            logger.error(SOURCE_MSG_ERROR);
            for (String s : msgPull)
                logger.error(s);
            throw new ServiceException(SOURCE_MSG_ERROR);
        }
        //1Г. Отчетный период экземпляра является периодом ввода остатков.
        if (departmentReportPeriod.isBalance()){
            throw new ServiceException("отчетный период является периодом ввода остатков, консолидация не может быть выполнена");
        }

        //1Д. Не существует ни одной назначенной формы-источника.
        List<DepartmentFormType> departmentFormTypesSources = departmentFormTypeDao.getFormSources(
                formData.getDepartmentId(),
                formData.getFormType().getId(),
                formData.getKind(),
                reportPeriod.getCalendarStartDate(),
                reportPeriod.getEndDate());
        if (departmentFormTypesSources.isEmpty()){
            throw new ServiceException("для текущей формы не назначено ни одного источника");
        }
    }

    @Override
    public String getFormDataFullName(long formDataId, String str, ReportType reportType) {
        //TODO: можно оптимизировать и сделать в 1 запрос
        FormData formData = formDataDao.get(formDataId, false);
        Department department = departmentService.getDepartment(formData.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        String name;
        if (reportType != null) {
            switch (reportType) {
                case CSV:
                case EXCEL:
                    name = String.format(LockData.DescriptionTemplate.FORM_DATA_REPORT.getText(),
                            reportType.getName(),
                            formData.getFormType().getName(),
                            formData.getKind().getName(),
                            department.getName(),
                            reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                            formData.getPeriodOrder() != null
                                    ? " " + Formats.getRussianMonthNameWithTier(formData.getPeriodOrder())
                                    : "",
                            reportPeriod.getCorrectionDate() != null
                                    ? " с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())
                                    : "");
                    break;
                case CONSOLIDATE_FD:
                case CALCULATE_FD:
                case CHECK_FD:
                    name = String.format(LockData.DescriptionTemplate.FORM_DATA_TASK.getText(),
                            String.format(reportType.getDescription(), formData.getFormType().getTaxType().getTaxText()),
                            formData.getFormType().getName(),
                            formData.getKind().getName(),
                            department.getName(),
                            reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                            formData.getPeriodOrder() != null
                                    ? " " + Formats.getRussianMonthNameWithTier(formData.getPeriodOrder())
                                    : "",
                            reportPeriod.getCorrectionDate() != null
                                    ? " с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())
                                    : "");
                    break;
                case MOVE_FD:
                    name = String.format(LockData.DescriptionTemplate.FORM_DATA_TASK.getText(),
                            String.format(reportType.getDescription(), str, formData.getFormType().getTaxType().getTaxText()),
                            formData.getFormType().getName(),
                            formData.getKind().getName(),
                            department.getName(),
                            reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                            formData.getPeriodOrder() != null
                                    ? " " + Formats.getRussianMonthNameWithTier(formData.getPeriodOrder())
                                    : "",
                            reportPeriod.getCorrectionDate() != null
                                    ? " с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())
                                    : "");
                    break;
                case IMPORT_FD:
                    name = String.format(LockData.DescriptionTemplate.FORM_DATA_TASK.getText(),
                            reportType.getDescription(),
                            formData.getFormType().getName(),
                            formData.getKind().getName(),
                            department.getName(),
                            reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                            formData.getPeriodOrder() != null
                                    ? " " + Formats.getRussianMonthNameWithTier(formData.getPeriodOrder())
                                    : "",
                            reportPeriod.getCorrectionDate() != null
                                    ? " с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())
                                    : "");
                    break;
                case IMPORT_TF_FD:
                    name = String.format(LockData.DescriptionTemplate.FORM_DATA_IMPORT.getText(),
                            str,
                            formData.getFormType().getName(),
                            formData.getKind().getName(),
                            department.getName(),
                            reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                            formData.getPeriodOrder() != null
                                    ? " " + Formats.getRussianMonthNameWithTier(formData.getPeriodOrder())
                                    : "",
                            reportPeriod.getCorrectionDate() != null
                                    ? " с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())
                                    : "");
                    break;
                default:
                    name = String.format(LockData.DescriptionTemplate.FORM_DATA.getText(),
                            formData.getFormType().getName(),
                            formData.getKind().getName(),
                            department.getName(),
                            reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                            formData.getPeriodOrder() != null
                                    ? " " + Formats.getRussianMonthNameWithTier(formData.getPeriodOrder())
                                    : "",
                            reportPeriod.getCorrectionDate() != null
                                    ? " с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())
                                    : "");
            }
        } else {
            name = String.format(LockData.DescriptionTemplate.FORM_DATA.getText(),
                    formData.getFormType().getName(),
                    formData.getKind().getName(),
                    department.getName(),
                    reportPeriod.getReportPeriod().getName() + " " + reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                    formData.getPeriodOrder() != null
                            ? " " + Formats.getRussianMonthNameWithTier(formData.getPeriodOrder())
                            : "",
                    reportPeriod.getCorrectionDate() != null
                            ? " с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())
                            : "");
        }
        return name;
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
        checkLockAnotherUser(lockService.lock(generateTaskKey(formDataId, ReportType.EDIT_FD),
                userInfo.getUser().getId(),
                getFormDataFullName(formDataId, null, null),
                lockService.getLockTimeout(LockData.LockObjects.FORM_DATA)), null, userInfo.getUser());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void unlock(final long formDataId, final TAUserInfo userInfo) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                lockService.unlock(generateTaskKey(formDataId, ReportType.EDIT_FD), userInfo.getUser().getId());
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
                return lockService.getLock(generateTaskKey(formDataId, ReportType.EDIT_FD));
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
        return formDataDao.get(formDataId, null).isManual();
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
                ReportPeriod period = reportPeriodService.getReportPeriod(formData.getReportPeriodId());
                DepartmentReportPeriod drp = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());

                logger.error(MSG_IS_EXIST_FORM,
                        formData.getFormType().getName(),
                        kind.getName(),
                        departmentService.getDepartment(departmentId).getName(),
                        period.getName() + (formData.getPeriodOrder() != null ? (" - " + Months.fromId(formData.getPeriodOrder()).getTitle()) : "") + " " + period.getTaxPeriod().getYear(),
                        (drp.getCorrectionDate() != null ? (" с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(drp.getCorrectionDate())) : ""));
            }
        }
        return !formDataIds.isEmpty();
    }

    @Override
    public void updateFDTBNames(int depTBId,  String depName, Date dateFrom, Date dateTo, boolean isChangeTB, TAUserInfo user) {
        if (dateFrom == null)
            throw new ServiceException("Должна быть установлена хотя бы \"Дата от\"");
        try {
            List<Long> formDataIds = formPerformerDao.getFormDataId(depTBId, dateFrom, dateTo);
            for(Long formDataId: formDataIds)
                deleteReport(formDataId, null, user.getUser().getId());
            formDataDao.updateFDPerformerTBDepartmentNames(depTBId, depName, dateFrom, dateTo, isChangeTB);
        } catch (ServiceException e){
            throw new ServiceException("Ошибка при обновлении имени ТБ", e);
        }
    }

    @Override
    public void updateFDDepartmentNames(int depTBId, String depName, Date dateFrom, Date dateTo, TAUserInfo user) {
        if (dateFrom == null)
            throw new ServiceException("Должна быть установлена хотя бы \"Дата от\"");
        try {
            List<Long> formDataIds = formPerformerDao.getFormDataId(depTBId, dateFrom, dateTo);
            for(Long formDataId: formDataIds)
                deleteReport(formDataId, null, user.getUser().getId());
            formDataDao.updateFDPerformerDepartmentNames(depTBId, depName, dateFrom, dateTo);
        } catch (ServiceException e){
            throw new ServiceException("Ошибка при обновлении имени ТБ", e);
        }
    }

    @Override
    public Integer getPreviousRowNumber(FormData formData, FormData savingFormData) {
        int previousRowNumber = 0;
        // Отчетный период подразделения
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        // Налоговый период
        TaxPeriod taxPeriod = departmentReportPeriod.getReportPeriod().getTaxPeriod();
        // Получить упорядоченный список экземпляров НФ, которые участвуют в сквозной нумерации и находятся до указанного экземпляра НФ
        List<FormData> formDataList = formDataDao.getPrevFormDataList(formData, taxPeriod);

        // Если экземпляр НФ является не первым экземпляром в сквозной нумерации
        if (formDataList.size() > 0) {
            for (FormData aFormData : formDataList) {
                if (beInOnAutoNumeration(aFormData.getState(), departmentReportPeriod)) {
                    previousRowNumber += dataRowDao.getSizeWithoutTotal(aFormData, savingFormData != null && (aFormData.getId().equals(savingFormData.getId())));
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
    void updatePreviousRowNumberAttr(FormData formData, Logger logger, TAUserInfo user) {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        if (!formData.isManual() && beInOnAutoNumeration(formData.getState(), departmentReportPeriod)
                && dataRowDao.isDataRowsCountChanged(formData)) {
            updatePreviousRowNumber(formData, logger, user, true);
        }
    }

    /**
     * Обновление значений атрибута "Номер последней строки предыдущей НФ" при переходе между ЖЦ
     *
     * @param workflowMove переход по ЖЦ
     * @param logger       логгер для регистрации ошибок
     * @param formData     редактируемый экземпляр НФ
     */
    public void updatePreviousRowNumberAttr(FormData formData, WorkflowMove workflowMove, Logger logger, TAUserInfo user) {
        if (canUpdatePreviousRowNumberWhenDoMove(workflowMove)) {
            updatePreviousRowNumber(formData, logger, user, false);
        }
    }

    @Override
    public void updatePreviousRowNumber(FormData formData, TAUserInfo user) {
        updatePreviousRowNumber(formData, null, user, false);
    }

    @Override
    public void updatePreviousRowNumber(FormData formData, Logger logger, TAUserInfo user, boolean isSave) {
        FormTemplate formTemplate = formTemplateService.get(formData.getFormTemplateId());
        updatePreviousRowNumber(formData, formTemplate, logger, user, isSave);
    }

    @Override
    public void updatePreviousRowNumber(FormData formData, FormTemplate formTemplate, Logger logger, TAUserInfo user, boolean isSave) {
        String msg = null;

        if (formTemplateService.isAnyAutoNumerationColumn(formTemplate, NumerationType.CROSS)) {
            // Получить налоговый период
            TaxPeriod taxPeriod = reportPeriodService.getReportPeriod(formData.getReportPeriodId()).getTaxPeriod();
            // Получить список экземпляров НФ следующих периодов
            List<FormData> formDataList = formDataDao.getNextFormDataList(formData, taxPeriod);

            // Устанавливаем значение для текущего экземпляра НФ
            formDataDao.updatePreviousRowNumber(formData.getId(), getPreviousRowNumber(formData, null));

            StringBuilder stringBuilder = new StringBuilder();
            // Обновляем последующие периоды
            int size = formDataList.size();

            for (FormData data : formDataList) {
                formDataDao.updatePreviousRowNumber(data.getId(), getPreviousRowNumber(data, isSave ? formData : null));
                deleteReport(data.getId(), null, user.getUser().getId());
                ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(data.getReportPeriodId());
                stringBuilder.append(reportPeriod.getName()).append(" ").append(reportPeriod.getTaxPeriod().getYear());
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
        ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(reportPeriodId);
        return formDataDao.getManualInputForms(departments, reportPeriodId, taxType, kind, reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
    }

    @Override
    public void batchUpdatePreviousNumberRow(FormTemplate formTemplate, TAUserInfo user) {
        List<FormData> formDataList = formDataDao.getFormDataListByTemplateId(formTemplate.getId());
        for (FormData formData : formDataList) {
            updatePreviousRowNumber(formData, formTemplate, null, user, false);
        }
    }

    @Override
    public FormData getLast(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder) {
        return formDataDao.getLast(formTypeId, kind, departmentId, reportPeriodId, periodOrder);
    }

    @Override
    public List<FormData> getLastList(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder) {
        return formDataDao.getLastListByDate(formTypeId, kind, departmentId, reportPeriodId, periodOrder, null);
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
     * Экземпляры в статусе "Создана" или в корр. периоде не участвуют в сквозной нумерации.
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

    @Override
    public void checkLockedByTask(long formDataId, Logger logger, TAUserInfo userInfo, String taskName, boolean editMode) {
        Pair<ReportType, LockData> lockType = getLockTaskType(formDataId);
        if (lockType != null &&
                !(editMode && ReportType.EDIT_FD.equals(lockType.getFirst()) && lockType.getSecond().getUserId() == userInfo.getUser().getId())) {
            logger.error("\"%s\" пользователем \"%s\" запущена операция \"%s\"",
                    SDF_HH_MM_DD_MM_YYYY.format(lockType.getSecond().getDateLock()),
                    userService.getUser(lockType.getSecond().getUserId()).getName(),
                    getTaskName(lockType.getFirst(), formDataId, userInfo));
            throw new ServiceLoggerException(LOCK_MESSAGE_TASK,
                    logEntryService.save(logger.getEntries()), taskName);
        }
    }

    void checkLockedMe(LockData lockData, TAUser user){
		if (lockData == null) {
			throw new ServiceException("Блокировка не найдена. Объект должен быть заблокирован текущим пользователем");
		}
        if (lockData.getUserId() != user.getId()) {
            throw new ServiceException(String.format("Объект заблокирован другим пользователем (\"%s\", срок \"%s\")",
					userService.getUser(lockData.getUserId()).getLogin(), SDF_HH_MM_DD_MM_YYYY.format(lockData.getDateBefore())));
        }
		// продлеваем пользовательскую блокировку
		lockService.extend(lockData.getKey(), user.getId(),
                lockService.getLockTimeout(LockData.LockObjects.FORM_DATA));
    }

    private List<String> generateReportKeys(ReportType reportType, long formDataId, Boolean manual) {
        List<String> lockKeys = new ArrayList<String>();
        boolean[] b = {false, true};
        for (boolean showChecked : b) {
            for(boolean saved : b) {
                if (manual != null) {
                    lockKeys.add(String.format("%s_%s_%s_isShowChecked_%s_manual_%s_saved_%s", LockData.LockObjects.FORM_DATA.name(), formDataId, reportType.getName(), showChecked, manual, saved));
                } else {
                    for(boolean manual1: b) {
                        lockKeys.add(String.format("%s_%s_%s_isShowChecked_%s_manual_%s_saved_%s", LockData.LockObjects.FORM_DATA.name(), formDataId, reportType.getName(), showChecked, manual1, saved));
                    }
                }
            }
        }
        return lockKeys;
    }

    @Override
    public void deleteReport(long formDataId, Boolean manual, int userId) {
        List<String> lockKeys = new ArrayList<String>();
        boolean[] b = {false, true};
        ReportType[] reportTypes = {ReportType.CSV, ReportType.EXCEL};
        for (ReportType reportType: reportTypes) {
            lockKeys.addAll(generateReportKeys(reportType, formDataId, manual));
        }
        lockService.interuptAllTasks(lockKeys, userId);
        reportService.delete(formDataId, manual);
    }

    @Override
    public void findFormDataIdsByRangeInReportPeriod(int formTemplateId, Date startDate, Date endDate, Logger logger) {
        List<Integer> fdIds = formDataDao.findFormDataIdsByRangeInReportPeriod(formTemplateId,
                startDate, endDate != null ? endDate : MAX_DATE);
        for (Integer id : fdIds){
            FormData fd = formDataDao.getWithoutRows(id);
            ReportPeriod rp = reportPeriodService.getReportPeriod(fd.getReportPeriodId());
            DepartmentReportPeriod drp = departmentReportPeriodService.get(fd.getDepartmentReportPeriodId());
            FormTemplate ft = formTemplateService.get(fd.getFormTemplateId());
            logger.error(FD_NOT_IN_RANGE,
                    rp.getName() + (fd.getPeriodOrder() != null?" " + Months.fromId(fd.getPeriodOrder()).getTitle():""),
                    rp.getTaxPeriod().getYear(),
                    drp.getCorrectionDate() != null ? String.format("с датой сдачи корректировки %s",
                            SDF_DD_MM_YYYY.format(drp.getCorrectionDate())) : "",
                    ft.getName(),
                    departmentService.getDepartment(fd.getDepartmentId()).getName(),
                    fd.getState().getName());
        }
    }

    @Override
    public String generateTaskKey(long formDataId, ReportType reportType) {
        if (reportType == null) {
            return LockData.LockObjects.FORM_DATA.name() + "_" + formDataId;
        }
        return LockData.LockObjects.FORM_DATA.name() + "_" + formDataId + "_" + reportType.getName();
    }

    @Override
    public void interruptTask(long formDataId, TAUserInfo userInfo, List<ReportType> reportTypes) {
        List<String> lockKeys = new ArrayList<String>();
        for(ReportType reportType: reportTypes)
            lockKeys.add(generateTaskKey(formDataId, reportType));
        lockService.interuptAllTasks(lockKeys, userInfo.getUser().getId());
    }


    @Override
    public Pair<ReportType, LockData> getLockTaskType(long formDataId) {
        ReportType[] reportTypes = {ReportType.MOVE_FD, ReportType.CONSOLIDATE_FD, ReportType.IMPORT_TF_FD, ReportType.CALCULATE_FD, ReportType.IMPORT_FD, ReportType.EDIT_FD};
        for (ReportType reportType: reportTypes) {
            LockData lockData = lockService.getLock(generateTaskKey(formDataId, reportType));
            if (lockData != null)
                return new Pair<ReportType, LockData>(reportType, lockData);
        }
        return null;
    }

    @Override
    public void locked(long formDataId, ReportType reportType, Pair<ReportType, LockData> lockType, Logger logger) {
        TAUser user = userService.getUser(lockType.getSecond().getUserId());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(user);
        String msg = "";
        switch (reportType) {
            case CONSOLIDATE_FD:
                logger.error(
                        String.format(
                                LOCK_CURRENT_1,
                                user.getName(),
                                SDF_HH_MM_DD_MM_YYYY.format(lockType.getSecond().getDateLock()),
                                getTaskName(lockType.getFirst(), formDataId, userInfo))
                );
                break;
            default:
                logger.error(
                        String.format(
                                LockData.LOCK_CURRENT,
                                SDF_HH_MM_DD_MM_YYYY.format(lockType.getSecond().getDateLock()),
                                user.getName(),
                                getTaskName(lockType.getFirst(), formDataId, userInfo))
                );
        }
        switch (reportType) {
            case EXCEL:
            case CSV:
                msg = "Для текущего экземпляра налоговой формы запущены операции, при которых формирование отчета невозможно";
                break;
            case CHECK_FD:
                msg = "Для текущего экземпляра налоговой формы запущены операции, при которых ее проверка невозможна";
                break;
            case MOVE_FD:
                msg = "Для текущего экземпляра налоговой формы запущены операции, при которых изменение его состояния невозможно";
                break;
            case CALCULATE_FD:
                msg = String.format("Выполнение операции \"%s\" невозможно, т.к. для текущего экземпляра налоговой формы запущена операция \"%s\". Расчет данных невозможен", getTaskName(reportType, formDataId, userInfo), getTaskName(lockType.getFirst(), formDataId, userInfo));
                break;
            case IMPORT_FD:
                msg = String.format("Выполнение операции \"%s\" невозможно, т.к. для текущего экземпляра налоговой формы запущена операция \"%s\". Загрузка данных из файла невозможна", getTaskName(reportType, formDataId, userInfo), getTaskName(lockType.getFirst(), formDataId, userInfo));
                break;
            case CONSOLIDATE_FD:
                msg = "";
                break;
        }
        throw new ServiceLoggerException(msg, logEntryService.save(logger.getEntries()));
    }

    @Override
    public Pair<BalancingVariants, Long> checkTaskLimit(TAUserInfo userInfo, FormData formData, ReportType reportType, String uuid) {
        switch (reportType) {
            case CHECK_FD:
            case MOVE_FD:
            case CALCULATE_FD:
                int rowCount = dataRowDao.getSavedSize(formData);
                int columnCount = formTemplateService.get(formData.getFormTemplateId()).getColumns().size();
                long cellCount = rowCount * columnCount;
                AsyncTaskTypeData taskTypeData = asyncTaskTypeDao.get(reportType.getAsyncTaskTypeId(true));
                if (cellCount < taskTypeData.getShortQueueLimit()) {
                    return new Pair<BalancingVariants, Long>(BalancingVariants.SHORT, cellCount);
                }
                return new Pair<BalancingVariants, Long>(BalancingVariants.LONG, cellCount);
            case EXCEL:
            case CSV:
                int rowCountReport = dataRowDao.getSavedSize(formData);
                int columnCountReport = formTemplateService.get(formData.getFormTemplateId()).getColumns().size();
                long cellCountReport = rowCountReport * columnCountReport;
                AsyncTaskTypeData taskTypeDataReport = asyncTaskTypeDao.get(reportType.getAsyncTaskTypeId(true));
                if (cellCountReport > taskTypeDataReport.getTaskLimit()) {
                    return new Pair<BalancingVariants, Long>(null, cellCountReport);
                } else if (cellCountReport < taskTypeDataReport.getShortQueueLimit()) {
                    return new Pair<BalancingVariants, Long>(BalancingVariants.SHORT, cellCountReport);
                }
                return new Pair<BalancingVariants, Long>(BalancingVariants.LONG, cellCountReport);
            case CONSOLIDATE_FD:
                ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(formData.getReportPeriodId());
                List<DepartmentFormType> departmentFormTypesSources = departmentFormTypeDao.getFormSources(
                        formData.getDepartmentId(),
                        formData.getFormType().getId(),
                        formData.getKind(),
                        reportPeriod.getCalendarStartDate(),
                        reportPeriod.getEndDate());
                long cellCountSource = 0;
                for (DepartmentFormType sourceDFT : departmentFormTypesSources){
                    // Последний отчетный период подразделения
                    DepartmentReportPeriod sourceDepartmentReportPeriod =
                            departmentReportPeriodService.getLast(sourceDFT.getDepartmentId(), formData.getReportPeriodId());
                    if (sourceDepartmentReportPeriod == null) {
                        continue;
                    }
                    FormData sourceForm = findFormData(sourceDFT.getFormTypeId(), sourceDFT.getKind(),
                            sourceDepartmentReportPeriod.getId(), formData.getPeriodOrder());
                    if (sourceForm == null){
                        continue;
                    }
                    //Запись на будущее, чтобы второго цикла не делать
                    //1E.
                    if (sourceForm.getState() == WorkflowState.ACCEPTED){
                        int rowCountSource = dataRowDao.getSavedSize(formData);
                        int columnCountSource = formTemplateService.get(formData.getFormTemplateId()).getColumns().size();
                        cellCountSource += rowCountSource * columnCountSource;
                    }
                }
                AsyncTaskTypeData taskTypeConsolidate = asyncTaskTypeDao.get(reportType.getAsyncTaskTypeId(true));
                if (cellCountSource < taskTypeConsolidate.getShortQueueLimit()) {
                    return new Pair<BalancingVariants, Long>(BalancingVariants.SHORT, cellCountSource);
                }
                return new Pair<BalancingVariants, Long>(BalancingVariants.LONG, cellCountSource);
            case IMPORT_FD:
                Long fileSize = blobDataService.getLength(uuid);
                AsyncTaskTypeData taskTypeDataImport = asyncTaskTypeDao.get(reportType.getAsyncTaskTypeId(true));
                Long maxSize = taskTypeDataImport.getTaskLimit() * 1024;
                Long shortSize = taskTypeDataImport.getShortQueueLimit() * 1024;
                if (maxSize != 0 && fileSize > maxSize) {
                    return new Pair<BalancingVariants, Long>(null, fileSize);
                } else if (fileSize < shortSize) {
                    return new Pair<BalancingVariants, Long>(BalancingVariants.SHORT, fileSize);
                }
                return new Pair<BalancingVariants, Long>(BalancingVariants.LONG, fileSize);
            default:
                throw new ServiceException("Неверный тип отчета(%s)", reportType.getName());
        }
    }

    @Override
    public String getTaskName(ReportType reportType, long formDataId, TAUserInfo userInfo) {
        FormData formData = formDataDao.get(formDataId, false);
        switch (reportType) {
            case EDIT_FD:
            case CALCULATE_FD:
            case CONSOLIDATE_FD:
            case CHECK_FD:
                return String.format(reportType.getDescription(), formData.getFormType().getTaxType().getTaxText());
            case EXCEL:
            case CSV:
            case IMPORT_FD:
            case IMPORT_TF_FD:
            case MOVE_FD:
                return reportType.getDescription();
            default:
                throw new ServiceException("Неверный тип отчета(%s)", reportType.getName());
        }
    }

    /**
     * Список операции, по которым требуется удалить блокировку
     * @param reportType
     * @return
     */
    private ReportType[] getCheckTaskList(ReportType reportType) {
        switch (reportType) {
            case CONSOLIDATE_FD:
                return new ReportType[]{ReportType.CHECK_FD, ReportType.CALCULATE_FD, ReportType.IMPORT_FD, ReportType.IMPORT_TF_FD, ReportType.EXCEL, ReportType.CSV};
            case CALCULATE_FD:
                return new ReportType[]{ReportType.CHECK_FD, ReportType.EXCEL, ReportType.CSV};
            case IMPORT_FD:
                return new ReportType[]{ReportType.CHECK_FD, ReportType.EXCEL, ReportType.CSV};
            case MOVE_FD:
                return new ReportType[]{ReportType.CHECK_FD};
            case CHECK_FD:
            default:
                return null;
        }
    }

    @Override
    public boolean checkExistTask(long formDataId, boolean manual, ReportType reportType, Logger logger, TAUserInfo userInfo) {
        ReportType[] reportTypes = getCheckTaskList(reportType);
        if (reportTypes == null) return false;
        boolean exist = false;
        Boolean manualReport;
        switch (reportType) {
            case CALCULATE_FD:
            case IMPORT_FD:
                manualReport = manual;
                break;
            default:
                manualReport = null;
        }
        for (ReportType reportType1: reportTypes) {
            List<String> taskKeyList = new ArrayList<String>();
            if (ReportType.CSV.equals(reportType1) || ReportType.EXCEL.equals(reportType1)) {
                taskKeyList.addAll(generateReportKeys(reportType1, formDataId, manualReport));
            } else {
                taskKeyList.add(generateTaskKey(formDataId, reportType1));
            }
            for(String key: taskKeyList) {
                LockData lock = lockService.getLock(key);
                if (lock != null) {
                    exist = true;
                    if (LockData.State.IN_QUEUE.getText().equals(lock.getState())) {
                        logger.info(LockData.CANCEL_TASK_NOT_PROGRESS,
                                SDF_DD_MM_YYYY_HH_MM_SS.format(lock.getDateLock()),
                                userService.getUser(lock.getUserId()).getName(),
                                getTaskName(reportType1, formDataId, userInfo));
                    } else {
                        logger.info(LockData.CANCEL_TASK_IN_PROGRESS,
                                SDF_DD_MM_YYYY_HH_MM_SS.format(lock.getDateLock()),
                                userService.getUser(lock.getUserId()).getName(),
                                getTaskName(reportType1, formDataId, userInfo));
                    }
                }
            }
        }
        return exist;
    }

    @Override
    public void interruptTask(long formDataId, boolean manual, int userId, ReportType reportType) {
        ReportType[] reportTypes = getCheckTaskList(reportType);
        if (reportTypes == null) return;
        Boolean manualReport;
        switch (reportType) {
            case CALCULATE_FD:
            case IMPORT_FD:
                manualReport = manual;
                break;
            default:
                manualReport = null;
        }
        for (ReportType reportType1: reportTypes) {
            List<String> taskKeyList = new ArrayList<String>();
            if (ReportType.CSV.equals(reportType1) || ReportType.EXCEL.equals(reportType1)) {
                taskKeyList.addAll(generateReportKeys(reportType1, formDataId, manualReport));
                reportService.delete(formDataId, manual);
            } else {
                taskKeyList.add(generateTaskKey(formDataId, reportType1));
            }
            for(String key: taskKeyList) {
                LockData lock = lockService.getLock(key);
                if (lock != null) {
                    lockService.interruptTask(lock, userId, true);
                }
            }
        }
    }
}
