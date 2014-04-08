package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecordVersion;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookRecordAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookRecordResult;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookRecordVersionData;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookValueSerializable;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetRefBookRecordHandler extends AbstractActionHandler<GetRefBookRecordAction, GetRefBookRecordResult> {

	@Autowired
	RefBookFactory refBookFactory;

	public GetRefBookRecordHandler() {
		super(GetRefBookRecordAction.class);
	}

	@Override
	public GetRefBookRecordResult execute(GetRefBookRecordAction action, ExecutionContext executionContext) throws ActionException {
		RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(action.getRefBookId());
		Map<String, RefBookValue> record = refBookDataProvider.getRecordData(action.getRefBookRecordId());
		RefBook refBook = refBookFactory.get(action.getRefBookId());
		GetRefBookRecordResult result = new GetRefBookRecordResult();
		result.setRecord(convert(refBook, record));

        RefBookRecordVersion recordVersion;

        //Получаем версию выбранной записи
        recordVersion = refBookDataProvider.getRecordVersionInfo(action.getRefBookRecordId());
        int versionCount = refBookDataProvider.getRecordVersionsCount(action.getRefBookRecordId());

        RefBookRecordVersionData fullVersionData = new RefBookRecordVersionData();
        fullVersionData.setVersionStart(recordVersion.getVersionStart());
        fullVersionData.setVersionEnd(recordVersion.getVersionEnd());
        fullVersionData.setVersionCount(versionCount);
        result.setVersionData(fullVersionData);
		return result;
	}

	private Map<String, RefBookValueSerializable> convert(RefBook refBook, Map<String, RefBookValue> record) {
		Map<String, RefBookValueSerializable> convertedRecord = new HashMap<String, RefBookValueSerializable>();
		for (Map.Entry<String, RefBookValue> recordValue : record.entrySet()) {
			RefBookValueSerializable serializedValue = new RefBookValueSerializable();
			serializedValue.setAttributeType(recordValue.getValue().getAttributeType());
			switch (recordValue.getValue().getAttributeType()) {
				case NUMBER:
					serializedValue.setNumberValue(recordValue.getValue().getNumberValue());
					break;
				case STRING:
					serializedValue.setStringValue(recordValue.getValue().getStringValue());
					break;
				case DATE:
					serializedValue.setDateValue(recordValue.getValue().getDateValue());
					break;
				case REFERENCE:
					if (recordValue.getValue().getReferenceValue() == null) {
						serializedValue.setReferenceValue(null);
						serializedValue.setDereferenceValue("");
					} else {
						serializedValue.setReferenceValue(recordValue.getValue().getReferenceValue());
						// получаем текущий атрибут
						RefBookAttribute attribute = refBook.getAttribute(recordValue.getKey());
						// получаем сам справочник, на который ссылаемся, и его провайдер данных
						RefBook refRefBook = refBookFactory.get(attribute.getRefBookId());
						RefBookDataProvider refDataProvider = refBookFactory.getDataProvider(refRefBook.getId());
						// запрашиваем строку, на которую у нас установлена ссылка
						Map<String, RefBookValue> refValue = refDataProvider.getRecordData(recordValue.getValue().getReferenceValue());
						// извлекаем значение отображаемого свойства (разыменовывание)
						RefBookAttribute refAttribute = refRefBook.getAttribute(attribute.getRefBookAttributeId());
						String dereferenceValue  = refValue.get(refAttribute.getAlias()).toString();
						serializedValue.setDereferenceValue(dereferenceValue);
					}
					break;
				default:
					serializedValue.setStringValue("");
					break;
			}
			convertedRecord.put(recordValue.getKey(), serializedValue);
		}

		return convertedRecord;
	}

	@Override
	public void undo(GetRefBookRecordAction getRefBookRecordAction, GetRefBookRecordResult getRefBookRecordResult, ExecutionContext executionContext) throws ActionException {
	}
}
