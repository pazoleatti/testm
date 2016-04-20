package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Реализация сервиса для работы с шаблонами налоговых форм
 * @author dsultanbekov
 */
@Service
@Transactional
public class FormTemplateServiceImpl implements FormTemplateService {

	private static final Log LOG = LogFactory.getLog(FormTemplateServiceImpl.class);
	private static final int FORM_STYLE_ALIAS_MAX_VALUE = 40;
	private static final int FORM_COLUMN_NAME_MAX_VALUE = 1000;
    private static final int FORM_COLUMN_SHORT_NAME_MAX_VALUE = 1000;
    private static final int FORM_COLUMN_ALIAS_MAX_VALUE = 100;
	private static final int DATA_ROW_ALIAS_MAX_VALUE = 20;
    private static final String CLOSE_PERIOD = "Следующие периоды %s данной версии макета закрыты: %s. " +
            "Для добавления в макет автонумеруемой графы с типом сквозной нумерации строк необходимо открыть перечисленные периоды!";
    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };
    private static final String GET_ERROR_MESSAGE = "Ошибка при получении версии макета НФ. %s";

	private Set<String> checkSet = new HashSet<String>();

    @Autowired
	private FormTemplateDao formTemplateDao;
    @Autowired
    private TransactionHelper tx;
    @Autowired
    private FormDataService formDataService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;
    @Autowired
    PeriodService periodService;
    @Autowired
    private FormDataScriptingService scriptingService;
    @Autowired
    private LogEntryService logEntryService;
	@Autowired
	private TAUserService userService;
    @Autowired
    @Qualifier("formTemplateMainOperatingService")
    MainOperatingService mainOperatingService;

	@Override
	public List<FormTemplate> listAll() {
        return formTemplateDao.listAll();
	}

	@Override
	public FormTemplate get(int formTemplateId) {
        try {
            return formTemplateDao.get(formTemplateId);
        }catch (DaoException e){
			LOG.error(String.format(GET_ERROR_MESSAGE, e.getMessage()), e);
            throw new ServiceException("Обновление статуса версии.", e);
        }
	}

	@Override
	public FormTemplate get(int formTemplateId, Logger logger) {
		try {
			return formTemplateDao.get(formTemplateId);
		} catch (DaoException e){
			LOG.error(String.format(GET_ERROR_MESSAGE, e.getMessage()), e);
			logger.error(GET_ERROR_MESSAGE, e.getMessage());
		}
		return null;
	}

    @Transactional(readOnly = false)
	@Override
	public int save(FormTemplate formTemplate) {
        Logger log = new Logger();
        checkScript(formTemplate, log);
        if (formTemplate.getId() != null) {
            int formTemplateId = formTemplateDao.save(formTemplate);
            List<Long> formDataIds = formDataService.getFormDataListInActualPeriodByTemplate(formTemplateId, formTemplate.getVersion());
            for (Long formDataId : formDataIds)
                formDataService.deleteReport(formDataId, null, 0, "Изменен макет НФ");
            return formTemplateId;
        } else
            return formTemplateDao.saveNew(formTemplate);
	}

    @Override
	public int getActiveFormTemplateId(int formTypeId, int reportPeriodId) {
        try {
            return formTemplateDao.getActiveFormTemplateId(formTypeId, reportPeriodId);
        } catch (DaoException e){
			LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
	}

    @Override
    public int getFormTemplateIdByFTAndReportPeriod(int formTypeId, int reportPeriodId) {
        try {
            ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
            return formTemplateDao.getFormTemplateIdByFTAndReportPeriod(formTypeId, reportPeriod.getStartDate(), reportPeriod.getEndDate());
        } catch (DaoException e){
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
	public void checkLockedByAnotherUser(Integer formTemplateId, TAUserInfo userInfo){
		if (formTemplateId!=null){
            LockData objectLock = lockDataService.getLock(LockData.LockObjects.FORM_TEMPLATE.name() + "_" + formTemplateId);
			if (objectLock != null && objectLock.getUserId() != userInfo.getUser().getId()) {
				throw new AccessDeniedException("Шаблон формы заблокирован другим пользователем");
			}
		}
	}

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public LockData getObjectLock(final Integer formTemplateId, final TAUserInfo userInfo) {
        return tx.executeInNewTransaction(new TransactionLogic<LockData>() {
            @Override
            public LockData execute() {
                return lockDataService.getLock(LockData.LockObjects.FORM_TEMPLATE.name() + "_" + formTemplateId);
            }
        });
    }

    @Override
    public List<FormTemplate> getByFilter(TemplateFilter filter) {
        List<FormTemplate> formTemplates = new ArrayList<FormTemplate>();
        for (Integer id : formTemplateDao.getByFilter(filter)) {
            formTemplates.add(formTemplateDao.get(id));
        }
        return formTemplates;
    }

    @Override
    public List<FormTemplate> getFormTemplateVersionsByStatus(int formTypeId, VersionedObjectStatus... status) {
        List<Integer> statusList = createStatusList(status);

        List<Integer> formTemplateIds =  formTemplateDao.getFormTemplateVersions(formTypeId, statusList);
        List<FormTemplate> formTemplates = new ArrayList<FormTemplate>();
        for (Integer id : formTemplateIds)
            formTemplates.add(formTemplateDao.get(id));
        return formTemplates;
    }

    @Override
    public List<Integer> getFTVersionIdsByStatus(int formTypeId, VersionedObjectStatus... status) {
        List<Integer> statusList = createStatusList(status);
        return formTemplateDao.getFormTemplateVersions(formTypeId, statusList);
    }

    @Override
    public List<VersionSegment> findFTVersionIntersections(int templateId, int typeId, Date actualBeginVersion, Date actualEndVersion) {
        return formTemplateDao.findFTVersionIntersections(typeId, templateId, actualBeginVersion, actualEndVersion);
    }

    @Override
    public int delete(int formTemplateId) {
        return formTemplateDao.delete(formTemplateId);
    }

    @Override
    public void delete(Collection<Integer> templateIds) {
        formTemplateDao.delete(templateIds);
    }

    @Override
    public FormTemplate getNearestFTRight(int formTemplateId, VersionedObjectStatus... status) {
        FormTemplate formTemplate = formTemplateDao.get(formTemplateId);

        int id = formTemplateDao.getNearestFTVersionIdRight(formTemplate.getType().getId(), createStatusList(status), formTemplate.getVersion());
        if (id == 0)
            return null;
        return formTemplateDao.get(id);
    }

    @Override
    public Date getFTEndDate(int formTemplateId) {
        if (formTemplateId == 0)
            return null;
        try {
            FormTemplate formTemplate = formTemplateDao.get(formTemplateId);
            return formTemplateDao.getFTVersionEndDate(formTemplate.getType().getId(), formTemplate.getVersion());
        } catch (DaoException e){
            throw new ServiceException("Ошибка получения даты окончания шаблона.", e);
        }
    }

    @Override
    public int versionTemplateCount(int formTypeId, VersionedObjectStatus... status) {
        List<Integer> statusList = createStatusList(status);
        return formTemplateDao.versionTemplateCount(formTypeId, statusList);
    }

    @Override
    public void update(List<FormTemplate> formTemplates) {
        try {
            if (ArrayUtils.contains(formTemplateDao.update(formTemplates), 0))
                throw new ServiceException("Не все записи макета обновились.");
        } catch (DaoException e){
            throw new ServiceException("Ошибка обновления версий.", e);
        }
    }

    @Override
    public Map<Long, Integer> versionTemplateCountByFormType(Collection<Integer> formTypeIds) {
        Map<Long, Integer> integerMap = new HashMap<Long, Integer>();
        if (formTypeIds.isEmpty())
            return integerMap;
        List<Map<String, Object>> mapList = formTemplateDao.versionTemplateCountByType(formTypeIds);
        for (Map<String, Object> map : mapList){
            integerMap.put(((BigDecimal) map.get("type_id")).longValue(), ((BigDecimal)map.get("version_count")).intValue());
        }
        return integerMap;
    }

    @Override
    public int updateVersionStatus(VersionedObjectStatus versionStatus, int formTemplateId) {
        return formTemplateDao.updateVersionStatus(versionStatus, formTemplateId);
    }

    @Override
	public boolean lock(int formTemplateId, TAUserInfo userInfo){
        FormTemplate formTemplate = get(formTemplateId);
        Date endVersion = getFTEndDate(formTemplateId);
        LockData objectLock = lockDataService.lock(LockData.LockObjects.FORM_TEMPLATE.name() + "_" + formTemplateId,
                userInfo.getUser().getId(),
                String.format(
                        LockData.DescriptionTemplate.FORM_TEMPLATE.getText(),
                        formTemplate.getType().getName(),
                        formTemplate.getType().getTaxType().getName(),
                        sdf.get().format(formTemplate.getVersion()),
                        endVersion != null ? sdf.get().format(endVersion) : "-"
                ));
        return !(objectLock != null && objectLock.getUserId() != userInfo.getUser().getId());
	}

	@Override
	public boolean unlock(int formTemplateId, TAUserInfo userInfo){
        lockDataService.unlock(LockData.LockObjects.FORM_TEMPLATE.name() + "_" + formTemplateId,
                userInfo.getUser().getId());
        return true;
	}

	@Override
	public void validateFormTemplate(FormTemplate formTemplate, Logger logger) {
		validateFormColumns(formTemplate, logger);
		validateFormStyles(formTemplate.getStyles(), logger);
		validateFormRows(formTemplate.getRows(), logger);
    }

	private void validateFormColumns(FormTemplate formTemplate, Logger logger) {
        List<Column> columns = formTemplate.getColumns();

        checkSet.clear();

        for (Column column : columns) {
            if (!checkSet.add(column.getAlias())) {
                logger.error(
                        String.format(
                                "Нарушено требование к уникальности, уже существует графа с псевдонимом \"%s\" в данной версии макета \"%s\"!",
                                column.getAlias(),
                                column.getName()
                        )
                );
            }

            if (column.getName() != null && column.getName().getBytes().length > FORM_COLUMN_NAME_MAX_VALUE) {
                logger.error("Значение для имени столбца \"" + column.getName() +
                        "\" слишком велико (фактическое: " + column.getName().getBytes().length +
                        ", максимальное: " + FORM_COLUMN_NAME_MAX_VALUE + ")");
            }
            if (column.getShortName() != null && column.getShortName().getBytes().length > FORM_COLUMN_SHORT_NAME_MAX_VALUE) {
                logger.error("Значение для краткого наименования столбца \"" + column.getAlias() +
                        "\" слишком велико (фактическое: " + column.getShortName().getBytes().length
                        + ", максимальное: " + FORM_COLUMN_SHORT_NAME_MAX_VALUE + ")");
            }
            if (column.getAlias() != null && column.getAlias().getBytes().length > FORM_COLUMN_ALIAS_MAX_VALUE) {
                logger.error("Значение для алиаса столбца \"" + column.getAlias() +
                        "\" слишком велико (фактическое: " + column.getAlias().getBytes().length
                        + ", максимальное: " + FORM_COLUMN_ALIAS_MAX_VALUE + ")");
            }

            if (ColumnType.STRING.equals(column.getColumnType())) {
                String filter = ((StringColumn)column).getFilter();
                if (filter != null && !filter.isEmpty()) {
                    try {
                        Pattern.compile(filter);
                    } catch (PatternSyntaxException e) {
                        logger.error("Значение фильтра столбца \"" + column.getName() +
                                "\" имеет некорректный формат!");
                    }
                }
            }
            /*if (ColumnType.STRING.equals(column.getColumnType()) && ((StringColumn) column).getMaxLength() < ((StringColumn) column).getPrevLength()) {
				if (formTemplateDao.checkExistLargeString(formTemplate.getId(), column)) {
					logger.error("Длина одного из существующих значений графы '" + column.getName() + "' больше указанной длины " + ((StringColumn) column).getPrevLength());
                }
            }*/
        }
    }

	private void validateFormRows(List<DataRow<Cell>> rows, Logger logger) {
		for (DataRow<Cell> row : rows) {
			if (row.getAlias() != null && row.getAlias().getBytes().length > DATA_ROW_ALIAS_MAX_VALUE) {
				logger.error("значение для кода строки \"" + row.getAlias() +
						"\" слишком велико (фактическое: " + row.getAlias().getBytes().length +
						", максимальное: " + DATA_ROW_ALIAS_MAX_VALUE + ")");
			}
		}
	}

	private void validateFormStyles(List<FormStyle> styles, Logger logger) {
		checkSet.clear();
		for (FormStyle style : styles) {
			if (!checkSet.add(style.getAlias())) {
				logger.error(
                        String.format("Нарушено требование к уникальности, уже существует стиль с именем \"%s\" в данной версии макета!",
                                style.getAlias()
                        )
                );
			}
			if (style.getAlias() != null && style.getAlias().getBytes().length > FORM_STYLE_ALIAS_MAX_VALUE) {
				logger.error("значение для алиаса стиля \"" + style.getAlias() +
						"\" слишком велико (фактическое: " + style.getAlias().getBytes().length +
						", максимальное: " + FORM_STYLE_ALIAS_MAX_VALUE + ")");
			}
		}
	}

    private List<Integer> createStatusList(VersionedObjectStatus[] status){
        List<Integer> statusList = new ArrayList<Integer>();
        if (status.length == 0){
            statusList.add(VersionedObjectStatus.NORMAL.getId());
            statusList.add(VersionedObjectStatus.FAKE.getId());
            statusList.add(VersionedObjectStatus.DRAFT.getId());
        }else {
            for (VersionedObjectStatus objectStatus : status)
                statusList.add(objectStatus.getId());
        }
        return statusList;
    }

    @Override
    public boolean isMonthly(int formTemplateId) {
        FormTemplate formTemplate = formTemplateDao.get(formTemplateId);
        return formTemplate.isMonthly();
    }

    @Override
    public boolean isComparative(Integer formTemplateId) {
        FormTemplate formTemplate = formTemplateDao.get(formTemplateId);
        return formTemplate.isComparative();
    }

    @Override
    public void validateFormAutoNumerationColumn(FormTemplate formTemplate, Logger logger, TAUserInfo user) {
		boolean newCross = isAnyAutoNumerationColumn(formTemplate, NumerationType.CROSS);
		boolean newSerial = isAnyAutoNumerationColumn(formTemplate, NumerationType.SERIAL);

		Integer formTemplateId = formTemplate.getId();
		FormTemplate formTemplateOld = get(formTemplateId);
		boolean oldCross = isAnyAutoNumerationColumn(formTemplateOld, NumerationType.CROSS);
		boolean oldSerial = isAnyAutoNumerationColumn(formTemplateOld, NumerationType.SERIAL);

		// если есть автонумеруемые графы и их тип поменялся
		// http://conf.aplana.com/pages/viewpage.action?pageId=11377661&focusedCommentId=14818097#comment-14818097
		if ((oldCross && newSerial) || (oldSerial && newCross)) { //9А
			List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodDao.getClosedForFormTemplate(formTemplateId);

			if (!departmentReportPeriodList.isEmpty()) {
				//9А.1.А
				StringBuilder stringBuilder = new StringBuilder();
				for (int i = 0; i < departmentReportPeriodList.size(); i++) {
					DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodList.get(i);
					stringBuilder.append(departmentReportPeriod.getReportPeriod().getName()).append(" ");
					stringBuilder.append(departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear());
					if (departmentReportPeriod.getCorrectionDate() != null) {
						stringBuilder.append(", корр. (").append(sdf.get().format(departmentReportPeriod.getCorrectionDate())).append(")");
					}
					if (i < departmentReportPeriodList.size() - 1) {
						stringBuilder.append(", ");
					}
				}
				logger.error(CLOSE_PERIOD, MessageGenerator.mesSpeckPluralD(formTemplate.getType().getTaxType()), stringBuilder.toString());
			} else if (oldSerial && newCross) { // 9А.1.1
				formDataService.batchUpdatePreviousNumberRow(formTemplate, user);
			}
        }
    }

    @Override
    public boolean isAnyAutoNumerationColumn(FormTemplate formTemplate, NumerationType type) {
        for (Column column : formTemplate.getColumns()) {
            if (ColumnType.AUTO.equals(column.getColumnType()) && ((AutoNumerationColumn) column).getNumerationType().equals(type)) {
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean existFormTemplate(int formTypeId, int reportPeriodId, boolean excludeInactiveTemplate) {
        return formTemplateDao.existFormTemplate(formTypeId, reportPeriodId, excludeInactiveTemplate);
    }

    @Override
    public void updateScript(FormTemplate formTemplate, Logger logger, TAUserInfo userInfo) {
        checkScript(formTemplate, logger);
        formTemplateDao.updateScript(formTemplate.getId(), formTemplate.getScript());
        mainOperatingService.logging(formTemplate.getId(), FormDataEvent.SCRIPTS_IMPORT, userInfo.getUser());
    }

    @Override
    public Integer get(int formTypeId, int year) {
        return formTemplateDao.get(formTypeId, year);
    }

	/**
	 * Проверка синтаксиса скрипта. Выполняется перед сохранением в БД
	 * @param formTemplate
	 * @param logger
	 */
    private void checkScript(final FormTemplate formTemplate, final Logger logger) {
        if (formTemplate.getScript() == null || formTemplate.getScript().isEmpty())
            return;
        Logger tempLogger = new Logger();
        try{
            // Формируем контекст выполнения скрипта(userInfo)
            TAUserInfo userInfo = userService.getSystemUserInfo();
            // Устанавливает тестовые параметры НФ. При необходимости в скрипте значения можно поменять
            FormData formData = new FormData(formTemplate);
            formData.setState(WorkflowState.CREATED);
            formData.setDepartmentId(userInfo.getUser().getDepartmentId());
            formData.setKind(FormDataKind.PRIMARY);
            formData.setDepartmentReportPeriodId(1);
            formData.setReportPeriodId(1);

            scriptingService.executeScriptInNewReadOnlyTransaction(userInfo, formTemplate.getScript(), formData, FormDataEvent.CHECK_SCRIPT, tempLogger, null);
        } catch (Exception ex) {
            tempLogger.error(ex);
            logger.getEntries().addAll(tempLogger.getEntries());
            throw new ServiceLoggerException("Обнаружены ошибки при выполнении проверки скрипта!", logEntryService.save(logger.getEntries()));
        }
        logger.getEntries().addAll(tempLogger.getEntries());
        if (!tempLogger.getEntries().isEmpty()) {
            throw new ServiceLoggerException("Обнаружены ошибки в скрипте!", logEntryService.save(logger.getEntries()));
        }
    }

}