package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.ObjectLockDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.service.FormDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Реализация сервиса для работы с шаблонами налоговых форм
 * TODO: сейчас это просто тонкая обёртка над FormTemplateDao, нужно дополнить её 
 * 	- вызовом механизма блокировок
 *  - проверкой прав доступа пользователя
 *  - возможно, валидацией сохраняемых данных
 * @author dsultanbekov
 */
@Service
@Transactional
public class FormTemplateServiceImpl implements FormTemplateService {
	//private static final int FORM_VERSION_MAX_VALUE = 20;
	private static final int FORM_STYLE_ALIAS_MAX_VALUE = 40;
	private static final int FORM_COLUMN_NAME_MAX_VALUE = 1000;
	private static final int FORM_COLUMN_ALIAS_MAX_VALUE = 100;
	//TODO: надо подумать как хендлить длину строковой ячейки и нужно ли это тут
	//private static final int FORM_COLUMN_CHK_MAX_VALUE = 500;
	private static final int DATA_ROW_ALIAS_MAX_VALUE = 20;

	private Set<String> checkSet = new HashSet<String>();

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
	private FormTemplateDao formTemplateDao;
	@Autowired
	private ObjectLockDao lockDao;
    @Autowired
    private FormDataScriptingService scriptingService;
    @Autowired
    TAUserService userService;
    @Autowired
    FormDataDao formDataDao;
    @Autowired
    LogEntryService logEntryService;

    @Autowired
    ReportPeriodDao reportPeriodDao;

	@Override
	public List<FormTemplate> listAll() {
        return formTemplateDao.listAll();
	}

	@Override
	public FormTemplate get(int formTemplateId) {
        try {
            return formTemplateDao.get(formTemplateId);
        }catch (DaoException e){
            throw new ServiceException("Обновление статуса версии.", e);
        }
	}

    @Transactional(readOnly = false)
	@Override
	public int save(FormTemplate formTemplate) {
        if (formTemplate.getId() != null)
		    return formTemplateDao.save(formTemplate);
        else
            return formTemplateDao.saveNew(formTemplate);
	}



    @Override
	public int getActiveFormTemplateId(int formTypeId, int reportPeriodId) {
        try {
            return formTemplateDao.getActiveFormTemplateId(formTypeId, reportPeriodId);
        } catch (DaoException e){
			logger.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при получении активного шаблона НФ.", e);
        }

	}

	@Override
	public void checkLockedByAnotherUser(Integer formTemplateId, TAUserInfo userInfo){
		if (formTemplateId!=null){
			ObjectLock<Integer> objectLock = lockDao.getObjectLock(formTemplateId, FormTemplate.class);
			if(objectLock != null && objectLock.getUserId() != userInfo.getUser().getId()){
				throw new AccessDeniedException("Шаблон формы заблокирован другим пользователем");
			}
		}
	}

    @Override
    public void executeTestScript(FormTemplate formTemplate) {
		// Создаем тестового пользователя
		TAUser user = new TAUser();
		user.setId(1);
		user.setName("Василий Пупкин");
		user.setActive(true);
		user.setDepartmentId(1);
		user.setLogin("vpupkin");
		user.setEmail("vpupkin@aplana.com");

        //Формируем контекст выполнения скрипта(userInfo)
        TAUserInfo userInfo = new TAUserInfo();
		userInfo.setUser(user);
        userInfo.setIp("127.0.0.1");

		// Устанавливает тестовые параметры НФ. При необходимости в скрипте значения можно поменять
        FormData formData = new FormData(formTemplate);
        formData.setState(WorkflowState.CREATED);
        formData.setDepartmentId(userInfo.getUser().getDepartmentId());
        formData.setKind(FormDataKind.PRIMARY);
        formData.setReportPeriodId(1);

        /*formTemplateDao.saveForm(formTemplate);
        logger.info("formTemplate is saved with body-text for testing");*/
        Logger log = new Logger();
        scriptingService.executeScript(userInfo, formData, FormDataEvent.TEST, log, null);
        if(!log.getEntries().isEmpty())
        {
            StringBuilder sb = new StringBuilder("В скрипте найдены ошибки: ");
            for(LogEntry logEntry : log.getEntries())
                sb.append(logEntry.getMessage());
            throw new ServiceException(sb.toString());
        }
        logger.info("Script has been executed successful.");
        throw new ServiceException("Скрипт выполнен успешно.");
    }

    @Override
    public String getFormTemplateScript(int formTemplateId, Logger logger) {
        try {
            return formTemplateDao.getFormTemplateScript(formTemplateId);
        } catch (DaoException e){
            this.logger.error("Ошибка получение НФ.", e);
            logger.error("Ошибка получение НФ.", e.getLocalizedMessage());
            return "";
        }

    }

    @Override
    public FormTemplate getFullFormTemplate(int formTemplateId) {
        try {
            FormTemplate formTemplate = formTemplateDao.get(formTemplateId);
            if(formTemplate.getRows().isEmpty()){
                formTemplate.getRows().addAll(formTemplateDao.getDataCells(formTemplate));
            }
            if (formTemplate.getHeaders().isEmpty()){
                formTemplate.getHeaders().addAll(formTemplateDao.getHeaderCells(formTemplate));
                FormDataUtils.setValueOners(formTemplate.getHeaders());
            }
            return formTemplate;
        } catch (DaoException e){
            logger.error("Ошибка при получении шаблона НФ.", e);
            throw new ServiceException("Ошибка при получении шаблона НФ.", e);
        }
    }

    @Override
    public FormTemplate getFullFormTemplate(int formTemplateId, Logger logger) {
        FormTemplate formTemplate = formTemplateDao.get(formTemplateId);
        if(formTemplate.getRows().isEmpty()){
            try {
                formTemplate.getRows().addAll(formTemplateDao.getDataCells(formTemplate));
            } catch (DaoException e){
                this.logger.error("Ошибка при получении начальных данных шаблона НФ.", e);
                logger.error("Ошибка при получении графы \"Начальные данные\" шаблона НФ. %s", e.getMessage());
            }
        }
        if (formTemplate.getHeaders().isEmpty()){
            try {
                formTemplate.getHeaders().addAll(formTemplateDao.getHeaderCells(formTemplate));
                FormDataUtils.setValueOners(formTemplate.getHeaders());
            } catch (DaoException e){
                this.logger.error("Ошибка при получении заголовков шаблона НФ.", e);
                logger.error("Ошибка при получении графы \"Заголовки\" заголовков шаблона НФ. %s", e.getMessage());
            }
        }
        return formTemplate;
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
    public List<VersionSegment> findFTVersionIntersections(int templateId, int typeId, Date actualBeginVersion, Date actualEndVersion) {
        return formTemplateDao.findFTVersionIntersections(typeId, templateId, actualBeginVersion, actualEndVersion);
    }

    @Override
    public int delete(int formTemplateId) {
        return formTemplateDao.delete(formTemplateId);
    }

    @Override
    public FormTemplate getNearestFTRight(int formTemplateId, VersionedObjectStatus... status) {
        FormTemplate formTemplate = formTemplateDao.get(formTemplateId);

        //formTemplate.setVersion(addCalendar(Calendar.DAY_OF_YEAR, 1, formTemplate.getVersion()));
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
		ObjectLock<Integer> objectLock = lockDao.getObjectLock(formTemplateId, FormTemplate.class);
		if(objectLock != null && objectLock.getUserId() != userInfo.getUser().getId()){
			return false;
		} else {
			lockDao.lockObject(formTemplateId, FormTemplate.class, userInfo.getUser().getId());
			return true;
		}
	}

	@Override
	public boolean unlock(int formTemplateId, TAUserInfo userInfo){
		ObjectLock<Integer> objectLock = lockDao.getObjectLock(formTemplateId, FormTemplate.class);
		if(objectLock != null && objectLock.getUserId() != userInfo.getUser().getId()){
			return false;
		} else {
			lockDao.unlockObject(formTemplateId, FormTemplate.class, userInfo.getUser().getId());
			return true;
		}
	}

	@Override
	public void validateFormTemplate(FormTemplate formTemplate, Logger logger) {
		//TODO: подумать над обработкой уникальности версии, на данный момент версия не меняется

		validateFormColumns(formTemplate, logger);
		validateFormStyles(formTemplate.getStyles(), logger);
		validateFormRows(formTemplate.getRows(), logger);
        validateFormAutoNumerationColumn(formTemplate, logger);
    }

	private void validateFormColumns(FormTemplate formTemplate, Logger logger) {
        List<Column> columns = formTemplate.getColumns();
        Integer formTemplateTypeId = formTemplate.getType().getId();

        checkSet.clear();

        for (Column column : columns) {
            if (!checkSet.add(column.getAlias())) {
                logger.error("найден повторяющийся алиас \" " + column.getAlias() +
                        "\" для столбца " + column.getName());
            }

            if (column.getName() != null && column.getName().getBytes().length > FORM_COLUMN_NAME_MAX_VALUE) {
                logger.error("значение для имени столбца \"" + column.getName() +
                        "\" слишком велико (фактическое: " + column.getName().getBytes().length +
                        ", максимальное: " + FORM_COLUMN_NAME_MAX_VALUE + ")");
            }
            if (column.getAlias() != null && column.getAlias().getBytes().length > FORM_COLUMN_ALIAS_MAX_VALUE) {
                logger.error("значение для алиаса столбца \"" + column.getAlias() +
                        "\" слишком велико (фактическое: " + column.getAlias().getBytes().length
                        + ", максимальное: " + FORM_COLUMN_ALIAS_MAX_VALUE + ")");
            }

            if (column instanceof StringColumn && ((StringColumn) column).getMaxLength() < ((StringColumn) column).getPrevLength()) {
                List<String> formDataList = formDataDao.getStringList(column.getId(), formTemplateTypeId);
                for (String string : formDataList) {
                    if (string != null && string.length() > ((StringColumn) column).getMaxLength()) {
                        logger.error("Длина одного из существующих значений графы '" + column.getName() + "' больше указанной длины " + ((StringColumn) column).getPrevLength());
                        break;
                    }
                }
            }
        }
    }

	private void validateFormRows(List<DataRow<Cell>> rows, Logger logger) {
		//TODO: подумать о уникальности порядка строк
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
				logger.error("найден повторяющийся алиас стиля " + style.getAlias());
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
    public boolean isMonthly(int formId) {
        FormTemplate formTemplate = formTemplateDao.get(formId);
        return formTemplate.isMonthly();
    }

    /**
     * Валидация сквозной нумерации
     * @param formTemplate макета НФ
     */
    protected void validateFormAutoNumerationColumn(FormTemplate formTemplate, Logger logger) {

        Integer formTemplateId = formTemplate.getId();

        // Если есть хоть одна автонумеруемая графа
        if (isAnyAutoNumerationColumn(formTemplate, AutoNumerationColumnType.CROSS.getType())) {
            // Проверяем наличие в версии макета до редактирования хотя бы одной автонумеруемой графы, у которой "Тип нумерации строк" != "Сквозная".
            FormTemplate fullFormTemplate = getFullFormTemplate(formTemplateId);
            if (isAnyAutoNumerationColumn(fullFormTemplate, AutoNumerationColumnType.SERIAL.getType())) {
                List<ReportPeriod> reportPeriodList = reportPeriodDao.getClosedPeriodsForFormTemplate(formTemplateId);

                if (reportPeriodList.size() != 0) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < reportPeriodList.size(); i++) {
                        stringBuilder.append(reportPeriodList.get(i).getName()).append(" ");
                        stringBuilder.append(reportPeriodList.get(i).getTaxPeriod().getYear());
                        if (i < reportPeriodList.size() - 1) {
                            stringBuilder.append(", ");
                        }
                    }
                    logger.error("Следующие периоды налоговых форм данной версии макета закрыты: " +
                            stringBuilder.toString() + ". " +
                            "Для добавления в макет автонумеруемой графы с типом сквозной нумерации строк необходимо открыть перечисленные периоды!");
                } else {

                    /*
                     * Здесь обновляем значение атрибута "Номер последней строки предыдущей НФ".
                     * http://conf.aplana.com/pages/viewpage.action?pageId=11377661 п.7А.1.1.1
                     */
                }
            }
        }
    }

    /**
     * Есть ли хоть одна автонумеруемая графа указанного типа в макете
     *
     * @param formTemplate макета НФ
     * @param type      тип нумерации
     * @return true - есть, false - нет
     */
    protected boolean isAnyAutoNumerationColumn(FormTemplate formTemplate, int type) {
        List<Column> columns = formTemplate.getColumns();
        for (Column column : columns) {
            if (column instanceof AutoNumerationColumn) {
                return (((AutoNumerationColumn) column).getType() == (type));
            }
        }
        return false;
    }
}
