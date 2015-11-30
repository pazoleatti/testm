package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataFileDao;
import com.aplana.sbrf.taxaccounting.dao.FormPerformerDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.CheckResult;
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

	private static final Log LOG = LogFactory.getLog(FormDataServiceImpl.class);

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
    public static final String MSG_IS_EXIST_FORM = "Существует экземпляр %s:";
    final static String LOCK_MESSAGE = "Форма заблокирована и не может быть изменена. Попробуйте выполнить операцию позже.";
    final static String LOCK_MESSAGE_TASK = "Выполнение операции \"%s\" невозможно, т.к. для текущего экземпляра налоговой формы запущена операция изменения данных";
    final static String LOCK_REFBOOK_MESSAGE = "Справочник \"%s\" заблокирован и не может быть использован для заполнения атрибутов формы. Попробуйте выполнить операцию позже.";
    final static String REF_BOOK_LINK =  "Строка %s%s \"%s\"%s";
    final static String DEPARTMENT_REPORT_PERIOD_NOT_FOUND_ERROR = "Не найден отчетный период подразделения с id = %d.";
    private static final String SAVE_ERROR = "Найдены ошибки при сохранении формы!";
    private static final String SORT_ERROR = "Найдены ошибки при сортировке строк формы!";
    private static final String FD_NOT_IN_RANGE = "Найдена форма: ";
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
    private BlobDataService blobDataService;
    @Autowired
    private FormDataFileDao formDataFileDao;

    @Override
    public long createFormData(Logger logger, TAUserInfo userInfo, int formTemplateId, int departmentReportPeriodId, Integer comparativePeriodId, boolean accruing, FormDataKind kind, Integer periodOrder, boolean importFormData) {
        formDataAccessService.canCreate(userInfo, formTemplateId, kind, departmentReportPeriodId, comparativePeriodId, accruing);
        return createFormDataWithoutCheck(logger, userInfo, formTemplateId, departmentReportPeriodId, comparativePeriodId, accruing, kind, periodOrder, importFormData);
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
        try {
            LOG.info(String.format("Создание временного файла: %s", key));
            if (stateLogger != null) {
                stateLogger.updateState("Создание временного файла");
            }
            dataFile = File.createTempFile("dataFile", ".original");
			OutputStream dataFileOutputStream = new BufferedOutputStream(new FileOutputStream(dataFile));
			try {
				IOUtils.copy(inputStream, dataFileOutputStream);
			} finally {
				IOUtils.closeQuietly(dataFileOutputStream);
			}

            String ext = getFileExtension(fileName);

            // Проверка ЭП
            // Если флаг проверки отсутствует или не равен «1», то файл считается проверенным
            boolean check = false;
            // исключить проверку ЭП для файлов эксель
            if (!ext.equals(XLS_EXT) && !ext.equals(XLSX_EXT) && !ext.equals(XLSM_EXT)) {
                List<String> signList = configurationDao.getByDepartment(0).get(ConfigurationParam.SIGN_CHECK, 0);
                if (signList != null && !signList.isEmpty() && SignService.SIGN_CHECK.equals(signList.get(0))) {
					try {
						LOG.info(String.format("Проверка ЭП: %s", key));
						check = signService.checkSign(dataFile.getAbsolutePath(), 0);
					} catch (Exception e) {
						logger.error("Ошибка при проверке ЭП: " + e.getMessage());
					}
					if (!check) {
						logger.error("Ошибка проверки цифровой подписи");
					}
                } else {
                    check = true;
                }
            } else {
                check = true;
            }

            FormData fd = formDataDao.get(formDataId, isManual);

            if (check) {
				InputStream dataFileInputStream = new BufferedInputStream(new FileInputStream(dataFile));
				try {
					Map<String, Object> additionalParameters = new HashMap<String, Object>();
					additionalParameters.put("ImportInputStream", dataFileInputStream);
					additionalParameters.put("UploadFileName", fileName);
					if (stateLogger != null) {
						stateLogger.updateState("Импорт XLSM-файла");
					}
					LOG.info(String.format("Выполнение скрипта: %s", key));
					formDataScriptingService.executeScript(userInfo, fd, formDataEvent, logger, additionalParameters);
				} finally {
					IOUtils.closeQuietly(dataFileInputStream);
				}
			}

            if (logger.containsLevel(LogLevel.ERROR)) {
                if (stateLogger != null) {
                    stateLogger.updateState("Сохранение ошибок");
                }
                LOG.info(String.format("Сохранение ошибок: %s", key));
                String uuid = logEntryService.save(logger.getEntries());
                throw new ServiceLoggerException("Есть критические ошибки при выполнении скрипта", uuid);
            } else if (isInner) {
                logger.info("Данные загружены");
            }
            formDataDao.updateSorted(fd.getId(), false);
            formDataDao.updateEdited(fd.getId(), true);
			dataRowDao.refreshRefBookLinks(fd);
            logBusinessService.add(formDataId, null, userInfo, formDataEvent, null);
            auditService.add(formDataEvent, userInfo, null, fd, fileName, null);
        } catch (IOException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        } finally {
            if (dataFile != null) {
                dataFile.delete();
            }
        }
    }

    private static String getFileExtension(String filename){
        int dotPos = filename.lastIndexOf('.') + 1;
        return filename.substring(dotPos);
    }


    @Override
	public long createFormDataWithoutCheck(Logger logger, TAUserInfo userInfo, int formTemplateId, int departmentReportPeriodId,
                                           Integer comparativePeriodId, boolean accruing,
                                           FormDataKind kind, Integer periodOrder, boolean importFormData) {
		FormTemplate formTemplate = formTemplateService.get(formTemplateId);
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
        formData.setSorted(false);
        formData.setComparativePeriodId(comparativePeriodId);
        formData.setAccruing(accruing);

        FormDataPerformer performer = null;
        FormData formDataOld = getPrevPeriodFormData(formTemplate, departmentReportPeriod, kind, periodOrder,
                comparativePeriodId, accruing);
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
					String.format(
                            "Произошли ошибки в скрипте создания %s", MessageGenerator.mesSpeckSingleD(formData.getFormType().getTaxType())),
                    logEntryService.save(logger.getEntries()));
		}
		formDataDao.save(formData);

        if (!importFormData) {
            logBusinessService.add(formData.getId(), null, userInfo, FormDataEvent.CREATE, null);
            auditService.add(FormDataEvent.CREATE, userInfo, null, formData, "Форма создана", null);
        }

		dataRowDao.saveRows(formData, formTemplate.getRows());

        // Execute scripts for the form event AFTER_CREATE
        formDataScriptingService.executeScript(userInfo, formData, FormDataEvent.AFTER_CREATE, logger, null);
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(
                    "Произошли ошибки в скрипте после создания налоговой формы",
                    logEntryService.save(logger.getEntries()));
        }
		dataRowDao.refreshRefBookLinks(formData);
        updatePreviousRowNumber(formData, userInfo);
        return formData.getId();
	}

    /**
     * Получение налоговой формы из предыдущего отчетного периода (для ежемесячных форм поиск ведется в текущем периоде, если это не первый месяц периода)
     * @param formTemplate
     * @param departmentReportPeriod
     * @param kind
     * @param periodOrder
     * @param comparativePeriodId Период сравнения - ссылка на DepartmentReportPeriod. Может быть null
     * @param accruing Признак расчета значений нарастающим итогом (false - не нарастающим итогом, true - нарастающим итогом, пустое - форма без периода сравнения)
     * @return
     */
    public FormData getPrevPeriodFormData(FormTemplate formTemplate, DepartmentReportPeriod departmentReportPeriod,
                                          FormDataKind kind, Integer periodOrder,
                                          Integer comparativePeriodId, boolean accruing) {
        if (periodOrder != null) {
            List<Months> availableMonthList = reportPeriodService.getAvailableMonthList(departmentReportPeriod.getReportPeriod().getId());
            if  (periodOrder > 1 && availableMonthList.contains(Months.fromId(periodOrder - 1))) {
                return formDataDao.find(formTemplate.getType().getId(), kind,
                        departmentReportPeriod.getId(), periodOrder - 1,
                        comparativePeriodId, accruing);
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
                return formDataDao.find(formTemplate.getType().getId(), kind,
                        departmentReportPeriodOld.getId(), lastPeriodOrder,
                        comparativePeriodId, accruing);
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

        formDataDao.updateSorted(formData.getId(), false);
        formDataDao.updateEdited(formData.getId(), true);

		Map<String, Object> additionalParameters = new HashMap<String, Object>();
		additionalParameters.put("currentDataRow", currentDataRow);
		formDataScriptingService.executeScript(userInfo, formData, FormDataEvent.ADD_ROW, logger, additionalParameters);
		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException("Произошли ошибки в скрипте добавления новой строки", logEntryService.save(logger.getEntries()));
		}
		dataRowDao.refreshRefBookLinks(formData);
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

        formDataDao.updateSorted(formData.getId(), false);
        formDataDao.updateEdited(formData.getId(), true);

		Map<String, Object> additionalParameters = new HashMap<String, Object>();
		additionalParameters.put("currentDataRow", currentDataRow);
		formDataScriptingService.executeScript(userInfo, formData, FormDataEvent.DELETE_ROW, logger, additionalParameters);
		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException("Произошли ошибки в скрипте удаления строки", logEntryService.save(logger.getEntries()));
		}
		dataRowDao.refreshRefBookLinks(formData);
	}

	/**
	 * Выполнить расчёты по налоговой форме
	 *
	 * @param logger логгер-объект для фиксации диагностических сообщений
	 * @param userInfo информация о пользователе, запросившего операцию
	 * @param formData объект с данными по налоговой форме
	 */
	@Override
	public void doCalc(Logger logger, TAUserInfo userInfo, FormData formData) {
		formDataAccessService.canEdit(userInfo, formData.getId(), formData.isManual());
        formDataDao.updateSorted(formData.getId(), false);
        formDataDao.updateEdited(formData.getId(), true);
        formDataScriptingService.executeScript(userInfo, formData, FormDataEvent.CALCULATE, logger, null);
        if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceException("Найдены ошибки при выполнении расчета формы");
		} else {
			logger.info("Расчет завершен, фатальных ошибок не обнаружено");
		}
		dataRowDao.refreshRefBookLinks(formData);
//        logBusinessService.add(formData.getId(), null, userInfo, FormDataEvent.CALCULATE, null);
    }

    /**
     * Выполнить обновление по налоговой форме
     *
     * @param logger логгер-объект для фиксации диагностических сообщений
     * @param userInfo информация о пользователе, запросившего операцию
     * @param formData объект с данными по налоговой форме
     */
    @Override
    public void doRefresh(Logger logger, TAUserInfo userInfo, FormData formData) {
        formDataAccessService.canEdit(userInfo, formData.getId(), formData.isManual());
        formDataDao.updateSorted(formData.getId(), false);
        formDataDao.updateEdited(formData.getId(), true);
        formDataScriptingService.executeScript(userInfo, formData, FormDataEvent.REFRESH, logger, null);
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceException("");
        }
        dataRowDao.refreshRefBookLinks(formData);
        //logBusinessService.add(formData.getId(), null, userInfo, FormDataEvent.REFRESH, null);
    }

	@Override
    @Transactional
	public void doCheck(final Logger logger, final TAUserInfo userInfo, final FormData formData, final boolean editMode) {
		tx.executeInNewReadOnlyTransaction(new TransactionLogic<Object>() {
			@Override
			public Object execute() {
                boolean consolidationOk = true;
				formDataAccessService.canRead(userInfo, formData.getId());
				formDataScriptingService.executeScript(userInfo, formData, FormDataEvent.CHECK, logger, null);
				checkPerformer(logger, formData);
				//Проверка на неактуальные консолидированные данные
				if (!sourceService.isFDConsolidationTopical(formData.getId())){
					logger.warn(CONSOLIDATION_NOT_TOPICAL);
                    consolidationOk = false;
				} else {
                    if (formData.getState() == WorkflowState.ACCEPTED) {
                        // Система проверяет, существует ли экземпляр формы-приёмника, консолидация в который не была выполнена.
                        List<Relation> relations = sourceService.getDestinationsInfo(formData, true, true, null, userInfo, logger);
                        for (Relation relation : relations) {
                            if (!sourceService.isFDSourceConsolidated(relation.getFormDataId(), formData.getId())){
                                consolidationOk = false;
                                logger.warn(
                                        NOT_CONSOLIDATE_DESTINATION_FORM_WARNING,
                                        relation.getFullDepartmentName(),
                                        relation.getFormTypeName(),
                                        relation.getFormDataKind().getTitle(),
                                        relation.getPeriodName() + (relation.getMonth() != null?" " + Months.fromId(relation.getMonth()).getTitle():""),
                                        relation.getYear(),
                                        relation.getCorrectionDate() != null ? String.format("с датой сдачи корректировки %s",
                                                SDF_DD_MM_YYYY.format(relation.getCorrectionDate())) : ""
                                );
                            }
                        }
                    }
                    //Система проверяет статус консолидации из форм-источников.
                    // получаем экземпляры-источники
                    List<Relation> relations = sourceService.getSourcesInfo(formData, true, false, null, userInfo, logger);
                    for (Relation relation : relations){
                        if (relation.getFormDataId() == null){
                            consolidationOk = false;
                            logger.warn(
                                    NOT_EXIST_SOURCE_FORM_WARNING,
                                    relation.getFullDepartmentName(),
                                    relation.getFormTypeName(),
                                    relation.getFormDataKind().getTitle(),
                                    relation.getPeriodName() + (relation.getMonth() != null ? " " + Months.fromId(relation.getMonth()).getTitle() : ""),
                                    relation.getYear(),
                                    relation.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s",
                                            SDF_DD_MM_YYYY.format(relation.getCorrectionDate())) : ""
                            );
                        } else if (!sourceService.isFDSourceConsolidated(formData.getId(), relation.getFormDataId())){
                            consolidationOk = false;
                            logger.warn(
                                    NOT_CONSOLIDATE_SOURCE_FORM_WARNING,
                                    relation.getFullDepartmentName(),
                                    relation.getFormTypeName(),
                                    relation.getFormDataKind().getTitle(),
                                    relation.getPeriodName() + (relation.getMonth() != null ? " " + Months.fromId(relation.getMonth()).getTitle() : ""),
                                    relation.getYear(),
                                    relation.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s",
                                            SDF_DD_MM_YYYY.format(relation.getCorrectionDate())) : "",
                                    relation.getState().getTitle()
                            );
                        }
                    }

                    if (!relations.isEmpty() && consolidationOk){
                        logger.info("Консолидация выполнена из всех форм-источников");
                    }
                }

                //Проверка справочных значений
                checkReferenceValues(logger, formData, false);

                if (logger.containsLevel(LogLevel.ERROR)){
                    throw new ServiceLoggerException("", logEntryService.save(logger.getEntries()));
                }

				logger.info("Проверка завершена, фатальных ошибок не обнаружено");
				return null;
			}
		});
    }

    private void checkPerformer(Logger logger, FormData formData) {
        boolean check = true;
        if (formData.getPerformer() == null || formData.getPerformer().getPrintDepartmentId() == null || formData.getSigners().isEmpty() ||
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

        deleteReport(formData.getId(), null, userInfo.getUser().getId(), "Изменены параметры печатной формы");
    }

    /**
	 * Сохранить данные по налоговой форме
	 *
	 * @param userInfo информация о пользователе, выполняющего операцию
	 * @param formData объект с данными налоговой формы
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
        // Отработка скриптом события сохранения
		formDataScriptingService.executeScript(userInfo, formData, FormDataEvent.SAVE, logger, null);
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(SAVE_ERROR, logEntryService.save(logger.getEntries()));
        }
        // Обновление для сквозной нумерации
        updateAutoNumeration(formData, logger, userInfo);
		// Обновление
        formDataDao.save(formData);
		dataRowDao.refreshRefBookLinks(formData);
        deleteReport(formData.getId(), formData.isManual(), userInfo.getUser().getId(), "Изменены данные налоговой формы");
        // ЖА и история изменений
		logBusinessService.add(formData.getId(), null, userInfo, FormDataEvent.SAVE, null);
		auditService.add(FormDataEvent.SAVE, userInfo, null, formData, "Форма сохранена", null);
		return formData.getId();
	}

	/**
	 * Получить данные по налоговой форме
	 *
	 *
     * @param userInfo информация о пользователе, выполняющего операцию
     * @param formDataId идентификатор записи, которую необходимо считать
     * @param manual
     * @param logger логгер-объект для фиксации диагностических сообщений
	 * @return объект с данными по налоговой форме
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException
	 *             если у пользователя нет прав просматривать налоговую форму с
	 *             такими параметрами
	 */
	@Override
	@Transactional
	public FormData getFormData(TAUserInfo userInfo, long formDataId, boolean manual, Logger logger) {
		formDataAccessService.canRead(userInfo, formDataId);
		FormData formData = formDataDao.get(formDataId, manual);
		formDataScriptingService.executeScript(userInfo, formData, FormDataEvent.AFTER_LOAD, logger, null);
		dataRowDao.refreshRefBookLinks(formData);
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
        ReportType reportType = ReportType.DELETE_FD;
        LockData lockDataCheck = lockService.getLock(generateTaskKey(formDataId, ReportType.CHECK_FD));
        Pair<ReportType, LockData> lockType = getLockTaskType(formDataId);
        if (!manual && (lockDataCheck != null || lockType != null)) {
            locked(formDataId, ReportType.DELETE_FD, lockType != null ? lockType : new Pair<ReportType, LockData>(ReportType.CHECK_FD, lockDataCheck), logger);
        }

        String keyTask = generateTaskKey(formDataId, reportType);
        if (lockService.lock(keyTask, userInfo.getUser().getId(),
                getFormDataFullName(formDataId, false, null, reportType)) == null) {
            try {
                FormData formData = formDataDao.get(formDataId, manual);
                if (manual) {
                    formDataAccessService.canDeleteManual(logger, userInfo, formDataId);
                    formDataDao.deleteManual(formData);
                    deleteReport(formDataId, true, userInfo.getUser().getId(), "Удалена версия ручного ввода для налоговой формы");
                } else {
                    formDataAccessService.canDelete(userInfo, formDataId);
                    sourceService.deleteFDConsolidationInfo(Arrays.asList(formDataId));
                    formDataDao.delete(formData.getFormTemplateId(), formDataId);
                    interruptTask(formDataId, false, userInfo.getUser().getId(), reportType, "Удалена налоговая форма");
                    auditService.add(FormDataEvent.DELETE, userInfo, null, formData, "Форма удалена", null);
                }
            } finally {
                lockService.unlock(keyTask, userInfo.getUser().getId());
            }
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
    public void doMove(long formDataId, boolean manual, TAUserInfo userInfo, WorkflowMove workflowMove, String note, Logger logger, boolean isAsync, LockStateLogger stateLogger) {
        formDataAccessService.checkDestinations(formDataId, userInfo, logger);
        List<WorkflowMove> availableMoves = formDataAccessService.getAvailableMoves(userInfo, formDataId);
        if (!availableMoves.contains(workflowMove)) {
            throw new ServiceException(
                    "Переход \""
                            + workflowMove.getRoute()
                            + "\" из текущего состояния невозможен, или у пользователя " +
                            "не хватает полномочий для его осуществления");
        }

        FormData formData = formDataDao.get(formDataId, manual);
        switch (workflowMove){
            case PREPARED_TO_APPROVED:
                lockForm(logger, formData);
                //Проверяем что записи справочников, на которые есть ссылки в нф все еще существуют в периоде формы
                stateLogger.updateState("Проверка ссылок на справочники");
                checkReferenceValues(logger, formData, false);
                //Делаем переход
                moveProcess(formData, userInfo, workflowMove, note, logger, isAsync, stateLogger);
                break;
            case CREATED_TO_PREPARED:
            case APPROVED_TO_ACCEPTED:
            case PREPARED_TO_ACCEPTED:
            case CREATED_TO_ACCEPTED:
                lockForm(logger, formData);
                //Проверяем что записи справочников, на которые есть ссылки в нф все еще существуют в периоде формы
                stateLogger.updateState("Проверка ссылок на справочники");
                checkReferenceValues(logger, formData, false);
                checkConsolidateFromSources(formData, logger, userInfo);
                //Делаем переход
                moveProcess(formData, userInfo, workflowMove, note, logger, isAsync, stateLogger);
                if (!formData.isSorted() && WorkflowState.ACCEPTED.equals(workflowMove.getToState())) {
                    FormTemplate formTemplate = formTemplateService.get(formData.getFormTemplateId());
                    if (!formTemplate.isFixedRows()) {
                        // Отработка скриптом события сортировки
                        formDataScriptingService.executeScript(userInfo, formData, FormDataEvent.SORT_ROWS, logger, null);
                        if (logger.containsLevel(LogLevel.ERROR)) {
                            throw new ServiceLoggerException(SORT_ERROR, logEntryService.save(logger.getEntries()));
                        }
                        // сортировка актуальна (событие сортировки отработало)
                        formDataDao.updateSorted(formData.getId(), true);
                        logger.info("Выполнена сортировка строк налоговой формы.");
                    }
                }
                break;
            case APPROVED_TO_CREATED:
            case ACCEPTED_TO_APPROVED:
            case ACCEPTED_TO_PREPARED:
            case ACCEPTED_TO_CREATED:
                if (workflowMove.getFromState().equals(WorkflowState.ACCEPTED)) {
                    // удаляем версию ручного ввода
                    deleteFormData(logger, userInfo, formDataId, true);
                    // устанавливаем признак в "1", т.к. могли отредактировать версию ручного ввода
                    formDataDao.updateSorted(formData.getId(), true);
                }
                sourceService.updateFDDDConsolidation(formDataId);
                moveProcess(formData, userInfo, workflowMove, note, logger, isAsync, stateLogger);
                break;
            default:
                //Делаем переход
                moveProcess(formData, userInfo, workflowMove, note, logger, isAsync, stateLogger);
        }
    }

    private void lockForm(Logger logger, FormData formData){
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
    }

    private void checkConsolidateFromSources(FormData formData, Logger logger, TAUserInfo userInfo){
        //Проверка на неактуальные консолидированные данные
        if (!sourceService.isFDConsolidationTopical(formData.getId())){
            logger.warn(CONSOLIDATION_NOT_TOPICAL);
        } else {
            //Проверка выполнена ли консолидация из всех принятых источников текущего экземпляра
            boolean hasNotAccepted = false;
            boolean hasNotConsolidated = false;
            for (Relation relation : sourceService.getSourcesInfo(formData, true, false, null, userInfo, logger)) {
                if (relation.isCreated() && relation.getState() == WorkflowState.ACCEPTED &&
                        !sourceService.isFDSourceConsolidated(formData.getId(), relation.getFormDataId())) {
                    hasNotConsolidated = true;
                    logger.error(NOT_CONSOLIDATED_SOURCE_FORM,
                            relation.getFullDepartmentName(),
                            relation.getFormDataKind().getTitle(),
                            relation.getFormTypeName(),
                            relation.getPeriodName() + (relation.getMonth() != null ? " " + Months.fromId(relation.getMonth()).getTitle() : ""),
                            relation.getYear(),
                            relation.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s",
                                    SDF_DD_MM_YYYY.format(relation.getCorrectionDate())) : ""
                    );
                } else if (!relation.isCreated() || relation.getState() != WorkflowState.ACCEPTED) {
                    hasNotAccepted = true;
                    logger.warn(NOT_ACCEPTED_SOURCE_FORM,
                            relation.getFullDepartmentName(),
                            relation.getFormDataKind().getTitle(),
                            relation.getFormTypeName(),
                            relation.getPeriodName() + (relation.getMonth() != null ? " " + Months.fromId(relation.getMonth()).getTitle() : ""),
                            relation.getYear(),
                            relation.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s",
                                    SDF_DD_MM_YYYY.format(relation.getCorrectionDate())) : "",
                            !relation.isCreated() ? "Не создана" : relation.getState().getTitle());
                }
            }
            //Если консолидация из всех принятых источников текущего экземпляра не была выполнена
            if (hasNotConsolidated) {
                logger.clear(LogLevel.WARNING);
                logger.getEntries().add(0, new LogEntry(LogLevel.ERROR, NOT_CONSOLIDATED_SOURCE_FORM_ERR));
                throw new ServiceLoggerException("", logEntryService.save(logger.getEntries()));
            }
            //Если консолидация из всех принятых источников текущего экземпляра была выполнена, но есть непринятые или несозданные источники
            if (hasNotAccepted) {
                logger.getEntries().add(0, new LogEntry(LogLevel.WARNING, NOT_ACCEPTED_SOURCE_FORM_WARN));
            }
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
		rows = dataRowDao.getRows(formData, null);
        if (!rows.isEmpty()) {
            for (Column column : formData.getFormColumns()) {
                if (ColumnType.REFBOOK.equals(column.getColumnType())) {
                    Long attributeId = ((RefBookColumn) column).getRefBookAttributeId();
                    if (attributeId != null) {
                        RefBook refBook = refBookDao.getByAttribute(attributeId);
                        for (DataRow<Cell> row : rows) {
                            if (row.getCell(column.getAlias()).getNumericValue() != null) {
                                //Если у справочника нет своей таблицы, значит он универсальный и провайдер для него один и тот же
                                long refBookId = refBook.getTableName() != null
                                        ? refBook.getId() : RefBook.UNIVERSAL_REF_BOOK_ID;
                                if (!recordsToCheck.containsKey(refBookId)) {
                                    recordsToCheck.put(refBookId, new ArrayList<Long>());
                                }
                                //Раскладываем значения ссылок по справочникам, на которые они ссылаются. Делим на группы - универсальные и особенные (типа REF_BOOK_OKTMO)
                                recordsToCheck.get(refBookId).add(row.getCell(column.getAlias()).getNumericValue().longValue());

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
                Map<Long, CheckResult> inactiveRecords = provider.getInactiveRecordsInPeriod(referencesToCheck.getValue(), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
                if (!inactiveRecords.isEmpty()) {
                    for (Map.Entry<Long, CheckResult> inactiveRecord : inactiveRecords.entrySet()) {
                        Long id = inactiveRecord.getKey();
                        CheckResult checkResult = inactiveRecord.getValue();
                        for (ReferenceInfo referenceInfo : referenceInfoMap.get(id)) {
                            switch (checkResult) {
                                case NOT_EXISTS:
                                    logger.error(String.format(REF_BOOK_LINK, referenceInfo.getRownum(),
                                            ": значение графы", referenceInfo.getColumnName(),
                                            " ссылается на несуществующую версию записи справочника!"));
                                    break;
                                case NOT_CROSS:
                                    logger.error(String.format(REF_BOOK_LINK, referenceInfo.getRownum(),
                                            ", атрибут", referenceInfo.getColumnName(),
                                            ": период актуальности значения не пересекается с отчетным периодом формы!"));
                                    break;
                                case NOT_LAST:
                                    logger.error(String.format(REF_BOOK_LINK, referenceInfo.getRownum(),
                                            ": значение графы", referenceInfo.getColumnName(),
                                            " не является последним актуальным значением в отчетном периоде формы!"));
                                    break;
                            }
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

    private void moveProcess(FormData formData, TAUserInfo userInfo, WorkflowMove workflowMove, String note, Logger logger, boolean isAsync, LockStateLogger stateLogger) {
        stateLogger.updateState("Проверка данных налоговой формы");
        formDataScriptingService.executeScript(userInfo, formData, workflowMove.getEvent(), logger, null);

        if (WorkflowMove.CREATED_TO_ACCEPTED.equals(workflowMove) ||
                WorkflowMove.CREATED_TO_APPROVED.equals(workflowMove) ||
                WorkflowMove.CREATED_TO_PREPARED.equals(workflowMove) ||
                WorkflowMove.PREPARED_TO_APPROVED.equals(workflowMove) ||
                WorkflowMove.PREPARED_TO_ACCEPTED.equals(workflowMove)) {
            stateLogger.updateState("Проверка заполнения параметров печатной формы");
            checkPerformer(logger, formData);
        }

        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(
                    "",
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

        stateLogger.updateState("Изменение состояния формы");
        eventHandlerLauncher.process(userInfo, formData, workflowMove.getEvent(), logger, null);

        if (workflowMove.getAfterEvent() != null) {
            formDataScriptingService.executeScript(userInfo, formData, workflowMove.getAfterEvent(), logger, null);
            if (logger.containsLevel(LogLevel.ERROR)) {
                throw new ServiceLoggerException(
                        "Произошли ошибки в скрипте, который выполняется после перехода",
                        logEntryService.save(logger.getEntries()));
            }
        }
		dataRowDao.refreshRefBookLinks(formData);

        if (!isAsync)
            logger.info("Форма \"" + formData.getFormType().getName() + "\" переведена в статус \"" + workflowMove.getToState().getTitle() + "\"");

        //Считаем что при наличие версии ручного ввода движение о жц невозможно
        stateLogger.updateState("Удаление отчетов формы");
        deleteReport(formData.getId(), null, userInfo.getUser().getId(), "");
        dataRowDao.removeCheckPoint(formData);

        logBusinessService.add(formData.getId(), null, userInfo, workflowMove.getEvent(), note);
        auditService.add(workflowMove.getEvent(), userInfo, null, formData, workflowMove.getEvent().getTitle(), null);

        stateLogger.updateState("Обновление сквозной нумерации");
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
    public void compose(final FormData formData, final TAUserInfo userInfo, Logger logger, LockStateLogger stateLogger) {
        stateLogger.updateState("Выполнение проверок на возможность консолидации");
        HashSet<Long> srcAcceptedIds = new HashSet<Long>();
        ArrayList<String> msgPull = new ArrayList<String>(0);
        //Проверяем наличие принятых экземпляров-источников
        List<Relation> relations = sourceService.getSourcesInfo(formData, true, true, WorkflowState.ACCEPTED, userInfo, logger);
        for (Relation relation : relations){
            srcAcceptedIds.add(relation.getFormDataId());
            msgPull.add(String.format(FORM_DATA_INFO_MSG,
                    relation.getFullDepartmentName(),
                    relation.getFormDataKind().getTitle(),
                    relation.getFormTypeName(),
                    relation.getPeriodName() + (relation.getMonth() != null ? " " + Months.fromId(relation.getMonth()).getTitle() : ""),
                    (relation.getCorrectionDate() != null ?
                            String.format(CORRECTION_PATTERN, SDF_DD_MM_YYYY.format(relation.getCorrectionDate()))
                            :
                            "")
            ));
        }

        //1Е.  Система проверяет экземпляр на возможность выполнения консолидации в него. Не существует ни одной принятой формы-источника
        if (srcAcceptedIds.isEmpty()){
            //Очищаем устаревшие данные, оставшиеся после старой консолидации
            tx.executeInNewTransaction(new TransactionLogic() {
                @Override
                public Object execute() {
                    clearDataRows(formData, userInfo);
					return null;
                }
            });
            throw new ServiceException("Для текущей формы не существует ни одного источника в статусе \"Принята\"");
        }

        //3. Консолидируем
        stateLogger.updateState("Консолидация данных в форму");
        ScriptComponentContextImpl scriptComponentContext = new ScriptComponentContextImpl();
        scriptComponentContext.setUserInfo(userInfo);
        scriptComponentContext.setLogger(logger);
        FormDataCompositionService formDataCompositionService = applicationContext.getBean(FormDataCompositionService.class);
        ((ScriptComponentContextHolder) formDataCompositionService).setScriptComponentContext(scriptComponentContext);
        formDataDao.updateSorted(formData.getId(), false);
        formDataDao.updateEdited(formData.getId(), true);
        formDataCompositionService.compose(formData, 0, null, formData.getFormType().getId(), formData.getKind());
        dataRowDao.refreshRefBookLinks(formData);

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
        updatePreviousRowNumber(formData, logger, userInfo, false, formData.getState() == WorkflowState.CREATED);
        //Обновление записей о консолидации
        sourceService.deleteFDConsolidationInfo(Arrays.asList(formData.getId()));
        sourceService.addFormDataConsolidationInfo(formData.getId(), srcAcceptedIds);
    }

    @Override
    public void checkCompose(final FormData formData, final TAUserInfo userInfo, Logger logger) {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(formData.getReportPeriodId());
        //1А. Отчетный период закрыт
        if (!departmentReportPeriod.isActive()){
            throw new ServiceException("отчетный период закрыт, консолидация не может быть выполнена.");
        }
        //1Б. Статус экземпляра не допускает его редактирование
        if (formData.getState() != WorkflowState.CREATED) {
            throw new ServiceException("форма находится в статусе \"%s\", консолидация возможна только в статусе \"Создана\"",
                    formData.getState().getTitle());
        }
        //1В. Проверяем формы-приемники
        ArrayList<String> msgPull = new ArrayList<String>(0);
        for (Relation destination : sourceService.getDestinationsInfo(formData, true, true, null, userInfo, logger)){
            if (destination.isCreated() && destination.getState() != WorkflowState.CREATED){
                msgPull.add(String.format(FORM_DATA_INFO_MSG,
                        destination.getFullDepartmentName(),
                        destination.getFormDataKind().getTitle(),
                        destination.getFormTypeName(),
                        destination.getPeriodName(),
                        (destination.getCorrectionDate() != null ?
                                String.format(CORRECTION_PATTERN, SDF_DD_MM_YYYY.format(destination.getCorrectionDate()))
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
        if (sourceService.getSourcesInfo(formData, true, false, null, userInfo, logger).isEmpty()){
            throw new ServiceException("для текущей формы не назначено ни одного источника");
        }
    }

    @Override
    public void checkSources(long formDataId, boolean manual, TAUserInfo userInfo, Logger logger) {
        List<String> notAcceptedMsgs = new ArrayList<String>();
        List<String> notExistMsgs = new ArrayList<String>();
        boolean existAcceptedFD = false;
        FormData formData = formDataDao.getWithoutRows(formDataId);
        for (Relation relation : sourceService.getSourcesInfo(formData, true, false, null, userInfo, logger)){
            if (relation.isCreated()) {
                if (!relation.getState().equals(WorkflowState.ACCEPTED)) {
                    notAcceptedMsgs.add(
                            MessageGenerator.getFDMsg("",
                                    relation.getFormTypeName(),
                                    relation.getFormDataKind().getTitle(),
                                    relation.getFullDepartmentName(),
                                    relation.getMonth(),
                                    relation.isManual(),
                                    relation.getPeriodName(),
                                    relation.getComparativePeriodName(),
                                    relation.getYear(),
                                    relation.getComparativePeriodYear(),
                                    relation.getCorrectionDate()
                            )
                    );
                } else {
                    existAcceptedFD = true;
                }
            } else {
                notExistMsgs.add(
                        MessageGenerator.getFDMsg("",
                                relation.getFormTypeName(),
                                relation.getFormDataKind().getTitle(),
                                relation.getFullDepartmentName(),
                                relation.getMonth(),
                                relation.isManual(),
                                relation.getPeriodName(),
                                relation.getComparativePeriodName(),
                                relation.getYear(),
                                relation.getComparativePeriodYear(),
                                relation.getCorrectionDate()
                        )
                );
            }
        }
        if (!existAcceptedFD) {
            logger.warn("Для текущей формы не существует ни одной формы-источника в статусе \"Принята\". Консолидация предусмотрена из форм-источников в статусе \"Принята\".");
        } else {
            if (!notAcceptedMsgs.isEmpty()) {
                logger.warn("Для текущей формы следующие формы-источники имеют статус отличный от \"Принята\" (консолидация предусмотрена из форм-источников в статусе \"Принята\"):");
                for (String msg : notAcceptedMsgs) {
                    logger.warn(msg);
                }
            }
            if (!notExistMsgs.isEmpty()) {
                logger.warn("Для текущей формы следующие формы-источники не созданы:");
                for(String msg : notExistMsgs) {
                    logger.warn(msg);
                }
            }
        }
    }

    // очищаем данные в нф, приводим их к исходному состоянию (только фиксированные)
    private void clearDataRows(FormData formData, TAUserInfo userInfo){
        List<DataRow<Cell>> fixRows = formTemplateService.get(formData.getFormTemplateId()).getRows();
		dataRowDao.saveRows(formData, fixRows);
		dataRowDao.refreshRefBookLinks(formData);
		updatePreviousRowNumber(formData, userInfo);
    }

    @Override
    public String getFormDataFullName(long formDataId, boolean manual, String str, ReportType reportType) {
        //TODO: можно оптимизировать и сделать в 1 запрос
        FormData formData = formDataDao.getWithoutRows(formDataId);
        Department department = departmentService.getDepartment(formData.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        DepartmentReportPeriod rpComparison =
                formData.getComparativePeriodId() != null ?
                        departmentReportPeriodService.get(formData.getComparativePeriodId()) : null;
        String name;
        if (reportType != null) {
            switch (reportType) {
                case MOVE_FD:
                    name = MessageGenerator.getFDMsg(str,
                            formData,
                            department.getName(),
                            manual,
                            reportPeriod,
                            rpComparison);
                    break;
                case CSV:
                case EXCEL:
                case IMPORT_FD:
                case IMPORT_TF_FD:
                case CONSOLIDATE_FD:
                case REFRESH_FD:
                case CALCULATE_FD:
                case CHECK_FD:
                case EDIT_FD:
                case DELETE_FD:
                case EDIT_FILE_COMMENT:
                    name = MessageGenerator.getFDMsg(getTaskName(reportType, formData),
                            formData,
                            department.getName(),
                            manual,
                            reportPeriod,
                            rpComparison);
                    break;
                default:
                    name = MessageGenerator.getFDMsg("Налоговая форма",
                            formData,
                            department.getName(),
                            manual,
                            reportPeriod,
                            rpComparison);
            }
        } else {
            name = MessageGenerator.getFDMsg("Налоговая форма",
                    formData,
                    department.getName(),
                    manual,
                    reportPeriod,
                    rpComparison);
        }
        return name;
    }

    @Override
    public FormData findFormData(int formTypeId, FormDataKind kind, int departmentReportPeriodId, Integer periodOrder,
                                 Integer comparativePeriodId, boolean accruing) {
        if (periodOrder == null || kind != FormDataKind.PRIMARY && kind != FormDataKind.CONSOLIDATED) {
            // Если форма-источник квартальная или форма-приемник не является первичной или консолидированной, то ищем квартальный экземпляр
            periodOrder = null;
        }
        return formDataDao.find(formTypeId, kind, departmentReportPeriodId, periodOrder, comparativePeriodId, accruing);
    }

    @Override
	@Transactional
	public void lock(long formDataId, boolean manual, TAUserInfo userInfo) {// используется для редактирования и миграции
        checkLockAnotherUser(lockService.lock(generateTaskKey(formDataId, ReportType.EDIT_FD),
                userInfo.getUser().getId(),
                getFormDataFullName(formDataId, manual, null, ReportType.EDIT_FD)), // FIXME для миграции не совсем верно
                null, userInfo.getUser());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Boolean unlock(final long formDataId, final TAUserInfo userInfo) {
        return tx.executeInNewTransaction(new TransactionLogic<Boolean>() {
            @Override
            public Boolean execute() {
                return lockService.unlock(generateTaskKey(formDataId, ReportType.EDIT_FD), userInfo.getUser().getId());
            }
        });
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public LockData getObjectLock(final long formDataId, final TAUserInfo userInfo) {
        return tx.executeInNewTransaction(new TransactionLogic<LockData>() {
            @Override
            public LockData execute() {
                return lockService.getLock(generateTaskKey(formDataId, ReportType.EDIT_FD));
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
        List<FormData> ifrsFormList = new ArrayList<FormData>();
        List<FormData> formDataList = formDataDao.getIfrsForm(reportPeriodId);
        List<Integer> formTypeList = formTypeService.getIfrsFormTypes();
        for(FormData formData: formDataList) {
            boolean flag = true;
            List<DepartmentFormType> departmentImpFormTypes = sourceService.getFormDestinations(formData.getDepartmentId(), formData.getFormType().getId(), formData.getKind(), reportPeriodId);
            for (DepartmentFormType departmentFormType : departmentImpFormTypes) {
                if (formTypeList.contains(departmentFormType.getFormTypeId())) {
                    flag = false;
                    break;
                }
            }

            if (flag)
                ifrsFormList.add(formData);
        }
        return ifrsFormList;
    }

    @Override
    public boolean existFormData(int formTypeId, FormDataKind kind, int departmentId, Logger logger) {
        // Если для удаляемого назначения нет созданных экземпляров форм
        List<Long> formDataIds = formDataDao.getFormDataIds(formTypeId, kind, departmentId);
        if (logger != null) {
            for (long formDataId : formDataIds) {
                FormData formData = formDataDao.getWithoutRows(formDataId);
                //ReportPeriod period = reportPeriodService.getReportPeriod(formData.getReportPeriodId());
                DepartmentReportPeriod drp = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
                DepartmentReportPeriod drpCompare = formData.getComparativePeriodId() != null ? departmentReportPeriodService.get(formData.getComparativePeriodId()) : null;
                FormTemplate ft = formTemplateService.get(formData.getFormTemplateId());

                logger.error(
                        MessageGenerator.getFDMsg(
                                String.format(MSG_IS_EXIST_FORM,
                                        ft.getType().getTaxType() == TaxType.ETR || ft.getType().getTaxType() == TaxType.DEAL ? "форм" : "налоговых форм"),
                                formData,
                                departmentService.getDepartment(departmentId).getName(),
                                formData.isManual(),
                                drp,
                                drpCompare)
                );
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
                deleteReport(formDataId, null, user.getUser().getId(), "Обновлении имени ТБ");
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
                deleteReport(formDataId, null, user.getUser().getId(), "Обновлении имени подразделения");
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
        if (!formDataList.isEmpty()) {
            for (FormData aFormData : formDataList) {
                if (beInOnAutoNumeration(aFormData.getState(), departmentReportPeriod)) {
                    previousRowNumber += dataRowDao.getAutoNumerationRowCount(aFormData);
                }
                if (aFormData.getId().equals(formData.getId())) {
                    return previousRowNumber;
                }
            }
        }

        return previousRowNumber;
    }

    /**
     * Обновление связанных со сквозной автонумерацией атрибутов
     *
     * @param logger   логгер для регистрации ошибок
     * @param formData редактируемый экземпляр НФ
     */
    void updateAutoNumeration(FormData formData, Logger logger, TAUserInfo user) {
		// Обновление значений атрибута "Номер последней строки предыдущей НФ" при сохранении
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        if (!formData.isManual() && beInOnAutoNumeration(formData.getState(), departmentReportPeriod)
                && dataRowDao.isDataRowsCountChanged(formData)) {
            updatePreviousRowNumber(formData, logger, user, true, false);
        }
		// Пересчет текущего кол-ва нумеруемых граф
		FormTemplate template = formTemplateService.get(formData.getFormTemplateId());
		if (formTemplateService.isAnyAutoNumerationColumn(template, NumerationType.CROSS)) {
			formDataDao.updateCurrentRowNumber(formData);
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
            updatePreviousRowNumber(formData, logger, user, false, workflowMove.getToState() == WorkflowState.CREATED);
        }
    }

    @Override
    public void updatePreviousRowNumber(FormData formData, TAUserInfo user) {
        updatePreviousRowNumber(formData, null, user, false, formData.getState() == WorkflowState.CREATED);
    }

    @Override
    public void updatePreviousRowNumber(FormData formData, Logger logger, TAUserInfo user, boolean isSave, boolean useZero) {
        FormTemplate formTemplate = formTemplateService.get(formData.getFormTemplateId());
        updatePreviousRowNumber(formData, formTemplate, logger, user, isSave, useZero);
    }

    @Override
    public void updatePreviousRowNumber(FormData formData, FormTemplate formTemplate, Logger logger, TAUserInfo user, boolean isSave, boolean useZero) {
        String msg = null;

        if (formTemplateService.isAnyAutoNumerationColumn(formTemplate, NumerationType.CROSS)) {
            // Получить налоговый период
            TaxPeriod taxPeriod = reportPeriodService.getReportPeriod(formData.getReportPeriodId()).getTaxPeriod();
            // Получить список экземпляров НФ следующих периодов
            List<FormData> formDataList = formDataDao.getNextFormDataList(formData, taxPeriod);

            // Устанавливаем значение для текущего экземпляра НФ
            Integer previousRowNumber = (useZero ? 0 : getPreviousRowNumber(formData, null));
            formDataDao.updatePreviousRowNumber(formData, previousRowNumber);

            StringBuilder stringBuilder = new StringBuilder();
            // Обновляем последующие периоды
            int size = formDataList.size();

            for (FormData data : formDataList) {
                formDataDao.updatePreviousRowNumber(data, getPreviousRowNumber(data, isSave ? formData : null));
                deleteReport(data.getId(), null, user.getUser().getId(), "Обновление автонумерации");
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
            updatePreviousRowNumber(formData, formTemplate, null, user, false, false);
        }
    }

    @Override
    public FormData getLast(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder, Integer comparativePeriodId, boolean accruing) {
        return formDataDao.getLast(formTypeId, kind, departmentId, reportPeriodId, periodOrder, comparativePeriodId, accruing);
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
                prevDepartmentReportPeriod.getId(), formData.getPeriodOrder(),
                formData.getComparativePeriodId(), formData.isAccruing());

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
            throw new ServiceException(String.format("Объект заблокирован другим пользователем (\"%s\", \"%s\")",
					userService.getUser(lockData.getUserId()).getLogin(), SDF_HH_MM_DD_MM_YYYY.format(lockData.getDateLock())));
        }
    }

    @Override
    public List<String> generateReportKeys(ReportType reportType, long formDataId, Boolean manual) {
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
    public void deleteReport(long formDataId, Boolean manual, int userId, String cause) {
        interruptTask(formDataId, manual, userId, ReportType.DELETE_REPORT_FD, cause);
    }

    @Override
    public void findFormDataIdsByRangeInReportPeriod(int formTemplateId, Date startDate, Date endDate, Logger logger) {
        List<Integer> fdIds = formDataDao.findFormDataIdsByRangeInReportPeriod(formTemplateId,
                startDate, endDate != null ? endDate : MAX_DATE);
        for (Integer id : fdIds){
            FormData fd = formDataDao.getWithoutRows(id);
            DepartmentReportPeriod drp = departmentReportPeriodService.get(fd.getDepartmentReportPeriodId());
            DepartmentReportPeriod drpCompare = fd.getComparativePeriodId() != null ? departmentReportPeriodService.get(fd.getComparativePeriodId()) : null;

            logger.error(MessageGenerator.getFDMsg(FD_NOT_IN_RANGE,
                    fd,
                    departmentService.getDepartment(fd.getDepartmentId()).getName(),
                    fd.isManual(),
                    drp,
                    drpCompare));
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
    public void interruptTask(long formDataId, TAUserInfo userInfo, List<ReportType> reportTypes, String cause) {
        List<String> lockKeys = new ArrayList<String>();
        for(ReportType reportType: reportTypes)
            lockKeys.add(generateTaskKey(formDataId, reportType));
        lockService.interruptAllTasks(lockKeys, userInfo.getUser().getId(), cause);
    }


    @Override
    public Pair<ReportType, LockData> getLockTaskType(long formDataId) {
        ReportType[] reportTypes = {ReportType.MOVE_FD, ReportType.CONSOLIDATE_FD, ReportType.IMPORT_TF_FD, ReportType.REFRESH_FD, ReportType.CALCULATE_FD, ReportType.IMPORT_FD, ReportType.EDIT_FD};
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
        FormData formData = formDataDao.get(formDataId, false);
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
                msg = String.format(
                        "Для текущего экземпляра %s запущены операции, при которых формирование отчета невозможно",
                        MessageGenerator.mesSpeckSingleD(formData.getFormType().getTaxType())
                );
                break;
            case CHECK_FD:
                msg = String.format(
                        "Для текущего экземпляра %s запущены операции, при которых ее проверка невозможна",
                        MessageGenerator.mesSpeckSingleD(formData.getFormType().getTaxType())
                );
                break;
            case MOVE_FD:
                msg = String.format(
                        "Для текущего экземпляра %s запущены операции, при которых изменение его состояния невозможно",
                        MessageGenerator.mesSpeckSingleD(formData.getFormType().getTaxType())
                );
                break;
            case REFRESH_FD:
                msg = String.format("Выполнение операции \"%s\" невозможно, т.к. для текущего экземпляра %s запущена операция \"%s\". Обновление данных невозможно",
                        getTaskName(reportType, formDataId, userInfo),
                        MessageGenerator.mesSpeckSingleD(formData.getFormType().getTaxType()),
                        getTaskName(lockType.getFirst(), formDataId, userInfo));
                break;
            case CALCULATE_FD:
                msg = String.format("Выполнение операции \"%s\" невозможно, т.к. для текущего экземпляра %s запущена операция \"%s\". Расчет данных невозможен",
                        getTaskName(reportType, formDataId, userInfo),
                        MessageGenerator.mesSpeckSingleD(formData.getFormType().getTaxType()),
                        getTaskName(lockType.getFirst(), formDataId, userInfo));
                break;
            case IMPORT_FD:
                msg = String.format("Выполнение операции \"%s\" невозможно, т.к. для текущего экземпляра %s запущена операция \"%s\". Загрузка данных из файла невозможна",
                        getTaskName(reportType, formDataId, userInfo),
                        MessageGenerator.mesSpeckSingleD(formData.getFormType().getTaxType()),
                        getTaskName(lockType.getFirst(), formDataId, userInfo));
                break;
            case CONSOLIDATE_FD:
                msg = "Операция не выполнена";
                break;
            case DELETE_FD:
                msg = String.format("Выполнение операции \"%s\" невозможно, т.к. для текущего экземпляра %s выполняется операция, блокирующая ее удаление",
                        getTaskName(reportType, formDataId, userInfo),
                        MessageGenerator.mesSpeckSingleD(formData.getFormType().getTaxType()));
                break;
            case EDIT_FD:
                msg = String.format("Выполнение операции \"%s\" невозможно, т.к. для текущего экземпляра %s запущена операция изменения данных",
                        getTaskName(reportType, formDataId, userInfo),
                        MessageGenerator.mesSpeckSingleD(formData.getFormType().getTaxType()));
                break;
        }
        throw new ServiceLoggerException(msg, logEntryService.save(logger.getEntries()));
    }

    @Override
    public Long getValueForCheckLimit(TAUserInfo userInfo, FormData formData, ReportType reportType, String uuid, Logger logger) {
        switch (reportType) {
            case CHECK_FD:
            case MOVE_FD:
            case REFRESH_FD:
            case CALCULATE_FD:
            case EXCEL:
            case CSV:
                int rowCountReport = dataRowDao.getRowCount(formData);
                int columnCountReport = formTemplateService.get(formData.getFormTemplateId()).getColumns().size();
                return (long) (rowCountReport * columnCountReport);
            case CONSOLIDATE_FD:
                long cellCountSource = 0;
                for (Relation relation : sourceService.getSourcesInfo(formData, true, true, WorkflowState.ACCEPTED, userInfo, logger)){
                    FormData sourceForm = formDataDao.getWithoutRows(relation.getFormDataId());
                    int rowCountSource = dataRowDao.getRowCount(sourceForm);
                    int columnCountSource = formTemplateService.get(sourceForm.getFormTemplateId()).getColumns().size();
                    cellCountSource += rowCountSource * columnCountSource;
                }
                return cellCountSource;
            case IMPORT_FD:
                return blobDataService.getLength(uuid)/1024;
            default:
                throw new ServiceException("Неверный тип отчета(%s)", reportType.getName());
        }
    }

    @Override
    public String getTaskName(ReportType reportType, long formDataId, TAUserInfo userInfo) {
        return getTaskName(reportType, formDataDao.getWithoutRows(formDataId));
    }

    private String getTaskName(ReportType reportType, FormData formData) {
        switch (reportType) {
            case DELETE_FD:
            case EDIT_FD:
            case REFRESH_FD:
            case CALCULATE_FD:
            case CONSOLIDATE_FD:
            case CHECK_FD:
            case MOVE_FD:
                return String.format(reportType.getDescription(), formData.getFormType().getTaxType().getTaxText());
            case EXCEL:
            case CSV:
                return String.format(reportType.getDescription(), MessageGenerator.mesSpeckSingleD(formData.getFormType().getTaxType()));
            case IMPORT_FD:
            case IMPORT_TF_FD:
            case EDIT_FILE_COMMENT:
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
            case EDIT_FD:
                return new ReportType[]{ReportType.CHECK_FD, ReportType.EXCEL, ReportType.CSV};
            case CONSOLIDATE_FD:
                return new ReportType[]{ReportType.MOVE_FD, ReportType.CHECK_FD, ReportType.REFRESH_FD, ReportType.CALCULATE_FD, ReportType.IMPORT_FD, ReportType.IMPORT_TF_FD, ReportType.EXCEL, ReportType.CSV};
            case REFRESH_FD:
                return new ReportType[]{ReportType.CHECK_FD, ReportType.EXCEL, ReportType.CSV};
            case CALCULATE_FD:
                return new ReportType[]{ReportType.CHECK_FD, ReportType.EXCEL, ReportType.CSV};
            case IMPORT_FD:
                return new ReportType[]{ReportType.CHECK_FD, ReportType.EXCEL, ReportType.CSV};
            case MOVE_FD:
                return new ReportType[]{ReportType.CHECK_FD, ReportType.EXCEL, ReportType.CSV};
            case DELETE_FD:
                return new ReportType[]{ReportType.EXCEL, ReportType.CSV};
            case IMPORT_TF_FD:
                return new ReportType[]{ReportType.EXCEL, ReportType.CSV};
            case DELETE_REPORT_FD:
                return new ReportType[]{ReportType.EXCEL, ReportType.CSV};
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
            case REFRESH_FD:
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
    public void interruptTask(long formDataId, Boolean manual, int userId, ReportType reportType, String cause) {
        ReportType[] reportTypes = getCheckTaskList(reportType);
        if (reportTypes == null) return;
        Boolean manualReport;
        switch (reportType) {
            case REFRESH_FD:
            case CALCULATE_FD:
            case IMPORT_FD:
            case DELETE_REPORT_FD:
                manualReport = manual;
                break;
            default:
                manualReport = null;
        }
        for (ReportType reportType1: reportTypes) {
            List<String> taskKeyList = new ArrayList<String>();
            if (ReportType.CSV.equals(reportType1) || ReportType.EXCEL.equals(reportType1)) {
                taskKeyList.addAll(generateReportKeys(reportType1, formDataId, manualReport));
                if (!ReportType.EDIT_FD.equals(reportType)) {
                    // при входе в режим редактирования не удаляем отчеты
                    reportService.delete(formDataId, manual);
                }
            } else {
                taskKeyList.add(generateTaskKey(formDataId, reportType1));
            }
            for(String key: taskKeyList) {
                LockData lock = lockService.getLock(key);
                if (lock != null) {
                    lockService.interruptTask(lock, userId, true, cause);
                }
            }
        }
    }

    @Override
    public void restoreCheckPoint(long formDataId, boolean manual, TAUserInfo userInfo) {
        interruptTask(formDataId, userInfo, Arrays.asList(ReportType.REFRESH_FD, ReportType.CALCULATE_FD, ReportType.IMPORT_FD, ReportType.CHECK_FD), "Отмена изменений");
        if (formDataDao.isEdited(formDataId)) {
            dataRowDao.restoreCheckPoint(getFormData(userInfo, formDataId, manual, new Logger()));
            formDataDao.restoreSorted(formDataId);
        } else {
            dataRowDao.removeCheckPoint(getFormData(userInfo, formDataId, manual, new Logger()));
        }
        if (!unlock(formDataId, userInfo)) {
            throw new ServiceException("Форма не заблокирована текущим пользователем, formDataId = %s", formDataId);
        }
    }

    @Override
    public boolean isEdited(long formDataId) {
        return formDataDao.isEdited(formDataId);
    }

    @Override
    public List<DataRow<HeaderCell>> getHeaders(final FormData formData, final TAUserInfo userInfo, final Logger logger) {
        FormTemplate formTemplate = formTemplateService.get(formData.getFormTemplateId());
        Map<String, Object> params = new HashMap<String, Object>();
        List<DataRow<HeaderCell>> headers = formTemplate.getHeaders();
        params.put("headers", headers);
        formDataScriptingService.executeScript(userInfo, formData, FormDataEvent.GET_HEADERS, logger, params);

        return headers;
    }

    @Override
    public List<FormDataFile> getFiles(long formDataId) {
        return formDataFileDao.getFiles(formDataId);
    }

    @Override
    public String getNote(long formDataId) {
        return formDataDao.getNote(formDataId);
    }

    @Override
    public void saveFilesComments(long formDataId, String note, List<FormDataFile> files) {
        formDataDao.updateNote(formDataId, note);
        formDataFileDao.saveFiles(formDataId, files);
    }
}
