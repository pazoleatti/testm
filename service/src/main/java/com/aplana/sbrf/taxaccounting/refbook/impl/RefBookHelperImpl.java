package com.aplana.sbrf.taxaccounting.refbook.impl;

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
        if ((refBookValue.getAttributeType()==RefBookAttributeType.REFERENCE)&&(referenceColumn.getRefBookAttributeId2()!=null)){
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
	public Map<String, String> singleRecordDereference(RefBook refBook,
			RefBookDataProvider provider, List<RefBookAttribute> attributes,
			Map<String, RefBookValue> record) {

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

}
