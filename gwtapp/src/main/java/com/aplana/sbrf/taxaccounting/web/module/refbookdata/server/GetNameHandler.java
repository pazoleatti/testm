package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
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
		String name = refBookFactory.get(action.getRefBookId()).getName();
		GetNameResult result = new GetNameResult();
		result.setName(name);

        if (action.getRecordId() != null) {
            //Получение значений уникальных параметров
            RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(action.getRefBookId());
            List<RefBookValue> values = refBookDataProvider.getUniqueAttributeValues(action.getRecordId());

            StringBuilder uniqueValues = new StringBuilder();

            for(int i = 0; i < values.size(); i++) {
                RefBookValue value = values.get(i);
                switch (value.getAttributeType()) {
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
                        uniqueValues.append("caramba");//TODO
                        /*if (value.getReferenceValue() != null) {
                            Map<String, RefBookValue> refValue = refProviders.get(attribute.getAlias()).getRecordData(value.getReferenceValue());
                            tableCell = refValue.get(refAliases.get(attribute.getAlias())).toString();
                        }*/
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
        }

		return result;
	}

	@Override
	public void undo(GetNameAction getNameAction, GetNameResult getNameResult, ExecutionContext executionContext) throws ActionException {
	}
}
