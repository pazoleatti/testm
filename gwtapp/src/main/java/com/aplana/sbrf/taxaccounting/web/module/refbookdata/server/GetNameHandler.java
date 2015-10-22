package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetNameAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetNameResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@PreAuthorize("isAuthenticated()")
public class GetNameHandler extends AbstractActionHandler<GetNameAction, GetNameResult> {

    @Autowired
    RefBookFactory refBookFactory;

    public GetNameHandler() {
        super(GetNameAction.class);
    }

    @Override
    public GetNameResult execute(GetNameAction action, ExecutionContext executionContext) throws ActionException {
        GetNameResult result = new GetNameResult();

        RefBook refBook = refBookFactory.get(action.getRefBookId());
        result.setName(refBook.getName());
        result.setRefBookType(refBook.getType());

        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(action.getRefBookId());
        //кэшируем список провайдеров для атрибутов-ссылок, чтобы для каждой строки их заново не создавать
        Map<String, RefBookDataProvider> refProviders = new HashMap<String, RefBookDataProvider>();
        Map<String, String> refAliases = new HashMap<String, String>();
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (attribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
                refProviders.put(attribute.getAlias(), refBookFactory.getDataProvider(attribute.getRefBookId()));
                RefBook refRefBook = refBookFactory.get(attribute.getRefBookId());
                RefBookAttribute refAttribute = refRefBook.getAttribute(attribute.getRefBookAttributeId());
                refAliases.put(attribute.getAlias(), refAttribute.getAlias());
            }
        }

        if (action.getUniqueRecordId() != null) {
            //Получение значений уникальных параметров
            Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> attributeValues = refBookDataProvider.getUniqueAttributeValues(action.getUniqueRecordId());

            StringBuilder uniqueValues = new StringBuilder();

            if (!attributeValues.isEmpty()) {
                for (Map.Entry<Integer, List<Pair<RefBookAttribute, RefBookValue>>> entry : attributeValues.entrySet()) {
                    List<Pair<RefBookAttribute, RefBookValue>> values = entry.getValue();
                    for (int i = 0; i < values.size(); i++) {
                        RefBookAttribute attribute = values.get(i).getFirst();
                        RefBookValue value = values.get(i).getSecond();
                        switch (attribute.getAttributeType()) {
                            case NUMBER:
                                if (value.getNumberValue() != null) {
                                    uniqueValues.append(value.getNumberValue().toString());
                                }
                                break;
                            case DATE:
                                if (value.getDateValue() != null) {
                                    uniqueValues.append(value.getDateValue().toString());
                                }
                                break;
                            case STRING:
                                if (value.getStringValue() != null) {
                                    uniqueValues.append(value.getStringValue());
                                }
                                break;
                            case REFERENCE:
                                if (value.getReferenceValue() != null) {
                                    Map<String, RefBookValue> refValue = refProviders.get(attribute.getAlias()).getRecordData(value.getReferenceValue());
                                    uniqueValues.append(refValue.get(refAliases.get(attribute.getAlias())).toString());
                                }
                                break;
                            default:
                                uniqueValues.append("undefined");
                                break;
                        }
                        uniqueValues.append("/");
                    }
                }
                if (uniqueValues.length() > 0) uniqueValues.delete(uniqueValues.length() - 1, uniqueValues.length());
            }
            result.setUniqueAttributeValues(uniqueValues.toString());
            Long recordId = refBookDataProvider.getRecordId(action.getUniqueRecordId());
            result.setRecordId(recordId);
        }

        return result;
    }

	@Override
	public void undo(GetNameAction getNameAction, GetNameResult getNameResult, ExecutionContext executionContext) throws ActionException {
	}
}
