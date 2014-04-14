package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.model.*;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: avanteev
 */
@Service
public class RefBookHelperImpl implements RefBookHelper {

	@Autowired
	RefBookFactory refBookFactory;

    @Autowired
    ColumnDao columnDao;

	public void dataRowsDereference(Collection<DataRow<Cell>> dataRows, List<Column> columns) {
		Map<Long, Pair<RefBookDataProvider, RefBookAttribute>> providers = new HashMap<Long, Pair<RefBookDataProvider, RefBookAttribute>>();
		for (DataRow<Cell> dataRow : dataRows) {
			for (Map.Entry<String, Object> entry : dataRow.entrySet()) {
				Cell cell = ((DataRow.MapEntry<Cell>) entry).getCell();
				Object value = cell.getValue();
				if ((cell.getColumn() instanceof RefBookColumn) && value != null) {
                    // Разыменование справочных ячеек
                    RefBookColumn column = (RefBookColumn) cell.getColumn();
                    Long refAttributeId = column.getRefBookAttributeId();
                    dereferenceRefBookValue(providers, refAttributeId, cell, value);
                } else if ((cell.getColumn() instanceof ReferenceColumn)) {
                    // Разыменование ссылочных ячеек
                    ReferenceColumn column = (ReferenceColumn) cell.getColumn();
                    Cell parentCell = dataRow.getCellByColumnId(column.getParentId());
                    value = parentCell.getValue();
                    if (value != null) {
                        dereferenceReferenceValue(providers, column, cell, value);
                    }
                }
			}
		}
	}

    private void dereferenceReferenceValue (Map<Long, Pair<RefBookDataProvider, RefBookAttribute>> providers,
                                            ReferenceColumn referenceColumn, Cell cell, Object value){
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
            record = pair.getFirst().getRecordData(refBookValue.getReferenceValue());
            refBookValue = record.get(pair.getSecond().getAlias());
        }
        cell.setRefBookDereference(String.valueOf(refBookValue));
    }

    private void dereferenceRefBookValue(Map<Long, Pair<RefBookDataProvider, RefBookAttribute>> providers,
                                         Long refAttributeId, Cell cell, Object value) {
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
        cell.setRefBookDereference(String.valueOf(refBookValue));
    }

	@Override
	public Map<String, String> singleRecordDereference(RefBook refBook,	RefBookDataProvider provider,
                                                       List<RefBookAttribute> attributes,	Map<String, RefBookValue> record) {

        //кэшируем список провайдеров для атрибутов-ссылок, чтобы для каждой строки их заново не создавать
        Map<String, RefBookDataProvider> refProviders = new HashMap<String, RefBookDataProvider>();
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (RefBookAttributeType.REFERENCE.equals(attribute.getAttributeType()) && !refProviders.containsKey(attribute.getAlias())) {
                refProviders.put(attribute.getAlias(), refBookFactory.getDataProvider(attribute.getRefBookId()));
            }
        }

        Map<String, String> result = new HashMap<String, String>();
		for (RefBookAttribute attribute : attributes) {
			RefBookAttributeType type = attribute.getAttributeType();
			String alias = attribute.getAlias();
			RefBookValue value = null;
			if (RefBookAttributeType.REFERENCE.equals(type)) {
				Long refValue = record.get(alias).getReferenceValue();
				if (refValue != null) {
					// получаем провайдер данных для целевого справочника
					RefBookDataProvider attrProvider = refProviders.get(alias);
					// запрашиваем значение для разыменовывания
					value = attrProvider.getValue(refValue, attribute.getRefBookAttributeId());
				}
			} else {
				value = record.get(alias);
			}
			result.put(alias, value == null ? "" : String.valueOf(value));
		}
		return result;
	}

    @Override
    public Map<Long, List<Long>> getAttrToListAttrId2Map(List<RefBookAttribute> attributes){
        Map<Long, List<Long>> attrId2Map = new HashMap<Long, List<Long>>();
        for (RefBookAttribute attribute : attributes) {
            List<Long> id2s = columnDao.getAttributeId2(attribute.getId());
            attrId2Map.put(attribute.getId(), id2s);
        }
        return attrId2Map;
    }

    @Override
    public Map<Long, String> singleRecordDereferenceWithAttrId2(RefBook refBook, RefBookDataProvider provider,
                                                                List<RefBookAttribute> attributes, Map<String, RefBookValue> record) {

        //кэшируем список провайдеров для атрибутов-ссылок, чтобы для каждой строки их заново не создавать
        Map<Long, RefBookDataProvider> refProviders = new HashMap<Long, RefBookDataProvider>();
        // кэшируем список дополнительных атрибутов если есть для каждого аттрибута
        Map<Long, List<Long>> attrId2Map = getAttrToListAttrId2Map(refBook.getAttributes());
        for (RefBookAttribute attribute : refBook.getAttributes()) {
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

        Map<Long, String> result = new HashMap<Long, String>();
        for (RefBookAttribute attribute : attributes) {
            String alias = attribute.getAlias();
            Long id = attribute.getId();
            RefBookValue value = null;

            if (RefBookAttributeType.REFERENCE.equals(attribute.getAttributeType())) {
                Long refValue = record.get(alias).getReferenceValue();
                if (refValue != null) {
                    // получаем провайдер данных для целевого справочника
                    RefBookDataProvider attrProvider = refProviders.get(id);
                    // запрашиваем значение для разыменовывания
                    value = attrProvider.getValue(refValue, attribute.getRefBookAttributeId());

                    // для каждого найденного дополнительного аттрибута разименуем значение
                    if (attrId2Map.get(id) != null) {
                        for (Long id2 : attrId2Map.get(id)) {
                            RefBookDataProvider attr2Provider = refProviders.get(id2);
                            RefBookValue value2 = attr2Provider.getValue(refValue, id2);
                            result.put(id2, value2 == null ? "" : String.valueOf(value2));
                        }
                    }
                }
            } else {
                value = record.get(alias);
            }
            result.put(id, value == null ? "" : String.valueOf(value));
        }
        return result;
    }

}
