package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
		Long refBookId = refBookFactory.getByAttribute(attributeId).getId();
		RefBookDataProvider provider = refBookFactory.getDataProvider(refBookId);
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
			cell.setRefBookDereference(refBookValue == null ? "" : String.valueOf(refBookValue));
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

}
