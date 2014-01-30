package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.script.FormDataCacheDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.script.DepartmentFormTypeService;
import com.aplana.sbrf.taxaccounting.service.script.FormDataService;
import com.aplana.sbrf.taxaccounting.service.script.ReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.script.TaxPeriodService;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.service.script.refbook.RefBookService;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContext;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
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
    private static final String COMPOSE_SUCCESS = "Формирование консолидированной формы прошло успешно.";
    private static final String REF_BOOK_NOT_FOUND_IMPORT_ERROR = "Проверка файла: Строка %d, столбец %d содержит значение, отсутствующее в справочнике «%s»!";
    private static final String REF_BOOK_ROW_NOT_FOUND_ERROR = "Строка %d, графа «%s» содержит значение, отсутствующее в справочнике «%s»!";
    private static final String REF_BOOK_NOT_FOUND_ERROR = "В справочнике «%s» не найдено значение «%s», соответствующее атрибуту «%s»!";
    private static final String REF_BOOK_DEREFERENCE_ERROR = "Строка %d, графа «%s»: В справочнике «%s» не найден элемент с id = %d»!";
    private static final String CHECK_UNIQ_ERROR = "Налоговая форма с заданными параметрами уже существует!";
    private static final String CHECK_BALANCE_PERIOD_ERROR = "Налоговая форма не может создаваться в периоде ввода остатков!";

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private FormDataDao dao;

    @Autowired
    private FormDataCacheDao cacheDao;

    @Autowired
    private DepartmentFormTypeService departmentFormTypeService;

    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private RefBookService refBookService;

    @Autowired
    private ReportPeriodService reportPeriodService;

    @Autowired
    private TaxPeriodService taxPeriodService;

    private Map<Number, DataRowHelper> helperHashMap = new HashMap<Number, DataRowHelper>();

    private static ApplicationContext applicationContext;

    // Объект-маркер для ускорения работы кэша с отсутствующими значениями
    private static final Long NULL_VALUE_MARKER = -1L;

    @Override
    public FormData find(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId) {
        return dao.find(formTypeId, kind, departmentId, reportPeriodId);
    }

    @Override
    public FormData findMonth(int formTypeId, FormDataKind kind, int departmentId, int taxPeriodId, int periodOrder) {
        return dao.findMonth(formTypeId, kind, departmentId, taxPeriodId, periodOrder);
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
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void fillRefBookCache(Long formDataId, Map<Long, Map<String, RefBookValue>> refBookCache) {
        if (formDataId == null || refBookCache == null) {
            return;
        }
        refBookCache.putAll(cacheDao.getRefBookMap(formDataId));
    }

    @Override
    public void consolidationSimple(FormData formData, int departmentId, Logger logger) {
        consolidationTotal(formData, departmentId, logger, null);
    }

    @Override
    public void consolidationTotal(FormData formData, int departmentId, Logger logger, List<String> totalAliases){
        DataRowHelper dataRowHelper = getDataRowHelper(formData);
        // Новый список строк
        List<DataRow<Cell>> rows = new LinkedList<DataRow<Cell>>();
        // НФ назначения
        List<DepartmentFormType> typeList = departmentFormTypeService.getFormSources(departmentId,
                formData.getFormType().getId(), formData.getKind());
        // периодичность приёмника "ежемесячно"
        boolean isFormDataMonthly = formData.getPeriodOrder() != null;

        for (DepartmentFormType type : typeList) {
            // поиск источника с учетом периодичности
            FormData sourceFormData;
            if (!isFormDataMonthly) {
                sourceFormData = find(type.getFormTypeId(), type.getKind(), type.getDepartmentId(),
                        formData.getReportPeriodId());
            } else {
                Integer taxPeriodId = reportPeriodService.get(formData.getReportPeriodId()).getTaxPeriod().getId();
                sourceFormData = findMonth(type.getFormTypeId(), type.getKind(), type.getDepartmentId(),
                        taxPeriodId, formData.getPeriodOrder());
            }

            // источник не нашелся или не в статусе "Принята"
            if (sourceFormData == null || sourceFormData.getState() != WorkflowState.ACCEPTED) {
                continue;
            }
            // приёмник ежемесячный, а источник нет или наоборот
            boolean isSourceFormDataMonthly = sourceFormData.getPeriodOrder() != null;
            if ((isFormDataMonthly && !isSourceFormDataMonthly) || (!isFormDataMonthly && isSourceFormDataMonthly)) {
                continue;
            }

            // Добавление строк из источников
            for (DataRow<Cell> row : getDataRowHelper(sourceFormData).getAll()) {
                if (row.getAlias() == null) {
                    rows.add(row);
                }
            }
        }

        // Добавление итоговых строк
        if (totalAliases != null) {
            for (DataRow<Cell> row : dataRowHelper.getAllCached()) {
                for (int i=0;i<totalAliases.size();i++) {
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

        dataRowHelper.save(rows);
        if (logger != null) {
            logger.info(COMPOSE_SUCCESS);
        }
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

    /**
     * Получение записи справочника
     */
    private Map<String, RefBookValue> getRefBookRecord(Long refBookId, Map<Long, Map<String, Long>> recordCache,
                                                       Map<Long, RefBookDataProvider> providerCache,
                                                       Map<Long, Map<String, RefBookValue>> refBookCache,
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
                template = "%s = %s";
            } else {
                template = "LOWER(%s) = LOWER('%s')";
            }
            filter = String.format(template, alias, value);
        }

        String dateStr = sdf.format(date);

        if (recordCache.containsKey(refBookId)) {
            Long recordId = recordCache.get(refBookId).get(dateStr + filter);

            // Сравнение объектов
            if (recordId == NULL_VALUE_MARKER) {
                // Нашли маркер
                return null;
            }

            if (recordId != null) {
                // Нашли в кэше
                if (refBookCache != null) {
                    return refBookCache.get(recordId);
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
        if (records.size() == 1) {
            Map<String, RefBookValue> retVal = records.get(0);
            Long recordId = retVal.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
            recordCache.get(refBookId).put(dateStr + filter, recordId);
            if (refBookCache != null) {
                refBookCache.put(recordId, retVal);
            }
            return retVal;
        }

        // Не нашли в кэше и не нашли в БД, добавляем маркер
        recordCache.get(refBookId).put(dateStr + filter, NULL_VALUE_MARKER);
        return null;
    }

    @Override
    public Map<String, RefBookValue> getRefBookRecord(Long refBookId, Map<Long, Map<String, Long>> recordCache,
                                                      Map<Long, RefBookDataProvider> providerCache,
                                                      Map<Long, Map<String, RefBookValue>> refBookCache,
                                                      String alias, String value, Date date,
                                                      int rowIndex, String columnName, Logger logger, boolean required) {
        Map<String, RefBookValue> retVal = getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value, date);
        if (retVal != null) {
            return retVal;
        }
        RefBook rb = refBookFactory.get(refBookId);
        String msg = columnName == null ?
                String.format(REF_BOOK_NOT_FOUND_ERROR, rb.getName(), value, rb.getAttribute(alias).getName()) :
                String.format(REF_BOOK_ROW_NOT_FOUND_ERROR, rowIndex, columnName, rb.getName());
        if (required) {
            throw new ServiceException(msg);
        } else {
            logger.warn(msg);
        }
        return null;
    }

    @Override
    public Long getRefBookRecordIdImport(Long refBookId, Map<Long, Map<String, Long>> recordCache,
                                         Map<Long, RefBookDataProvider> providerCache, String alias, String value,
                                         Date date, int rowIndex, int colIndex, Logger logger, boolean required) {
        Map<String, RefBookValue> record = getRefBookRecord(refBookId, recordCache, providerCache, alias, value, date);

        if (record != null) {
            Long retVal = record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
            if (retVal != null) {
                return retVal;
            }
        }
        RefBook rb = refBookFactory.get(refBookId);
        String msg = String.format(REF_BOOK_NOT_FOUND_IMPORT_ERROR, rowIndex, colIndex, rb.getName());
        if (required) {
            throw new ServiceException(msg);
        } else {
            logger.warn(msg);
        }
        return null;
    }

    @Override
    public Long getRefBookRecordId(Long refBookId, Map<Long, Map<String, Long>> recordCache,
                                   Map<Long, RefBookDataProvider> providerCache, String alias, String value, Date date,
                                   int rowIndex, String columnName, Logger logger, boolean required) {
        Map<String, RefBookValue> record = getRefBookRecord(refBookId, recordCache, providerCache, alias, value, date);

        if (record != null) {
            Long retVal = record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
            if (retVal != null) {
                return retVal;
            }
        }
        RefBook rb = refBookFactory.get(refBookId);
        String msg = columnName == null ?
                String.format(REF_BOOK_NOT_FOUND_ERROR, rb.getName(), value, rb.getAttribute(alias).getName()) :
                String.format(REF_BOOK_ROW_NOT_FOUND_ERROR, rowIndex, columnName, rb.getName());
        if (required) {
            throw new ServiceException(msg);
        } else {
            logger.warn(msg);
        }
        return null;
    }

    @Override
    public Map<String, RefBookValue> getRefBookValue(long refBookId, Long recordId,
                                                     Map<Long, Map<String, RefBookValue>> refBookCache) {
        if (recordId == null) {
            return null;
        }
        if (!refBookCache.containsKey(recordId)) {
            refBookCache.put(recordId, refBookService.getRecordData(refBookId, recordId));
        }
        return refBookCache.get(recordId);
    }

    @Override
    public boolean checkNSI(long refBookId, Map<Long, Map<String, RefBookValue>> refBookCache, DataRow<Cell> row,
                            String alias, Logger logger, boolean required) {
        if (row == null || alias == null) {
            return true;
        }
        Cell cell = row.getCell(alias);
        if (cell == null || cell.getValue() == null) {
            return true;
        }
        Object value = cell.getValue();
        if (value instanceof BigDecimal && getRefBookValue(refBookId,
                ((BigDecimal) value).longValue(), refBookCache) == null) {
            RefBook rb = refBookFactory.get(refBookId);
            String msg = String.format(REF_BOOK_DEREFERENCE_ERROR, row.getIndex(), cell.getColumn().getName(),
                    rb.getName(), value);
            if (required) {
                logger.error(msg);
            } else {
                logger.warn(msg);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean checkUnique(FormData formData, Logger logger) {
        // поиск формы с учетом периодичности
        FormData existingFormData;
        if (formData.getPeriodOrder() == null) {
            existingFormData = find(formData.getFormType().getId(), formData.getKind(), formData.getDepartmentId(),
                    formData.getReportPeriodId());
        } else {
            Integer taxPeriodId = reportPeriodService.get(formData.getReportPeriodId()).getTaxPeriod().getId();
            existingFormData = findMonth(formData.getFormType().getId(), formData.getKind(), formData.getDepartmentId(),
                    taxPeriodId, formData.getPeriodOrder());
        }
        // форма найдена
        if (existingFormData != null) {
            logger.error(CHECK_UNIQ_ERROR);
            return false;
        }
        return true;
    }

    @Override
    public boolean checksBalancePeriod(FormData formData, Logger logger) {
        ReportPeriod reportPeriod = reportPeriodService.get(formData.getReportPeriodId());
        if (reportPeriod != null && reportPeriodService.isBalancePeriod(formData.getReportPeriodId(),
                formData.getDepartmentId())) {
            logger.error(CHECK_BALANCE_PERIOD_ERROR);
            return false;
        }
        return true;
    }

    @Override
    public FormData getFormDataPrev(FormData formData, int departmentId) {
        if (formData == null) {
            return null;
        }
        if (formData.getPeriodOrder() == null) {
            // Квартальная форма
            ReportPeriod prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.getReportPeriodId());
            if (prevReportPeriod != null) {
                return find(formData.getFormType().getId(), formData.getKind(), departmentId, prevReportPeriod.getId());
            }
        } else {
            // Ежемесячная форма
            int month;
            ReportPeriod currentPeriod = reportPeriodService.get(formData.getReportPeriodId());
            TaxPeriod taxPeriod = currentPeriod.getTaxPeriod();

            if (formData.getPeriodOrder() == 1) {
                // Переход через год
                month = 12;
                List<TaxPeriod> taxPeriodList = taxPeriodService.listByTaxType(currentPeriod.getTaxPeriod().getTaxType());
                int currentIndex = -1;
                for (int i = 0; i < taxPeriodList.size(); i++) {
                    if (taxPeriodList.get(i).getId().equals(taxPeriod.getId())) {
                        currentIndex = i;
                        break;
                    }
                }
                if (currentIndex == 0 || currentIndex == -1) {
                    return null;
                }
                taxPeriod = taxPeriodList.get(currentIndex - 1);
            } else {
                month = formData.getPeriodOrder() - 1;
            }
            return findMonth(formData.getFormType().getId(), formData.getKind(), departmentId, taxPeriod.getId(), month);
        }
        return null;
    }

    @Override
    public BigDecimal getPrevRowNumber(FormData formData, int departmentId, String alias) {
        ReportPeriod reportPeriod = reportPeriodService.get(formData.getReportPeriodId());
        if (reportPeriod != null && reportPeriod.getOrder() == 1) {
            return BigDecimal.ZERO;
        }
        BigDecimal rowNumber = BigDecimal.ZERO;
        FormData prevFormData = getFormDataPrev(formData, departmentId);
        List<DataRow<Cell>> prevDataRows = (prevFormData != null ? getDataRowHelper(prevFormData).getAllCached() : null);
        if (prevDataRows != null && !prevDataRows.isEmpty()) {
            for (int i = prevDataRows.size() - 1; i >= 0; i--) {
                DataRow<Cell> row = prevDataRows.get(i);
                if (row.getAlias() == null) {
                    Object value = row.getCell(alias).getValue();
                    if (value instanceof BigDecimal) {
                        rowNumber = (BigDecimal) value;
                    }
                    break;
                }
            }
        }
        return rowNumber == null ? BigDecimal.ZERO : rowNumber;
    }

    @Override
    public boolean existAcceptedFormDataPrev(FormData formData, int departmentId) {
        FormData prevFormData = getFormDataPrev(formData, departmentId);
        if (prevFormData != null && prevFormData.getState() == WorkflowState.ACCEPTED) {
            DataRowHelper dataRowHelper = getDataRowHelper(prevFormData);
            List<DataRow<Cell>> prevDataRows = dataRowHelper.getAllCached();
            return !CollectionUtils.isEmpty(prevDataRows);
        }
        return false;
    }
}
