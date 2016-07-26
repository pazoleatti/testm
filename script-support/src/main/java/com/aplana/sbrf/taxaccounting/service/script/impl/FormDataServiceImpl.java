package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.script.FormDataCacheDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.service.script.DepartmentFormTypeService;
import com.aplana.sbrf.taxaccounting.service.script.FormDataService;
import com.aplana.sbrf.taxaccounting.service.script.RefBookService;
import com.aplana.sbrf.taxaccounting.service.script.ReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContext;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/*
 * Реализация FormDataService
 * @author auldanov
 * @author Dmitriy Levykin
 */
@Transactional(readOnly = true)
@Component("formDataService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FormDataServiceImpl implements FormDataService, ScriptComponentContextHolder, ApplicationContextAware {

    private ScriptComponentContext scriptComponentContext;

    private static final String FIND_ERROR = "FormData не сохранена, id = null.";
    private static final String CHECK_UNIQUE_ERROR = "%s с заданными параметрами уже существует!";
    private static final String REF_BOOK_ROW_NOT_FOUND_ERROR = "Строка %d, графа «%s» содержит значение, отсутствующее в справочнике «%s»!";
    private static final String REF_BOOK_NOT_FOUND_ERROR = "В справочнике «%s» не найдено значение «%s», соответствующее атрибуту «%s»!";
    private static final String WRONG_FORM_IS_NOT_ACCEPTED = "Не найдены экземпляры «%s» за %s в статусе «Принята». Расчеты не могут быть выполнены.";
    private static final String REF_BOOK_TOO_MANY_FOUND_ERROR = "В справочнике «%s» содержится более одного раза значение «%s», соответствующее атрибуту «%s»!";
    private static final String REF_BOOK_ROW_TOO_MANY_FOUND_ERROR = "Строка %d, графа «%s» содержит значение, встречающееся более одного раза в справочнике «%s»!";

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Autowired
    private FormDataDao dao;
    @Autowired
    private FormDataCacheDao cacheDao;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private RefBookService refBookService;
	@Autowired
    private ReportPeriodService reportPeriodService;
    @Autowired
    private FormTypeDao formTypeDao;
    @Autowired
    private FormTemplateDao formTemplateDao;
    @Autowired
    private TAUserDao userDao;
    @Autowired
    private SourceService sourceService;

    private Map<Number, DataRowHelper> helperHashMap = new HashMap<Number, DataRowHelper>();

    private static ApplicationContext applicationContext;

    // Объект-маркер для ускорения работы кэша с отсутствующими значениями
    private static final Long NULL_VALUE_MARKER = -1L;

    // Объект-маркер для ускорения работы кэша с дублированными значениями
    private static final Long TOO_MANY_VALUE_MARKER = -2L;

    @Override
    public FormData findMonth(int formTypeId, FormDataKind kind, int departmentId, int taxPeriodId, int periodOrder) {
        return dao.findMonth(formTypeId, kind, departmentId, taxPeriodId, periodOrder);
    }

    @Override
    public FormData find(int formTypeId, FormDataKind kind, int departmentReportPeriodId, Integer periodOrder, Integer comparativePeriodId, boolean accruing) {
        return dao.find(formTypeId, kind, departmentReportPeriodId, periodOrder, comparativePeriodId, accruing);
    }

    @Override
    public FormData getLast(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder, Integer comparativePeriodId, boolean accruing) {
        return dao.getLast(formTypeId, kind, departmentId, reportPeriodId, periodOrder, comparativePeriodId, accruing);
    }

    @Override
    public FormData getLastByDate(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder, Date correctionDate, Integer comparativePeriodId, boolean accruing) {
        return dao.getLastByDate(formTypeId, kind, departmentId, reportPeriodId, periodOrder, correctionDate, comparativePeriodId, accruing);
    }

    @Override
    public FormTemplate getFormTemplate(int formTypeId, int reportPeriodId) {
        int formTemplateId = formTemplateDao.getActiveFormTemplateId(formTypeId, reportPeriodId);
        FormTemplate formTemplate = formTemplateDao.get(formTemplateId);
        return formTemplate.clone();
    }

    @Override
    public FormTemplate getFormTemplate(int formTemplateId) {
        FormTemplate formTemplate = formTemplateDao.get(formTemplateId);
        return formTemplate.clone();
    }

    @Override
    public DataRowHelper getDataRowHelper(FormData formData) {
        if (formData.getId() == null) {
            throw new ServiceException(FIND_ERROR);
        }
        if (helperHashMap.containsKey(formData.getId())) {
            return helperHashMap.get(formData.getId());
        }
        DataRowHelperImpl dataRowHelperImpl = applicationContext.getBean(DataRowHelperImpl.class);
        dataRowHelperImpl.setFormData(formData);
        dataRowHelperImpl.setScriptComponentContext(scriptComponentContext);
        helperHashMap.put(formData.getId(), dataRowHelperImpl);
        return dataRowHelperImpl;
    }

    @Override
    public void setScriptComponentContext(ScriptComponentContext context) {
        this.scriptComponentContext = context;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void fillRefBookCache(FormData formData, Map<String, Map<String, RefBookValue>> refBookCache) {
        if (formData == null || refBookCache == null) {
            return;
        }
        refBookCache.putAll(cacheDao.getRefBookMap(formData));
    }

    @Override
    public void consolidationSimple(FormData formData, Logger logger, TAUserInfo userInfo) {
        consolidationTotal(formData, logger, userInfo, null);
    }

    @Override
    public void consolidationTotal(FormData formData, Logger logger, TAUserInfo userInfo, List<String> totalAliases) {
        DataRowHelper dataRowHelper = getDataRowHelper(formData);
        // Новый список строк
        List<DataRow<Cell>> rows = new LinkedList<DataRow<Cell>>();
        // периодичность приёмника "ежемесячно"
        boolean isFormDataMonthly = formData.getPeriodOrder() != null;
        // получаем принятые экземпляры-источники
        List<Relation> relations = sourceService.getSourcesInfo(formData, true, true, WorkflowState.ACCEPTED, userInfo, logger);
        for (Relation relation : relations) {
            FormData sourceFormData = dao.get(relation.getFormDataId(), null);
            // источник не нашелся или не в статусе "Принята"
            if (sourceFormData.getState() != WorkflowState.ACCEPTED) {
                continue;
            }
            // приёмник ежемесячный, а источник нет или наоборот
            boolean isSourceFormDataMonthly = sourceFormData.getPeriodOrder() != null;
            if ((isFormDataMonthly && !isSourceFormDataMonthly) || (!isFormDataMonthly && isSourceFormDataMonthly)) {
                continue;
            }

            // Добавление строк из источников
            for (DataRow<Cell> row : getDataRowHelper(sourceFormData).getAllSaved()) {
                if (row.getAlias() == null) {
                    rows.add(row);
                }
            }
        }

        // Добавление итоговых строк
        if (totalAliases != null) {
            for (DataRow<Cell> row : dataRowHelper.getAllCached()) {
                for (int i = 0; i < totalAliases.size(); i++) {
                    String alias = totalAliases.get(i);
                    if (alias.equals(row.getAlias())) {
                        rows.add(row);
                        totalAliases.remove(alias);
                        i--;
                    }
                }
            }
            for (String alias : totalAliases) {
                throw new IllegalArgumentException("Wrong row alias requested: " + alias);
            }
        }

        dataRowHelper.setAllCached(rows);
    }

    @Override
    public DataRow<Cell> addRow(FormData formData, DataRow<Cell> currentDataRow, List<String> editableColumns,
                                List<String> autoFillColumns) {
        DataRowHelper dataRowHelper = getDataRowHelper(formData);
        DataRow<Cell> row = formData.createDataRow();
        List<DataRow<Cell>> dataRows = dataRowHelper.getAllCached();
        int size = dataRows.size();
        int index = 0;
        // Стиль для редактируемых
        if (editableColumns != null) {
            for (String alias : editableColumns) {
                row.getCell(alias).setEditable(true);
                row.getCell(alias).setStyleAlias(EDITABLE_CELL_STYLE);
            }
        }
        // Стиль для автозаполняемых
        if (autoFillColumns != null) {
            for (String alias : autoFillColumns) {
                row.getCell(alias).setStyleAlias(AUTO_FILL_CELL_STYLE);
            }
        }

        if (currentDataRow != null) {
            index = currentDataRow.getIndex();
            DataRow<Cell> pointRow = currentDataRow;
            while (pointRow.getAlias() != null && index > 0) {
                pointRow = dataRows.get(--index);
            }
            if (index != currentDataRow.getIndex() && dataRows.get(index).getAlias() == null) {
                index++;
            }
        } else if (size > 0) {
            for (int i = size - 1; i >= 0; i--) {
                DataRow<Cell> pointRow = dataRows.get(i);
                if (pointRow.getAlias() == null) {
                    index = dataRows.indexOf(pointRow) + 1;
                    break;
                }
            }
        }
        dataRowHelper.insert(row, index + 1);
        return row;
    }

    @Override
    public RefBookDataProvider getRefBookProvider(RefBookFactory refBookFactory, Long refBookId,
                                                  Map<Long, RefBookDataProvider> providerCache) {
        if (!providerCache.containsKey(refBookId)) {
            providerCache.put(refBookId, refBookFactory.getDataProvider(refBookId));
        }
        return providerCache.get(refBookId);
    }

    /**
     * Получение записи справочника
     */
    private Map<String, RefBookValue> getRefBookRecord(Long refBookId, Map<Long, Map<String, Long>> recordCache,
                                                       Map<Long, RefBookDataProvider> providerCache,
                                                       String alias, String value, Date date) {
        return getRefBookRecord(refBookId, recordCache, providerCache, null, alias, value, date);
    }

    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Получение записи справочника
     */
    private Map<String, RefBookValue> getRefBookRecord(Long refBookId, Map<Long, Map<String, Long>> recordCache,
                                                       Map<Long, RefBookDataProvider> providerCache,
                                                       Map<String, Map<String, RefBookValue>> refBookCache,
                                                       String alias, String value, Date date) {
        // Не указали справочник
        if (refBookId == null) {
            return null;
        }

        RefBook rb = refBookFactory.get(refBookId);

        String filter;

        if (value == null || value.isEmpty()) {
            filter = alias + " is null";
        } else {
            RefBookAttributeType type = rb.getAttribute(alias).getAttributeType();
            String template;
            // TODO: поиск по выражениям с датами не реализован
            if (type == RefBookAttributeType.REFERENCE || type == RefBookAttributeType.NUMBER) {
                if (!isNumeric(value)) {
                    // В справочнике поле числовое, а у нас строка, которая не парсится — ничего не ищем выдаем ошибку
                    return null;
                }
                template = "%s = %s";
            } else {
                template = "LOWER(%s) = LOWER('%s')";
            }
            filter = String.format(template, alias, value);
        }

        String dateStr = sdf.get().format(date);

        if (recordCache.containsKey(refBookId)) {
            Long recordId = recordCache.get(refBookId).get(dateStr + filter);

            // Сравнение объектов
            if (NULL_VALUE_MARKER.equals(recordId)) {
                // Нашли маркер
                return null;
            } else if (TOO_MANY_VALUE_MARKER.equals(recordId)) {
                throw new ArrayStoreException();
            }

            if (recordId != null) {
                // Нашли в кэше
                if (refBookCache != null) {
                    return refBookCache.get(ScriptUtils.getRefBookCacheKey(refBookId, recordId));
                } else {
                    Map<String, RefBookValue> retVal = new HashMap<String, RefBookValue>();
                    retVal.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, recordId));
                    return retVal;
                }
            }
        } else {
            recordCache.put(refBookId, new HashMap<String, Long>());
        }

        // Поиск в БД
        RefBookDataProvider provider = getRefBookProvider(refBookFactory, refBookId, providerCache);
        PagingResult<Map<String, RefBookValue>> records = provider.getRecords(date, null, filter, null);
        if (records != null) {
            if (records.size() == 1) {
                Map<String, RefBookValue> retVal = records.get(0);
                Long recordId = retVal.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
                recordCache.get(refBookId).put(dateStr + filter, recordId);
                if (refBookCache != null) {
                    refBookCache.put(ScriptUtils.getRefBookCacheKey(refBookId, recordId), retVal);
                }
                return retVal;
            } else if (records.size() > 1) {
                recordCache.get(refBookId).put(dateStr + filter, TOO_MANY_VALUE_MARKER);
                throw new ArrayStoreException();
            }
        }

        // Не нашли в кэше и не нашли в БД, добавляем маркер
        recordCache.get(refBookId).put(dateStr + filter, NULL_VALUE_MARKER);
        return null;
    }

    @Override
    public Map<String, RefBookValue> getRefBookRecord(Long refBookId, Map<Long, Map<String, Long>> recordCache,
                                                      Map<Long, RefBookDataProvider> providerCache,
                                                      Map<String, Map<String, RefBookValue>> refBookCache,
                                                      String alias, String value, Date date,
                                                      int rowIndex, String columnName, Logger logger, boolean required) {
        return getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value, null, date, rowIndex, columnName, logger, required);
    }

    @Override
    public Map<String, RefBookValue> getRefBookRecord(Long refBookId, Map<Long, Map<String, Long>> recordCache,
                                                      Map<Long, RefBookDataProvider> providerCache,
                                                      Map<String, Map<String, RefBookValue>> refBookCache,
                                                      String alias, String value, String msgValue, Date date,
                                                      int rowIndex, String columnName, Logger logger, boolean required) {
        boolean tooManyValue = false;
        try {
            Map<String, RefBookValue> retVal = getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value, date);
            if (retVal != null) {
                return retVal;
            }
        } catch (ArrayStoreException ex) {
            tooManyValue = true;
        }
        RefBook rb = refBookFactory.get(refBookId);
        String msg = columnName == null ?
                String.format(tooManyValue ? REF_BOOK_TOO_MANY_FOUND_ERROR : REF_BOOK_NOT_FOUND_ERROR, rb.getName(), msgValue != null ? msgValue : value, rb.getAttribute(alias).getName()) :
                String.format(tooManyValue ? REF_BOOK_ROW_TOO_MANY_FOUND_ERROR : REF_BOOK_ROW_NOT_FOUND_ERROR, rowIndex, columnName.replaceAll("%%", "%"), rb.getName());
        if (required) {
            throw new ServiceException("%s", msg);
        } else {
            logger.warn("%s", msg);
        }
        return null;
    }

    @Override
    public Long getRefBookRecordIdImport(Long refBookId, Map<Long, Map<String, Long>> recordCache,
                                         Map<Long, RefBookDataProvider> providerCache, String alias, String value,
                                         Date date, int rowIndex, int colIndex, Logger logger, boolean required) {
        return getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value, null, date, rowIndex, colIndex, logger, required);
    }

    @Override
    public Long getRefBookRecordIdImport(Long refBookId, Map<Long, Map<String, Long>> recordCache,
                                         Map<Long, RefBookDataProvider> providerCache, String alias, String value, String msgValue,
                                         Date date, int rowIndex, int colIndex, Logger logger, boolean required) {
        if (refBookId == null) {
            return null;
        }
        boolean tooManyValue = false;
        try {
            Map<String, RefBookValue> record = getRefBookRecord(refBookId, recordCache, providerCache, alias, value, date);
            if (record != null) {
                Long retVal = record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
                if (retVal != null) {
                    return retVal;
                }
            }
        } catch (ArrayStoreException ex) {
            tooManyValue = true;
        }

        RefBook rb = refBookFactory.get(refBookId);
        String msg = String.format(tooManyValue ? ScriptUtils.REF_BOOK_TOO_MANY_FOUND_IMPORT_ERROR : ScriptUtils.REF_BOOK_NOT_FOUND_IMPORT_ERROR,
                rowIndex, ScriptUtils.getXLSColumnName(colIndex), rb.getName(), rb.getAttribute(alias).getName(),
                msgValue != null ? msgValue : value, (new SimpleDateFormat("dd.MM.yyyy")).format(date));
        if (required) {
            throw new ServiceException("%s", msg);
        } else {
            logger.warn("%s", msg);
        }
        return null;
    }

    @Override
    public Map<String, RefBookValue> getRefBookRecordImport(Long refBookId,
                                                            Map<Long, Map<String, Long>> recordCache,
                                                            Map<Long, RefBookDataProvider> providerCache,
                                                            Map<String, Map<String, RefBookValue>> refBookCache,
                                                            String alias, String value, Date date,
                                                            int rowIndex, int colIndex, Logger logger, boolean required) {
        return getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value, null, date, rowIndex, colIndex, logger, required);
    }

    @Override
    public Map<String, RefBookValue> getRefBookRecordImport(Long refBookId,
                                                            Map<Long, Map<String, Long>> recordCache,
                                                            Map<Long, RefBookDataProvider> providerCache,
                                                            Map<String, Map<String, RefBookValue>> refBookCache,
                                                            String alias, String value, String msgValue, Date date,
                                                            int rowIndex, int colIndex, Logger logger, boolean required) {
        boolean tooManyValue = false;
        try {
            Map<String, RefBookValue> record = getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value, date);
            if (record != null) {
                return record;
            }
        } catch (ArrayStoreException ex) {
            tooManyValue = true;
        }

        RefBook rb = refBookFactory.get(refBookId);
        String msg = String.format(tooManyValue ? ScriptUtils.REF_BOOK_TOO_MANY_FOUND_IMPORT_ERROR : ScriptUtils.REF_BOOK_NOT_FOUND_IMPORT_ERROR,
                rowIndex, ScriptUtils.getXLSColumnName(colIndex), rb.getName(), rb.getAttribute(alias).getName(),
                msgValue != null ? msgValue : value, (new SimpleDateFormat("dd.MM.yyyy")).format(date));
        if (required) {
            throw new ServiceException("%s", msg);
        } else {
            logger.warn("%s", msg);
        }
        return null;
    }

    @Override
    public Long getRefBookRecordId(Long refBookId, Map<Long, Map<String, Long>> recordCache,
                                   Map<Long, RefBookDataProvider> providerCache, String alias, String value, Date date,
                                   int rowIndex, String columnName, Logger logger, boolean required) {
        return getRefBookRecordId(refBookId, recordCache, providerCache, alias, value, null, date, rowIndex, columnName, logger, required);
    }

    @Override
    public Long getRefBookRecordId(Long refBookId, Map<Long, Map<String, Long>> recordCache,
                                   Map<Long, RefBookDataProvider> providerCache, String alias, String value, String msgValue, Date date,
                                   int rowIndex, String columnName, Logger logger, boolean required) {

        boolean tooManyValue = false;
        try {
            Map<String, RefBookValue> record = getRefBookRecord(refBookId, recordCache, providerCache, alias, value, date);

            if (record != null) {
                Long retVal = record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
                if (retVal != null) {
                    return retVal;
                }
            }
        } catch (ArrayStoreException ex) {
            tooManyValue = true;
        }

        RefBook rb = refBookFactory.get(refBookId);
        String msg = columnName == null ?
                String.format(tooManyValue ? REF_BOOK_TOO_MANY_FOUND_ERROR : REF_BOOK_NOT_FOUND_ERROR, rb.getName(), msgValue != null ? msgValue : value, rb.getAttribute(alias).getName()) :
                String.format(tooManyValue ? REF_BOOK_ROW_TOO_MANY_FOUND_ERROR : REF_BOOK_ROW_NOT_FOUND_ERROR, rowIndex, columnName.replaceAll("%%", "%"), rb.getName());
        if (required) {
            throw new ServiceException("%s", msg);
        } else {
            logger.warn("%s", msg);
        }
        return null;
    }

    @Override
    public Map<String, RefBookValue> getRefBookValue(long refBookId, Long recordId,
                                                     Map<String, Map<String, RefBookValue>> refBookCache) {
        if (recordId == null) {
            return null;
        }
        String key = ScriptUtils.getRefBookCacheKey(refBookId, recordId);
        if (!refBookCache.containsKey(key)) {
            refBookCache.put(key, refBookService.getRecordData(refBookId, recordId));
        }
        return refBookCache.get(key);
    }

    // Поиск предыдущей НФ относительно формы с заданными параметрами
    private FormData getFormDataPrev(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId,
                                     Integer periodOrder, Integer comparativePeriodId, boolean accruing) {
        if (periodOrder == null) {
            // Квартальная форма, берем предыдущий отчетный период
            ReportPeriod prevReportPeriod = reportPeriodService.getPrevReportPeriod(reportPeriodId);
            if (prevReportPeriod != null) {
                // Последний экземпляр
                return getLast(formTypeId, kind, departmentId, prevReportPeriod.getId(), null, comparativePeriodId, accruing);
            }
        } else {
            // Ежемесячная форма
            // Текущий отчетный период
            ReportPeriod currentPeriod = reportPeriodService.get(reportPeriodId);
            int prevMonth = periodOrder == 1 ? 12 : periodOrder - 1;

            Date periodStartDate = currentPeriod.getCalendarStartDate();
            Date monthStartDate = reportPeriodService.getMonthStartDate(reportPeriodId,
                    periodOrder).getTime();

            // Признак перехода через отчетный период — дата отчетного периода и дата месяца отличаются
            boolean overReportPeriod = periodStartDate.equals(monthStartDate);

            if (!overReportPeriod) {
                // Последний экземпляр в том же отчетном периоде, но предыдущем месяце
                return getLast(formTypeId, kind, departmentId,
                        reportPeriodId, prevMonth, comparativePeriodId, accruing);
            } else {
                // Предыдущий отчетный период
                ReportPeriod prevReportPeriod = reportPeriodService.getPrevReportPeriod(reportPeriodId);
                if (prevReportPeriod != null) {
                    // Последний экземпляр
                    return getLast(formTypeId, kind, departmentId, prevReportPeriod.getId(), prevMonth,
                            comparativePeriodId, accruing);
                }
            }
        }
        return null;
    }

    @Override
    public FormData getFormDataPrev(FormData formData) {
        if (formData == null) {
            return null;
        }
        return getFormDataPrev(formData.getFormType().getId(), formData.getKind(), formData.getDepartmentId(),
                formData.getReportPeriodId(), formData.getPeriodOrder(), formData.getComparativePeriodId(), formData.isAccruing());
    }

    @Override
    public void checkReferenceValue(Long refBookId, String referenceValue, String expectedValue, int rowIndex, int colIndex,
                                    Logger logger, boolean required) {
        List<String> expectedValues = new ArrayList<String>();
        expectedValues.add(expectedValue);
        checkReferenceValue(refBookId, referenceValue, expectedValues, rowIndex, colIndex, logger, required);
    }

    @Override
    public void checkReferenceValue(Long refBookId, String referenceValue, List<String> expectedValues, int rowIndex, int colIndex,
                                    Logger logger, boolean required) {
        if (checkReferenceValue(referenceValue, expectedValues)) {
            return;
        }
        RefBook rb = refBookFactory.get(refBookId);
        String msg = String.format(ScriptUtils.REF_BOOK_REFERENCE_NOT_FOUND_IMPORT_ERROR, rowIndex, ScriptUtils.getXLSColumnName(colIndex), referenceValue, rb.getName());
        if (required) {
            throw new ServiceException("%s", msg);
        } else {
            logger.warn("%s", msg);
        }
    }

    @Override
    public void checkReferenceValue(String referenceValue, List<String> expectedValues,
                                    String parentColumnName, String parentColumnValue,
                                    int rowIndex, int colIndex, Logger logger, boolean required) {
        if (checkReferenceValue(referenceValue, expectedValues)) {
            return;
        }
        // заменить пустые значения из базы на "значение не задано"
        Object [] tmpArray = expectedValues.toArray();
        for (int i = 0; i < tmpArray.length; i++) {
            if (tmpArray[i] == null || "".equals(tmpArray[i])) {
                tmpArray[i] = "значение не задано";
            }
        }
        // обернуть значения из базы ковычками
        String expectedValuesInStr = StringUtils.join(tmpArray, "», «", null);
        String msg = String.format(ScriptUtils.REF_BOOK_REFERENCE_NOT_FOUND_IMPORT_ERROR_2, rowIndex, ScriptUtils.getXLSColumnName(colIndex),
                referenceValue, expectedValuesInStr, parentColumnName, parentColumnValue);
        if (required) {
            throw new ServiceException("%s", msg);
        } else {
            logger.warn("%s", msg);
        }
    }

    /**
     * Проверить значение из файла и значения из базы.
     *
     * @param referenceValue значение из файла
     * @param expectedValues значение из базы
     */
    private boolean checkReferenceValue(String referenceValue, List<String> expectedValues) {
        if (expectedValues == null) {
            throw new IllegalArgumentException("expectedValues cannot be null");
        }
        if (referenceValue == null && expectedValues.isEmpty()) {
            return true;
        }
        for (String expectedValue : expectedValues) {
            if ((referenceValue == null && expectedValue == null) ||
                    (referenceValue == null && "".equals(expectedValue)) ||
                    ("".equals(referenceValue) && expectedValue == null) ||
                    (referenceValue != null && expectedValue != null && referenceValue.equalsIgnoreCase(expectedValue))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void checkFormExistAndAccepted(int formTypeId, FormDataKind kind, int departmentId,
                                          int currentReportPeriodId, Boolean prevPeriod,
                                          Logger logger, boolean required, Integer comparativePeriodId, boolean accruing) {
        // определение периода формы
        ReportPeriod reportPeriod;
        if (prevPeriod) {
            reportPeriod = reportPeriodService.getPrevReportPeriod(currentReportPeriodId);
        } else {
            reportPeriod = reportPeriodService.get(currentReportPeriodId);
        }

        // получение данных формы
        FormData formData = null;
        if (reportPeriod != null) {
            formData = getLast(formTypeId, kind, departmentId, reportPeriod.getId(), null, comparativePeriodId, accruing);
        }

        // проверка существования, принятости и наличия данных
        boolean accepted = false;
        if (formData != null && formData.getState() == WorkflowState.ACCEPTED) {
            DataRowHelper dataRowHelper = getDataRowHelper(formData);
            List<DataRow<Cell>> dataRows = dataRowHelper.getAllCached();
            accepted = !CollectionUtils.isEmpty(dataRows);
        }

        // выводить ли сообщение
        if (!accepted) {
            String formName = (formData == null ? formTypeDao.get(formTypeId).getName() : formData.getFormType().getName());
            // период может не найтись для предыдущего периода, потому что периода не существует
            String periodName = "предыдущий период";
            if (reportPeriod != null) {
                periodName = reportPeriod.getName() + " " + reportPeriod.getTaxPeriod().getYear();
            }
            String msg = String.format(WRONG_FORM_IS_NOT_ACCEPTED, formName, periodName);
            if (required) {
                throw new ServiceException("%s", msg);
            } else {
                logger.warn("%s", msg);
            }
        }
    }

    @Override
    public void checkMonthlyFormExistAndAccepted(final int formTypeId, FormDataKind kind, int departmentId,
                                                 int currentReportPeriodId, int currentPeriodOrder, boolean prevPeriod,
                                                 Logger logger, boolean required, Integer comparativePeriodId, boolean accruing) {

        FormData formData = prevPeriod ?
                getFormDataPrev(formTypeId, kind, departmentId, currentReportPeriodId, currentPeriodOrder, comparativePeriodId, accruing) :
                getLast(formTypeId, kind, departmentId, currentReportPeriodId, currentPeriodOrder, comparativePeriodId, accruing);

        int month = prevPeriod ? currentPeriodOrder - 1 : currentPeriodOrder;
        month = month == 0 ? 12 : month;

        ReportPeriod reportPeriod = prevPeriod && month == 12 ? reportPeriodService.getPrevReportPeriod(currentReportPeriodId) :
                reportPeriodService.get(currentReportPeriodId);

        // проверка существования, принятости и наличия данных
        boolean accepted = false;
        if (formData != null && formData.getState() == WorkflowState.ACCEPTED) {
            DataRowHelper dataRowHelper = getDataRowHelper(formData);
            List<DataRow<Cell>> dataRows = dataRowHelper.getAllCached();
            accepted = !CollectionUtils.isEmpty(dataRows);
        }

        // выводить ли сообщение
        if (!accepted) {
            String formName = (formData == null ? formTypeDao.get(formTypeId).getName() : formData.getFormType().getName());
            // период может не найтись для предыдущего периода, потому что периода не существует
            String monthPeriod = "предыдущий месяц";
            if (reportPeriod != null) {
                monthPeriod = Formats.getRussianMonthNameWithTier(month) + " " + reportPeriod.getTaxPeriod().getYear();
            }
            String msg = String.format(WRONG_FORM_IS_NOT_ACCEPTED, formName, monthPeriod);
            if (required) {
                throw new ServiceException("%s", msg);
            } else {
                logger.warn("%s", msg);
            }
        }
    }

    @Override
    public boolean checkUnique(FormData formData, Logger logger) {
        // поиск формы с учетом периодичности
        FormData existingFormData = dao.find(formData.getFormType().getId(), formData.getKind(),
                formData.getDepartmentReportPeriodId(), formData.getPeriodOrder(), formData.getComparativePeriodId(), formData.isAccruing());

        // форма найдена
        if (existingFormData != null) {
            logger.error(String.format(CHECK_UNIQUE_ERROR, MessageGenerator.mesSpeckSingle(formData.getFormType().getTaxType())));
            return false;
        }
        return true;
    }

    @Override
    public void saveCachedDataRows(FormData formData, Logger logger) {
        if (!logger.containsLevel(LogLevel.ERROR)) {
            DataRowHelper dataRowHelper = getDataRowHelper(formData);
            dataRowHelper.save(dataRowHelper.getAllCached());
        }
    }

    @Override
    public void saveCachedDataRows(FormData formData, Logger logger, FormDataEvent formDataEvent, ScriptStatusHolder scriptStatusHolder) {
        if (Arrays.asList(FormDataEvent.CALCULATE, FormDataEvent.REFRESH, FormDataEvent.IMPORT).contains(formDataEvent)
                || !logger.containsLevel(LogLevel.ERROR)) {
            DataRowHelper dataRowHelper = getDataRowHelper(formData);
            dataRowHelper.save(dataRowHelper.getAllCached());
            if (scriptStatusHolder != null) {
                scriptStatusHolder.setScriptStatus(ScriptStatus.SUCCESS);
            }
        } else {
            if (scriptStatusHolder != null) {
                scriptStatusHolder.setScriptStatus(ScriptStatus.SKIP);
            }
        }
    }

    @Override
    public PagingResult<TAUserView> getUsersByFilter(MembersFilterData filter) {
        return userDao.getUsersByFilter(filter);
    }

    @Override
    public List<Relation> getSourcesInfo(FormData destinationFormData, boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction, TAUserInfo userInfo, Logger logger) {
        return sourceService.getSourcesInfo(destinationFormData, light, excludeIfNotExist, stateRestriction, userInfo, logger);
    }

    @Override
    public List<Relation> getDestinationsInfo(FormData sourceFormData, boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction, TAUserInfo userInfo, Logger logger) {
        return sourceService.getDestinationsInfo(sourceFormData, light, excludeIfNotExist, stateRestriction, userInfo, logger);
    }

    @Override
    public FormData get(long formDataId, Boolean manual) {
        return dao.get(formDataId, manual);
    }
}
