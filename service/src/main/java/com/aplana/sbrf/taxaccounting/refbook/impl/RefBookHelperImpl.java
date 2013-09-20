package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.RefBookColumn;
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
					RefBookColumn column = (RefBookColumn) cell.getColumn();
					Long refAttributeId = column.getRefBookAttributeId();
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
			}

		}
	}

	@Override
	public Map<String, String> singleRecordDereference(RefBook refBook,
			RefBookDataProvider provider, List<RefBookAttribute> attributes,
			Map<String, RefBookValue> record) {
        System.out.println("singleRecordDereference started!");

        //кэшируем список провайдеров для атрибутов-ссылок, чтобы для каждой строки их заново не создавать
        Map<String, RefBookDataProvider> refProviders = new HashMap<String, RefBookDataProvider>();
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (attribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
                refProviders.put(attribute.getAlias(), refBookFactory.getDataProvider(attribute.getRefBookId()));
            }
        }

        Map<String, String> result = new HashMap<String, String>();
		for (RefBookAttribute refBookAttribute : attributes) {
			RefBookAttributeType type = record.get(refBookAttribute.getAlias())
					.getAttributeType();
			String alias = refBookAttribute.getAlias();
			if (RefBookAttributeType.REFERENCE.equals(type)) {
				Long longRefValue = record.get(alias).getReferenceValue();
				if (longRefValue == null) {
					result.put(alias, new String());
				} else {
					if (refBook.getId().equals(refBookAttribute.getRefBookId())) {
						RefBookValue val = provider.getValue(longRefValue,
								refBookAttribute.getRefBookAttributeId());
						result.put(alias, String.valueOf(val));
					} else {
                        RefBookValue val = refProviders.get(refBookAttribute.getAlias()).getValue(longRefValue,
                                refBookAttribute.getRefBookAttributeId());
                        result.put(alias, String.valueOf(val));
					}
				}
			} else {
				String derefValue = String.valueOf(record.get(alias));
				result.put(alias, derefValue);
			}
		}
		return result;
	}

}
