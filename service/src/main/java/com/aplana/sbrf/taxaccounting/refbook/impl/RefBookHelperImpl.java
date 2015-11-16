package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: avanteev
 */
@Service
@Transactional
public class RefBookHelperImpl implements RefBookHelper {

	@Autowired
	private RefBookFactory refBookFactory;

    @Autowired
	private ColumnDao columnDao;

    @Autowired
    private PeriodService periodService;



    @Override
    public void dataRowsCheck(Collection<DataRow<Cell>> dataRows, List<Column> columns) {
        Map<Long, Pair<RefBookDataProvider, RefBookAttribute>> providers = new HashMap<Long, Pair<RefBookDataProvider, RefBookAttribute>>();
        try {
            for (DataRow<Cell> dataRow : dataRows) {
                for (Map.Entry<String, Object> entry : dataRow.entrySet()) {
                    Cell cell = ((DataRow.MapEntry<Cell>) entry).getCell();
                    Object value = cell.getValue();
                    if (ColumnType.REFBOOK.equals(cell.getColumn().getColumnType()) && value != null) {
                        // Разыменование справочных ячеек
                        RefBookColumn column = (RefBookColumn) cell.getColumn();
                        Long refAttributeId = column.getRefBookAttributeId();
                        checkRefBookValue(providers, refAttributeId, value);
                    } else if (ColumnType.REFERENCE.equals(cell.getColumn().getColumnType())) {
                        // Разыменование ссылочных ячеек
                        ReferenceColumn column = (ReferenceColumn) cell.getColumn();
                        Cell parentCell = dataRow.getCellByColumnId(column.getParentId());
                        value = parentCell.getValue();
                        if (value != null) {
                            checkReferenceValue(providers, column, value);
                        }
                    }
                }
            }
        } catch (DaoException e) {
            throw new ServiceException("Данные не могут быть сохранены, так как часть выбранных справочных значений была удалена. Отредактируйте таблицу и попытайтесь сохранить заново", e);
        }
    }


    private void checkReferenceValue (Map<Long, Pair<RefBookDataProvider, RefBookAttribute>> providers,
                                            ReferenceColumn referenceColumn, Object value){
        Long refAttributeId = referenceColumn.getRefBookAttributeId();
        Pair<RefBookDataProvider, RefBookAttribute> pair = providers.get(refAttributeId);
        if (pair == null) {
            RefBook refBook = refBookFactory.getByAttribute(refAttributeId);
            RefBookAttribute attribute = refBook.getAttribute(refAttributeId);
            RefBookDataProvider provider = refBookFactory.getDataProvider(refBook.getId());
            pair = new Pair<RefBookDataProvider, RefBookAttribute>(provider, attribute);
            providers.put(refAttributeId, pair);
        }
        Map<String, RefBookValue> record = pair.getFirst().getRecordData((Long) value);
        RefBookValue refBookValue = record.get(pair.getSecond().getAlias());
        // Если найденое значение ссылка, то делаем разыменование для 2 уровня
        if ((refBookValue.getReferenceValue()!=null)&&(refBookValue.getAttributeType()==RefBookAttributeType.REFERENCE)&&(referenceColumn.getRefBookAttributeId2()!=null)){
            refAttributeId = referenceColumn.getRefBookAttributeId2();
            RefBook refBook = refBookFactory.getByAttribute(refAttributeId);
            RefBookAttribute attribute = refBook.getAttribute(refAttributeId);
            RefBookDataProvider provider = refBookFactory.getDataProvider(refBook.getId());
            pair = new Pair<RefBookDataProvider, RefBookAttribute>(provider, attribute);
            providers.put(refAttributeId, pair);
            pair.getFirst().getRecordData(refBookValue.getReferenceValue());
        }
    }

    private void checkRefBookValue(Map<Long, Pair<RefBookDataProvider, RefBookAttribute>> providers,
                                         Long refAttributeId, Object value) {
        Pair<RefBookDataProvider, RefBookAttribute> pair = providers.get(refAttributeId);
        if (pair == null) {
            RefBook refBook = refBookFactory.getByAttribute(refAttributeId);
            RefBookAttribute attribute = refBook.getAttribute(refAttributeId);
            RefBookDataProvider provider = refBookFactory.getDataProvider(refBook.getId());
            pair = new Pair<RefBookDataProvider, RefBookAttribute>(provider, attribute);
            providers.put(refAttributeId, pair);
        }
        pair.getFirst().getRecordData((Long) value);
    }

	/**
	 * Ищет в списке графу по идентификатору
	 * @param columnId идентификатор графы
	 * @param columns список граф
	 * @return
	 */
	private Column getColumnById(int columnId, List<Column> columns) {
		for (Column column : columns) {
			if (column.getId().equals(columnId)) {
				return column;
			}
		}
		throw new IllegalArgumentException("Attribute not found");
	}

	/**
	 * Групповое разыменование ссылок
	 *
	 * @param column графа для установки разыменованных значений
	 * @param parentColumn родительская графа, актуальна для зависимых граф для первого уровня разыменовывания
	 * @param attributeId атрибут справочника по которому хотим разыменовать ссылки
	 * @param dataRows куда будем записывать итоговый результат
	 * @param recordIds список ссылок для разыменования
	 * @param isLevel2 признак разименования второго уровня (справочной или зависимой ячейки)
	 */
	private void dereference(Column column, Column parentColumn, Long attributeId, Collection<DataRow<Cell>> dataRows,
                             Set<Long> recordIds, boolean isLevel2) {
		if (recordIds.isEmpty()) {
			return;
		}
		// получение данных по ссылкам
        RefBook refBook = refBookFactory.getByAttribute(attributeId);
        RefBookDataProvider provider = refBookFactory.getDataProvider(refBook.getId());
		Map<Long, RefBookValue> values = provider.dereferenceValues(attributeId, recordIds);
		// псевдоним для получения значений ссылки
		String valueAlias = parentColumn == null ? column.getAlias() : parentColumn.getAlias();
		// установка разыменованных значений
		for (DataRow<Cell> dataRow : dataRows) {
			Cell cell = dataRow.getCell(column.getAlias());
			Cell valueCell = dataRow.getCell(valueAlias);
			RefBookValue refBookValue = null;
			if (!isLevel2) {
				BigDecimal reference = valueCell.getNumericValue();
				if (reference != null) {
					refBookValue = values.get(reference.longValue());
				}
			} else { // случай для разыменования второго уровня
				String reference = valueCell.getRefBookDereference();
				if (reference != null && !reference.isEmpty()) {
					refBookValue = values.get(Long.valueOf(reference));
				}
			}
            if (refBookValue != null && RefBookAttributeType.DATE.equals(refBookValue.getAttributeType()) && refBookValue.getDateValue() != null) {
                RefBookAttribute attribute = refBook.getAttribute(attributeId);
                cell.setRefBookDereference(attribute.getFormat() != null ?
                        (new SimpleDateFormat(attribute.getFormat().getFormat())).format(refBookValue.getDateValue()) :
                        String.valueOf(refBookValue));
            } else {
                cell.setRefBookDereference(refBookValue == null ? "" : String.valueOf(refBookValue));
            }
		}
	}

	@Override
    public void dataRowsDereference(Logger logger, Collection<DataRow<Cell>> dataRows, List<Column> columns) {
		if (dataRows.isEmpty() || columns.isEmpty()) {
			return;
		}
		for (Column column : columns) {
			if (ColumnType.REFBOOK.equals(column.getColumnType()) || ColumnType.REFERENCE.equals(column.getColumnType())) {
				Column parentColumn = null;
				// псевдоним для получения значений ссылки
				String valueAlias;
				if (ColumnType.REFBOOK.equals(column.getColumnType())) {
					valueAlias = column.getAlias();
				} else {
					ReferenceColumn refColumn = ((ReferenceColumn) column);
					parentColumn = getColumnById(refColumn.getParentId(), columns);
					valueAlias = parentColumn.getAlias();
				}
				// сбор всех ссылок
				Set<Long> recordIds = new HashSet<Long>();
				for (DataRow<Cell> dataRow : dataRows) {
					Object value = dataRow.get(valueAlias);
					if (value != null) {
						recordIds.add((Long) value);
					}
				}
				if (!recordIds.isEmpty()) {
					// установка значений
					Long attributeId = ColumnType.REFBOOK.equals(column.getColumnType()) ?
						((RefBookColumn) column).getRefBookAttributeId() :
						((ReferenceColumn) column).getRefBookAttributeId();
					dereference(column, parentColumn, attributeId, dataRows, recordIds, false);
				}
			}
		}

        // разыменовывание ссылок второго уровня
        for (Column column : columns) {
            if (ColumnType.REFBOOK.equals(column.getColumnType()) || ColumnType.REFERENCE.equals(column.getColumnType())) {
                Long refBookAttributeId2 = (ColumnType.REFBOOK.equals(column.getColumnType()) ?
                        ((RefBookColumn) column).getRefBookAttributeId2() :
                        ((ReferenceColumn) column).getRefBookAttributeId2());
                if (refBookAttributeId2 != null) {
                    // сбор всех ссылок на справочники по графе
                    Set<Long> recordIds = new HashSet<Long>();
                    for (DataRow<Cell> dataRow : dataRows) {
                        Cell cell = dataRow.getCell(column.getAlias());
                        String value = cell.getRefBookDereference();
                        if (value != null && !value.isEmpty()) {
                            recordIds.add(Long.valueOf(value));
                        }
                    }
                    if (!recordIds.isEmpty()) {
                        // установка значений
                        dereference(column, null, refBookAttributeId2, dataRows, recordIds, true);
                    }
                }
            }
        }
	}

    @Override
    public Map<Long, List<Long>> getAttrToListAttrId2Map(List<RefBookAttribute> attributes){
        return columnDao.getAttributeId2(attributes);
    }

    @Override
    public Map<Long, RefBookDataProvider> getHashedProviders(List<RefBookAttribute> attributes, Map<Long, List<Long>> attrId2Map){
        //кэшируем список провайдеров для атрибутов-ссылок, чтобы для каждой строки их заново не создавать
        Map<Long, RefBookDataProvider> refProviders = new HashMap<Long, RefBookDataProvider>();
        for (RefBookAttribute attribute : attributes) {
            if (RefBookAttributeType.REFERENCE.equals(attribute.getAttributeType())) {
                if (!refProviders.containsKey(attribute.getId())) {
                    refProviders.put(attribute.getId(), refBookFactory.getDataProvider(attribute.getRefBookId()));
                }
                // проверяем если на этот атрибут для колонок дополнительные отрибуты и кешируем продайдеров
                List<Long> id2s = attrId2Map.get(attribute.getId());
                if (id2s != null) {
                    for (Long id2 : id2s) {
                        if (!refProviders.containsKey(id2)) {
                            refProviders.put(id2, refBookFactory.getDataProvider(refBookFactory.getByAttribute(id2).getId()));
                        }
                    }
                }
            }
        }
        return refProviders;
    }

    @Override
    public Map<Long, RefBookDataProvider> getProviders(Set<Long> attributeIds) {
        Map<Long, RefBookDataProvider> result = new HashMap<Long, RefBookDataProvider>();
        for (Long attributeId : attributeIds) {
            Long refBookId = refBookFactory.getByAttribute(attributeId).getId();
            if (!result.containsKey(refBookId)) {
                result.put(refBookId, refBookFactory.getDataProvider(refBookId));
            }
        }
        return result;
    }

    @Override
    public RefBookRecordVersion saveOrUpdateDepartmentConfig(Long uniqueRecordId, long refBookId, long slaveRefBookId,
                                                      int reportPeriodId, String departmentAlias, long departmentId,
                                                      Map<String, RefBookValue> mainConfig,
                                                      List<Map<String, RefBookValue>> tablePart,
                                                      Logger logger) {
        RefBook slaveRefBook = refBookFactory.get(slaveRefBookId);
        RefBookDataProvider provider = refBookFactory.getDataProvider(refBookId);
        RefBookDataProvider providerSlave = refBookFactory.getDataProvider(slaveRefBookId);
        ReportPeriod rp = periodService.getReportPeriod(reportPeriodId);
        String filter = departmentAlias + " = " + departmentId;

        boolean needEdit = false;
        RefBookRecord record = new RefBookRecord();

        // Поиск версий настроек для указанного подразделения. Если они есть - создаем новую версию с существующим record_id, иначе создаем новый record_id (по сути элемент справочника)
        List<Pair<Long, Long>> recordPairsExistence = provider.checkRecordExistence(null, filter);
        if (!recordPairsExistence.isEmpty()) {
            //Проверяем, к одному ли элементу относятся версии
            Set<Long> recordIdSet = new HashSet<Long>();
            for (Pair<Long, Long> pair : recordPairsExistence) {
                recordIdSet.add(pair.getSecond());
            }

            if (recordIdSet.size() > 1) {
                throw new ServiceException("Версии настроек, отобраные по фильтру, относятся к разным подразделениям");
            }

            // Существуют версии настроек для указанного подразделения
            record.setRecordId(recordPairsExistence.get(0).getSecond());
        }

        // Проверяем, нужно ли обновление существующих настроек
        List<Pair<Long, Long>> recordPairs = provider.checkRecordExistence(rp.getCalendarStartDate(), filter);
        if (!recordPairs.isEmpty()) {
            needEdit = true;
            // Запись нашлась
            if (recordPairs.size() != 1) {
                throw new ServiceException("Найдено несколько настроек для подразделения ");
            }
        }

        mainConfig.put(departmentAlias, new RefBookValue(RefBookAttributeType.REFERENCE, departmentId));

        RefBookRecordVersion recordVersion;
        if (!needEdit) {
            record.setValues(mainConfig);
            uniqueRecordId = provider.createRecordVersion(logger, rp.getCalendarStartDate(), null, Arrays.asList(record)).get(0);
        } else {
            provider.updateRecordVersion(logger, uniqueRecordId, rp.getCalendarStartDate(), null, mainConfig);
        }
        recordVersion = provider.getRecordVersionInfo(uniqueRecordId);

        /** Сохраняем табличную часть */
        String filterSlave = "LINK = " + uniqueRecordId;
        RefBookAttribute sortAttr = slaveRefBook.getAttribute("ROW_ORD");

        PagingResult<Map<String, RefBookValue>> paramsSlave = providerSlave.getRecords(rp.getCalendarStartDate(), null, filterSlave, sortAttr);

        Set<Map<String, RefBookValue>> toUpdate = new HashSet<Map<String, RefBookValue>>();
        Set<Map<String, RefBookValue>> toAdd = new HashSet<Map<String, RefBookValue>>();
        Set<Map<String, RefBookValue>> toDelete = new HashSet<Map<String, RefBookValue>>();

        int maxRowOrd = 0;
        for (Map<String, RefBookValue> rowFromClient : tablePart) {
            boolean contains = false;
            for (Map<String, RefBookValue> rowFromServer : paramsSlave) {
                if (rowFromClient.get("TAX_ORGAN_CODE").getStringValue().equals(rowFromServer.get("TAX_ORGAN_CODE").getStringValue())
                        && rowFromClient.get("KPP").getStringValue().equals(rowFromServer.get("KPP").getStringValue())) {
                    contains = true;
                    rowFromClient.put("LINK",new RefBookValue(RefBookAttributeType.REFERENCE, uniqueRecordId));
                    rowFromClient.put("ROW_ORD",rowFromServer.get("ROW_ORD"));
                    rowFromClient.put("record_id",rowFromServer.get("record_id"));
                    rowFromClient.put("DEPARTMENT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, departmentId));
                    toUpdate.add(rowFromClient);
                    break;
                }
            }
            if (rowFromClient.containsKey("ROW_ORD") && rowFromClient.get("ROW_ORD") != null) {
                int rowOrd = rowFromClient.get("ROW_ORD").getNumberValue().intValue();
                if (rowOrd > maxRowOrd) {
                    maxRowOrd = rowOrd;
                }
            }
            if (!contains) {
                rowFromClient.put("LINK", new RefBookValue(RefBookAttributeType.REFERENCE, uniqueRecordId));
                rowFromClient.put("ROW_ORD", new RefBookValue(RefBookAttributeType.NUMBER, ++maxRowOrd));
                rowFromClient.put("DEPARTMENT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, departmentId));
                toAdd.add(rowFromClient);
            }
        }

        List<RefBookRecord> recordsToAdd = new ArrayList<RefBookRecord>();
        for (Map<String, RefBookValue> add : toAdd) {
            RefBookRecord slaveRecord = new RefBookRecord();
            slaveRecord.setValues(add);
            slaveRecord.setRecordId(null);
            recordsToAdd.add(slaveRecord);
        }


        for (Map<String, RefBookValue> rowFromServer : paramsSlave) {
            boolean notFound = true;
            for (Map<String, RefBookValue> rowFromClient : tablePart) {
                if (rowFromClient.get("TAX_ORGAN_CODE").getStringValue().equals(rowFromServer.get("TAX_ORGAN_CODE").getStringValue())
                        && rowFromClient.get("KPP").getStringValue().equals(rowFromServer.get("KPP").getStringValue())) {
                    notFound = false;
                    break;
                }
            }
            if (notFound) {
                toDelete.add(rowFromServer);
            }
        }

        List<Long> deleteIds = new ArrayList<Long>();
        for (Map<String, RefBookValue> del : toDelete) {
            deleteIds.add(del.get("record_id").getNumberValue().longValue());
        }

        if (!logger.containsLevel(LogLevel.ERROR)) {
            if (!recordsToAdd.isEmpty()) {
                providerSlave.createRecordVersion(logger, recordVersion.getVersionStart(), recordVersion.getVersionEnd(), recordsToAdd);
            }

            for (Map<String, RefBookValue> up : toUpdate) {
                providerSlave.updateRecordVersion(logger, up.get("record_id").getNumberValue().longValue(), recordVersion.getVersionStart(), recordVersion.getVersionEnd(), up);
            }

            if (!deleteIds.isEmpty()) {
                providerSlave.deleteRecordVersions(logger, deleteIds);
            }
        }
        return recordVersion;
    }

}
