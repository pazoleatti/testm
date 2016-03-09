package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
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
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("isAuthenticated()")
public class GetRefBookRecordHandler extends AbstractActionHandler<GetRefBookRecordAction, GetRefBookRecordResult> {

	@Autowired
	RefBookFactory refBookFactory;

	public GetRefBookRecordHandler() {
		super(GetRefBookRecordAction.class);
	}

	@Override
	public GetRefBookRecordResult execute(GetRefBookRecordAction action, ExecutionContext executionContext) throws ActionException {
		RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(action.getRefBookId());
        RefBook refBookTemp = refBookFactory.get(action.getRefBookId());
        RefBook refBook = SerializationUtils.clone(refBookTemp);
        refBook.setAttributes(new ArrayList<RefBookAttribute>());
        refBook.getAttributes().addAll(refBookTemp.getAttributes());
		GetRefBookRecordResult result = new GetRefBookRecordResult();
		Map<String, RefBookValueSerializable> recordData = new HashMap<String, RefBookValueSerializable>();
		RefBookRecordVersionData fullVersionData = new RefBookRecordVersionData();

        RefBookRecordVersion recordVersion;

        Long recordId;
        if (action.isCreate()) {
            PagingResult<Map<String, RefBookValue>> refBookPage = refBookDataProvider
                    .getRecordVersionsById(action.getUniqueRecordId(), new PagingParams(0, 1000), null, refBook.getAttributes().get(0));
            if (refBookPage.isEmpty()) {
                recordId = null;
            } else {
                recordId = refBookPage.get(refBookPage.getTotalCount()-1).get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
            }

            refBook.getAttributes().add(RefBook.getVersionFromAttribute());
            refBook.getAttributes().add(RefBook.getVersionToAttribute());
        } else {
            recordId = action.getRefBookRecordId();
        }

        Map<String, RefBookValue> record = refBookDataProvider.getRecordData(recordId);

		if (record != null) {
			recordData = convert(refBook, record);

			//Получаем версию выбранной записи
			recordVersion = refBookDataProvider.getRecordVersionInfo(recordId);
			int versionCount = refBookDataProvider.getRecordVersionsCount(recordId);

			if (action.isCreate()) {
				if (recordVersion.getVersionEnd() != null) {
					Calendar calendar = new GregorianCalendar();
					calendar.setTime(recordVersion.getVersionEnd());
					calendar.add(Calendar.DATE, 1);
					fullVersionData.setVersionStart(calendar.getTime());
				} else {
					fullVersionData.setVersionStart(null);
				}
				fullVersionData.setVersionEnd(null);
			} else {
				fullVersionData.setVersionStart(recordVersion.getVersionStart());
				fullVersionData.setVersionEnd(recordVersion.getVersionEnd());
			}
			fullVersionData.setVersionCount(versionCount);
		}

		result.setRecord(recordData);
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
