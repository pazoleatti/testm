package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.script.FormDataCacheDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.script.DepartmentFormTypeService;
import com.aplana.sbrf.taxaccounting.service.script.FormDataService;
import com.aplana.sbrf.taxaccounting.service.script.ReportPeriodService;
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

import java.math.BigDecimal;
import java.util.*;

/*
 * Реализация FormDataService
 * @author auldanov
 */
@Transactional(readOnly = true)
@Component("formDataService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FormDataServiceImpl implements FormDataService, ScriptComponentContextHolder, ApplicationContextAware {

    private ScriptComponentContext scriptComponentContext;

    private static final String FIND_ERROR = "FormData не сохранена, id = null.";
    private static final String COMPOSE_SUCCESS = "Формирование консолидированной формы прошло успешно.";
    private static final String REF_BOOK_NOT_FOUND_IMPORT_ERROR = "Строка %d, колонка %d содержит значение, отсутствующее в справочнике «%s»!";
    private static final String REF_BOOK_ROW_NOT_FOUND_ERROR = "Строка %d, графа «%s» содержит значение, отсутствующее в справочнике «%s»!";
    private static final String REF_BOOK_NOT_FOUND_ERROR = "В справочнике «%s» не найдено значение «%s», соответствующее атрибуту «%s»!";
    private static final String REF_BOOK_DEREFERENCE_ERROR = "Строка %d, графа «%s»: В справочнике «%s» не найден элемент с id = %d»!";
    private static final String CHECK_UNIQ_ERROR = "Налоговая форма с заданными параметрами уже существует!";
    private static final String CHECK_BALANCE_PERIOD_ERROR = "Налоговая форма не может создаваться в периоде ввода остатков!";

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

    private HashMap<Number, DataRowHelper> helperHashMap = new HashMap<Number, DataRowHelper>();

    private static ApplicationContext applicationContext;

    @Override
    public FormData find(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId) {
        return dao.find(formTypeId, kind, departmentId, reportPeriodId);
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
        DataRowHelper dataRowHelper = getDataRowHelper(formData);
        // Новый список строк
        List<DataRow<Cell>> rows = new LinkedList<DataRow<Cell>>();
        // НФ назначения
        List<DepartmentFormType> typeList = departmentFormTypeService.getFormSources(departmentId,
                formData.getFormType().getId(), formData.getKind());

        for (DepartmentFormType type : typeList) {
            FormData sourceFormData = find(type.getFormTypeId(), type.getKind(), type.getDepartmentId(),
                    formData.getReportPeriodId());

            if (sourceFormData != null && sourceFormData.getState() == WorkflowState.ACCEPTED) {
                for (DataRow<Cell> row : getDataRowHelper(sourceFormData).getAll()) {
                    if (row.getAlias() == null) {
                        // Добавление строк из источников
                        rows.add(row);
                    }
                }
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
     * Получение Id записи справочника
     * @param refBookId
     * @param recordCache
     * @param providerCache
     * @param alias
     * @param value
     * @param date
     * @return
     */
    private Long getRefBookRecordId(Long refBookId,
                                    Map<Long, Map<String, Long>> recordCache,
                                    Map<Long, RefBookDataProvider> providerCache, String alias,
                                    String value, Date date) {
        if (refBookId == null) {
            return null;
        }

        String filter = value == null || value.isEmpty() ? alias + " is null" :
                "LOWER(" + alias + ") = LOWER('" + value + "')";

        if (recordCache.containsKey(refBookId)) {
            Long retVal = recordCache.get(refBookId).get(filter);
            if (retVal != null) {
                return retVal;
            }
        } else {
            recordCache.put(refBookId, new HashMap<String, Long>());
        }

        RefBookDataProvider provider = getRefBookProvider(refBookFactory, refBookId, providerCache);
        PagingResult<Map<String, RefBookValue>> records = provider.getRecords(date, null, filter, null);
        if (records.size() == 1) {
            Long retVal = records.get(0).get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
            recordCache.get(refBookId).put(filter, retVal);
            return retVal;
        }
        return null;
    }

    @Override
    public Long getRefBookRecordIdImport(Long refBookId, Map<Long, Map<String, Long>> recordCache,
                                   Map<Long, RefBookDataProvider> providerCache, String alias, String value, Date date,
                                   int rowIndex, int colIndex, Logger logger, boolean required) {
        Long retVal = getRefBookRecordId(refBookId, recordCache, providerCache, alias, value, date);
        if (retVal != null) {
            return retVal;
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
        Long retVal = getRefBookRecordId(refBookId, recordCache, providerCache, alias, value, date);
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
        if (find(formData.getFormType().getId(), formData.getKind(), formData.getDepartmentId(),
                formData.getReportPeriodId()) != null) {
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
}
