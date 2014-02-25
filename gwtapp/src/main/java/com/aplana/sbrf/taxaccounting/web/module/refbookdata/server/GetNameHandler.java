package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
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

import java.util.List;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
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

        if (action.getUniqueRecordId() != null) {
            //Получение значений уникальных параметров
            List<Pair<RefBookAttribute, RefBookValue>> values = refBookDataProvider.getUniqueAttributeValues(action.getUniqueRecordId());

            StringBuilder uniqueValues = new StringBuilder();

            for(int i = 0; i < values.size(); i++) {
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
                            RefBookDataProvider referenceDataProvider = refBookFactory.getDataProvider(attribute.getRefBookId());
                            Map<String, RefBookValue> refValue = referenceDataProvider.getRecordData(value.getReferenceValue());
                            uniqueValues.append(refValue.get(attribute.getAlias()).toString());
                        }
                        break;
                    default:
                        uniqueValues.append("undefined");
                        break;
                }
                if (i < values.size() - 1) {
                    uniqueValues.append("/");
                }
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
